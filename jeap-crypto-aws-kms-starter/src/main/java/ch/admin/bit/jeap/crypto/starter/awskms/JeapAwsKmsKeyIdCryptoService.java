package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.api.*;
import ch.admin.bit.jeap.crypto.starter.awskms.JeapCryptoAwsKmsConfigProperties.AwsKmsKeyConfigProperties;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

class JeapAwsKmsKeyIdCryptoService implements KeyIdCryptoService {

    private final JeapCryptoAwsKmsConfigProperties jeapCryptoAwsKmsConfigProperties;
    private final KeyReferenceCryptoService keyReferenceCryptoService;
    private final Set<KeyId> configuredKeyIds;

    JeapAwsKmsKeyIdCryptoService(JeapCryptoAwsKmsConfigProperties jeapCryptoAwsKmsConfigProperties,
                                 @Qualifier("awsKms") KeyReferenceCryptoService keyReferenceCryptoService) {
        this.jeapCryptoAwsKmsConfigProperties = jeapCryptoAwsKmsConfigProperties;
        this.configuredKeyIds = jeapCryptoAwsKmsConfigProperties.getKeys().keySet().stream()
                .map(KeyId::of).collect(toSet());
        this.keyReferenceCryptoService = keyReferenceCryptoService;
    }

    @Override
    public byte[] encrypt(byte[] plaintext, KeyId keyId) {
        KeyReference keyReference = getKeyReferenceForKeyId(keyId).orElseThrow(
                () -> CryptoException.unknownKeyId(keyId));
        return keyReferenceCryptoService.encrypt(plaintext, keyReference);
    }

    @Override
    public byte[] decrypt(byte[] ciphertextCryptoContainer) {
        return keyReferenceCryptoService.decrypt(ciphertextCryptoContainer);
    }

    @Override
    public Set<KeyId> configuredKeyIds() {
        return configuredKeyIds;
    }

    private Optional<KeyReference> getKeyReferenceForKeyId(KeyId keyId) {
        return Optional.ofNullable(jeapCryptoAwsKmsConfigProperties.getKeys().get(keyId.id()))
                .map(AwsKmsKeyConfigProperties::getKeyReference);
    }

    @Override
    public boolean canDecrypt(byte[] ciphertext) {
        return keyReferenceCryptoService.canDecrypt(ciphertext);
    }
}
