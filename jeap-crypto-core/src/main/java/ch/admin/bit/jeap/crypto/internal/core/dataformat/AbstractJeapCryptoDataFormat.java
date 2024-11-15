package ch.admin.bit.jeap.crypto.internal.core.dataformat;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;

import java.nio.ByteBuffer;

public abstract class AbstractJeapCryptoDataFormat implements JeapCryptoDataFormat {

    private static final int AUTH_TAG_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;
    private static final int AES_256_KEY_SIZE = 32;
    protected static final int NONCE_FIELD_LENGTH = NONCE_LENGTH;
    protected static final int DATA_KEY_FIELD_LENGTH = AES_256_KEY_SIZE + NONCE_LENGTH + AUTH_TAG_LENGTH;

    @Override
    public final byte[] format(JeapCryptoContainer cryptoContainer) {
        ByteBuffer byteBuffer = formatToByteBuffer(cryptoContainer);
        assertBufferHasNoRemainingBytes(byteBuffer);
        return byteBuffer.array();
    }

    protected abstract ByteBuffer formatToByteBuffer(JeapCryptoContainer cryptoContainer);

    protected static void assertBufferHasNoRemainingBytes(ByteBuffer byteBuffer) {
        if (byteBuffer.remaining() != 0) {
            throw CryptoException.unexpectedBufferSize(byteBuffer.remaining());
        }
    }

    protected static byte[] getNonce(ByteBuffer byteBuffer) {
        byte[] nonce = new byte[NONCE_FIELD_LENGTH];
        byteBuffer.get(nonce);
        return nonce;
    }

    protected static byte[] getEncryptedDataKey(ByteBuffer byteBuffer) {
        byte[] encryptedDataKeyBytes = new byte[DATA_KEY_FIELD_LENGTH];
        byteBuffer.get(encryptedDataKeyBytes);
        return encryptedDataKeyBytes;
    }

    protected static byte[] getCiphertext(ByteBuffer byteBuffer) {
        int ciphertextLength = byteBuffer.remaining();
        byte[] ciphertext = new byte[ciphertextLength];
        assertCiphertextNotEmpty(ciphertextLength);
        byteBuffer.get(ciphertext);
        return ciphertext;
    }

    protected static void assertCiphertextNotEmpty(int ciphertextLength) {
        if (ciphertextLength < 1) {
            throw CryptoException.emptyCiphertext();
        }
    }

    protected static void assertFieldLength(byte[] bytes, int expectedLength, String fieldName) {
        int actualLength = bytes.length;
        if (actualLength != expectedLength) {
            throw CryptoException.badFieldLength(fieldName, actualLength, expectedLength);
        }
    }
}
