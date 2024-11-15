package ch.admin.bit.jeap.crypto.starter.vault;

import ch.admin.bit.jeap.crypto.api.*;
import ch.admin.bit.jeap.crypto.starter.vault.JeapCryptoVaultConfigProperties.VaultKeyConfigProperties;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

class JeapVaultKeyIdCryptoService implements KeyIdCryptoService {

    private final JeapCryptoVaultConfigProperties jeapCryptoVaultConfig;
    private final KeyReferenceCryptoService keyReferenceCryptoService;
    private final Set<KeyId> configuredKeyIds;

    JeapVaultKeyIdCryptoService(JeapCryptoVaultConfigProperties jeapCryptoVaultConfig,
                                @Qualifier("vault") KeyReferenceCryptoService keyReferenceCryptoService) {
        this.jeapCryptoVaultConfig = jeapCryptoVaultConfig;
        this.configuredKeyIds = jeapCryptoVaultConfig.getKeys().keySet().stream()
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
        return Optional.ofNullable(jeapCryptoVaultConfig.getKeys().get(keyId.id()))
                .map(VaultKeyConfigProperties::getKeyReference);
    }

    @Override
    public boolean canDecrypt(byte[] ciphertext) {
        return keyReferenceCryptoService.canDecrypt(ciphertext);
    }
}
