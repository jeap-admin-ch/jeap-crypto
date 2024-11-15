package ch.admin.bit.jeap.crypto.internal.core.dataformat;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.aes.EncryptedDataKeyStub;
import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowEncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class JeapCryptoMultiKeyDataFormatTest {

    @Test
    void formatToByteBuffer() {
        JeapCryptoMultiKeyDataFormat format = new JeapCryptoMultiKeyDataFormat(new EncryptedDataKeyFormatStub());
        JeapCryptoContainer cryptoContainer = createJeapCryptoContainerFake();

        ByteBuffer result = format.formatToByteBuffer(cryptoContainer);
        JeapCryptoContainer parsed = format.parse(result.array());

        assertThat(parsed)
                .isEqualTo(cryptoContainer);
        assertThat(format.canParse(result.array()))
                .isTrue();
    }

    @Test
    void formatToByteBuffer_withEscrowKey() {
        JeapCryptoMultiKeyDataFormat format = new JeapCryptoMultiKeyDataFormat(new EncryptedDataKeyFormatStub());
        JeapCryptoContainer cryptoContainer = createJeapCryptoContainerFakeWithEscrowKey();

        ByteBuffer result = format.formatToByteBuffer(cryptoContainer);
        JeapCryptoContainer parsed = format.parse(result.array());

        assertThat(parsed)
                .isEqualTo(cryptoContainer);
        assertThat(format.canParse(result.array()))
                .isTrue();
    }

    private static JeapCryptoContainer createJeapCryptoContainerFake() {
        return createJeapCryptoContainerFake(null);
    }

    private static JeapCryptoContainer createJeapCryptoContainerFake(EscrowDataKey escrowDataKey) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] dataKeyCiphertext = new byte[60];
        byte[] nonce = new byte[12];
        byte[] ciphertext = new byte[100];
        secureRandom.nextBytes(dataKeyCiphertext);
        secureRandom.nextBytes(nonce);
        secureRandom.nextBytes(ciphertext);
        KeyReference keyReference = new KeyReference("testLocation");
        EncryptedDataKey encryptedDataKey = new EncryptedDataKeyStub(dataKeyCiphertext, escrowDataKey, keyReference);
        return new JeapCryptoContainer(encryptedDataKey, nonce, ciphertext);
    }

    private static JeapCryptoContainer createJeapCryptoContainerFakeWithEscrowKey() {
        EscrowDataKey escrowDataKey = new EscrowEncryptedDataKey("escrow".getBytes(UTF_8), EscrowKeyType.RSA_4096);
        return createJeapCryptoContainerFake(escrowDataKey);
    }
}
