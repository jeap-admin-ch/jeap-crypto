package ch.admin.bit.jeap.crypto.awskms.service;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.awskms.client.AwsKmsClient;
import ch.admin.bit.jeap.crypto.awskms.client.DataKeyResponse;
import ch.admin.bit.jeap.crypto.awskms.key.AwsKmsEncryptedDataKey;
import ch.admin.bit.jeap.crypto.awskms.key.EscrowKeyConfig;
import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowEncryptionService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.CryptoMetricsService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;

import java.util.Map;

import static ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType.NONE;

public class AwsKeyManagementService implements KeyManagementService {

    private final AwsKmsClient kmsClient;
    private final EscrowKeyConfig defaultEscrowKey;
    private final Map<KeyReference, EscrowKeyConfig> escrowKeys;
    private final EscrowEncryptionService escrowEncryptionService;
    private final CryptoMetricsService cryptoMetricsService;

    public AwsKeyManagementService(AwsKmsClient kmsClient,
                                   EscrowKeyConfig defaultEscrowKey,
                                   Map<KeyReference, EscrowKeyConfig> escrowKeys,
                                   EscrowEncryptionService escrowEncryptionService,
                                   CryptoMetricsService cryptoMetricsService) {
        this.kmsClient = kmsClient;
        this.defaultEscrowKey = defaultEscrowKey;
        this.escrowKeys = escrowKeys;
        this.escrowEncryptionService = escrowEncryptionService;
        this.cryptoMetricsService = cryptoMetricsService;
    }

    @Override
    public DataKeyPair getDataKey(KeyReference wrappingKeyReference) {
        DataKeyResponse response = kmsClient.createDataKey(wrappingKeyReference.keyLocation());
        DataKey dataKey = new DataKey(response.dataKey());
        EscrowDataKey escrowDataKey = encryptWithEscrowKey(wrappingKeyReference, dataKey);
        AwsKmsEncryptedDataKey encryptedDataKey = new AwsKmsEncryptedDataKey(
                response.encryptedDataKey(),
                escrowDataKey,
                response.keyId());
        DataKeyPair keyPair = new DataKeyPair(dataKey, encryptedDataKey);
        cryptoMetricsService.countKeyUsedForEncryption(wrappingKeyReference, keyPair);
        return keyPair;
    }

    private EscrowDataKey encryptWithEscrowKey(KeyReference wrappingKeyReference, DataKey dataKey) {
        EscrowKeyConfig escrowKeyConfig = requireEscrowKeyConfig(wrappingKeyReference);
        return escrowEncryptionService.encryptEscrowDataKey(dataKey, escrowKeyConfig.keyType(), escrowKeyConfig.publicKey());
    }

    private EscrowKeyConfig requireEscrowKeyConfig(KeyReference wrappingKeyReference) {
        EscrowKeyConfig escrowKeyConfig = escrowKeys.get(wrappingKeyReference);
        if (escrowKeyConfig == null) {
            return requireDefaultEscrowKeyConfig(wrappingKeyReference);
        }
        return escrowKeyConfig;
    }

    private EscrowKeyConfig requireDefaultEscrowKeyConfig(KeyReference wrappingKeyReference) {
        if (defaultEscrowKey == null || (defaultEscrowKey.keyType() != NONE && defaultEscrowKey.publicKey() == null)) {
            throw CryptoException.missingEscrowKey(wrappingKeyReference);
        }
        return defaultEscrowKey;
    }

    @Override
    public byte[] decryptDataKey(KeyReference wrappingKeyReference, EncryptedDataKey dataKey) {
        byte[] encryptedKmsDataKey = dataKey.ciphertext();
        byte[] plaintext = kmsClient.decryptDataKey(encryptedKmsDataKey);
        cryptoMetricsService.countKeyUsedForDecryption(wrappingKeyReference);
        return plaintext;
    }
}
