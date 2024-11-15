package ch.admin.bit.jeap.crypto.vault.keymanagement;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.CryptoMetricsService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.Ciphertext;
import org.springframework.vault.support.Plaintext;
import org.springframework.vault.support.VaultResponse;

import java.util.Base64;
import java.util.Map;

public class VaultKeyManagementService implements KeyManagementService {

    private static final String PLAINTEXT_ATTRIBUTE = "plaintext";
    private static final String CIPHERTEXT_ATTRIBUTE = "ciphertext";
    private static final String KEY_VERSION_ATTRIBUTE = "key_version";

    private final VaultOperations vaultOperations;
    private final CryptoMetricsService cryptoMetricsService;

    public VaultKeyManagementService(VaultOperations vaultOperations, CryptoMetricsService cryptoMetricsService) {
        this.vaultOperations = vaultOperations;
        this.cryptoMetricsService = cryptoMetricsService;
    }

    @Override
    public DataKeyPair getDataKey(KeyReference wrappingKeyReference) {
        VaultKeyLocation keyLocation = VaultKeyLocation.fromKeyReference(wrappingKeyReference);
        String path = keyLocation.secretEnginePath() + "/datakey/plaintext/" + keyLocation.keyName();
        try {
            DataKeyPair keyPair = getDataKeyPairFromVault(wrappingKeyReference, path);
            cryptoMetricsService.countKeyUsedForEncryption(wrappingKeyReference, keyPair);
            return keyPair;
        } catch (Exception ex) {
            throw CryptoException.getDataKeyFailed(ex, path);
        }
    }

    private DataKeyPair getDataKeyPairFromVault(KeyReference wrappingKeyReference, String path) {
        VaultResponse vaultResponse = vaultOperations.write(path, null);
        if (vaultResponse == null) {
            throw CryptoException.getDataKeyFailed(path);
        }

        Map<String, Object> requiredData = vaultResponse.getRequiredData();
        String plaintextBase64 = (String) requiredData.get(PLAINTEXT_ATTRIBUTE);
        String ciphertextWithVaultPrefix = (String) requiredData.get(CIPHERTEXT_ATTRIBUTE);
        int keyVersion = (int) requiredData.get(KEY_VERSION_ATTRIBUTE);

        int ciphertextStartsAt = ciphertextWithVaultPrefix.lastIndexOf(':') + 1;
        String ciphertextBase64 = ciphertextWithVaultPrefix.substring(ciphertextStartsAt);

        byte[] plaintextBytes = Base64.getDecoder().decode(plaintextBase64);
        byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertextBase64);

        DataKey dataKey = new DataKey(plaintextBytes);
        VaultEncryptedDataKey encryptedDataKey = new VaultEncryptedDataKey(ciphertextBytes, wrappingKeyReference, keyVersion);
        return new DataKeyPair(dataKey, encryptedDataKey);
    }

    @Override
    public byte[] decryptDataKey(KeyReference wrappingKeyReference, EncryptedDataKey dataKey) {
        try {
            VaultKeyLocation vaultKeyLocation = VaultKeyLocation.fromKeyReference(wrappingKeyReference);
            VaultEncryptedDataKey versionedDataKey = (VaultEncryptedDataKey) dataKey;
            byte[] plaintext = decryptDataKeyUsingVault(vaultKeyLocation, versionedDataKey);
            cryptoMetricsService.countKeyUsedForDecryption(wrappingKeyReference);
            return plaintext;
        } catch (Exception ex) {
            throw CryptoException.decryptDataKeyFailed(ex, wrappingKeyReference.toString());
        }
    }

    private byte[] decryptDataKeyUsingVault(VaultKeyLocation vaultKeyLocation, VaultEncryptedDataKey dataKey) {
        byte[] ciphertextBytes = dataKey.ciphertext();
        String ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertextBytes);
        int version = dataKey.wrappingKeyVersion();
        Ciphertext ciphertext = Ciphertext.of("vault:v" + version + ":" + ciphertextBase64);

        VaultTransitOperations vaultTransitOperations = vaultOperations.opsForTransit(vaultKeyLocation.secretEnginePath());
        Plaintext plaintext = vaultTransitOperations.decrypt(vaultKeyLocation.keyName(), ciphertext);
        return plaintext.getPlaintext();
    }
}
