package ch.admin.bit.jeap.crypto.vault.format;

import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;
import ch.admin.bit.jeap.crypto.vault.keymanagement.VaultEncryptedDataKey;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

class JeapCryptoCompactDataFormatTest {

    @Test
    void format_parse_roundtrip() {
        JeapCryptoCompactDataFormat formatter = new JeapCryptoCompactDataFormat();
        JeapCryptoContainer container = createJeapCryptoContainerFake();

        byte[] bytes = formatter.format(container);

        JeapCryptoContainer result = formatter.parse(bytes);

        Assertions.assertThat(result.ciphertext())
                .isEqualTo(container.ciphertext());
        Assertions.assertThat(result.nonce())
                .isEqualTo(container.nonce());
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
        EncryptedDataKey encryptedDataKey = new VaultEncryptedDataKey(dataKeyCiphertext, null, 1);
        return new JeapCryptoContainer(encryptedDataKey, nonce, ciphertext);
    }
}
