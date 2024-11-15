package ch.admin.bit.jeap.crypto.vault.format;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;
import ch.admin.bit.jeap.crypto.vault.keymanagement.VaultEncryptedDataKey;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

class JeapCryptoKeyReferenceDataFormatTest {

    @Test
    void format_parse_roundtrip() {
        JeapCryptoKeyReferenceDataFormat formatter = new JeapCryptoKeyReferenceDataFormat();
        JeapCryptoContainer container = createJeapCryptoContainerFake();

        byte[] bytes = formatter.format(container);

        JeapCryptoContainer result = formatter.parse(bytes);

        assertThat(result.ciphertext())
                .isEqualTo(container.ciphertext());
        assertThat(result.nonce())
                .isEqualTo(container.nonce());
        assertThat(result.encryptedDataKey().wrappingKeyReference())
                .isNotNull();
        assertThat(result.encryptedDataKey().wrappingKeyReference().keyLocation())
                .isEqualTo("testLocation");
        assertThat(result.encryptedDataKey().ciphertext())
                .isEqualTo(container.encryptedDataKey().ciphertext());
        assertThat(((VaultEncryptedDataKey) result.encryptedDataKey()).wrappingKeyVersion())
                .isEqualTo(((VaultEncryptedDataKey) container.encryptedDataKey()).wrappingKeyVersion());
    }

    private static JeapCryptoContainer createJeapCryptoContainerFake() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] dataKeyCiphertext = new byte[60];
        byte[] nonce = new byte[12];
        byte[] ciphertext = new byte[100];
        secureRandom.nextBytes(dataKeyCiphertext);
        secureRandom.nextBytes(nonce);
        secureRandom.nextBytes(ciphertext);
        KeyReference keyReference = new KeyReference("testLocation");
        EncryptedDataKey encryptedDataKey = new VaultEncryptedDataKey(dataKeyCiphertext, keyReference, 1);
        return new JeapCryptoContainer(encryptedDataKey, nonce, ciphertext);
    }
}
