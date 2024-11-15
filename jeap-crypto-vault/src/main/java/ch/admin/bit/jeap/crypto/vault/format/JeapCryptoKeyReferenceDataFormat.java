package ch.admin.bit.jeap.crypto.vault.format;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.AbstractJeapCryptoDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.DataFormatIdentifier;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;
import ch.admin.bit.jeap.crypto.vault.keymanagement.VaultEncryptedDataKey;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class JeapCryptoKeyReferenceDataFormat extends AbstractJeapCryptoDataFormat {

    private static final int AUTH_TAG_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;
    private static final int AES_256_KEY_SIZE = 32;
    private static final int DATA_KEY_FIELD_LENGTH = AES_256_KEY_SIZE + NONCE_LENGTH + AUTH_TAG_LENGTH;
    private static final int FORMAT_FIELD_LENGTH = 1;
    private static final int KEY_VERSION_FIELD_LENGTH = 4;
    private static final int KEY_PATH_LENGTH_FIELD_LENGTH = 4;
    private static final int NONCE_FIELD_LENGTH = NONCE_LENGTH;

    @Override
    public ByteBuffer formatToByteBuffer(JeapCryptoContainer cryptoContainer) {
        VaultEncryptedDataKey encryptedDataKey = (VaultEncryptedDataKey) cryptoContainer.encryptedDataKey();

        byte[] wrappingKeyReferenceBytes = getWrappingKeyReferenceBytes(encryptedDataKey);
        int length = calculateSize(cryptoContainer, wrappingKeyReferenceBytes.length);
        byte[] dataKeyCiphertext = encryptedDataKey.ciphertext();
        byte[] nonce = cryptoContainer.nonce();
        byte[] ciphertext = cryptoContainer.ciphertext();
        assertFieldLength(dataKeyCiphertext, DATA_KEY_FIELD_LENGTH, "dataKey");
        assertFieldLength(nonce, NONCE_FIELD_LENGTH, "nonce");
        assertCiphertextNotEmpty(ciphertext.length);

        int wrappingKeyVersion = encryptedDataKey.wrappingKeyVersion();

        return ByteBuffer.allocate(length)
                .put(DataFormatIdentifier.KEY_REFERENCE_FORMAT_IDENTIFIER.formatId())
                .putInt(wrappingKeyReferenceBytes.length)
                .put(wrappingKeyReferenceBytes)
                .putInt(wrappingKeyVersion)
                .put(dataKeyCiphertext)
                .put(nonce)
                .put(ciphertext);
    }

    private static byte[] getWrappingKeyReferenceBytes(EncryptedDataKey encryptedDataKey) {
        return encryptedDataKey.requireWrappingKeyReference().keyLocation().getBytes(UTF_8);
    }

    @Override
    public JeapCryptoContainer parse(byte[] dataContainerBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(dataContainerBytes);
        assertFormatIdentifierIsForKeyReferenceFormat(byteBuffer);

        KeyReference wrappingKeyReference = getKeyReference(byteBuffer);
        int wrappingKeyVersion = byteBuffer.getInt();
        byte[] encryptedDataKeyBytes = getEncryptedDataKey(byteBuffer);
        byte[] nonce = getNonce(byteBuffer);
        byte[] ciphertext = getCiphertext(byteBuffer);
        assertBufferHasNoRemainingBytes(byteBuffer);

        EncryptedDataKey encryptedDataKey = new VaultEncryptedDataKey(encryptedDataKeyBytes, wrappingKeyReference, wrappingKeyVersion);
        return new JeapCryptoContainer(encryptedDataKey, nonce, ciphertext);
    }

    private static void assertFormatIdentifierIsForKeyReferenceFormat(ByteBuffer byteBuffer) {
        byte formatIdentifier = byteBuffer.get();
        if (formatIdentifier != DataFormatIdentifier.KEY_REFERENCE_FORMAT_IDENTIFIER.formatId()) {
            throw CryptoException.badContainerFormatIdentifier(formatIdentifier);
        }
    }

    private static KeyReference getKeyReference(ByteBuffer byteBuffer) {
        int wrappingKeyLocationLength = byteBuffer.getInt();
        String wrappingKeyLocation = new String(byteBuffer.array(),
                byteBuffer.arrayOffset() + byteBuffer.position(), wrappingKeyLocationLength, UTF_8);
        byteBuffer.position(byteBuffer.position() + wrappingKeyLocationLength);
        return new KeyReference(wrappingKeyLocation);
    }

    private int calculateSize(JeapCryptoContainer cryptoContainer, int keyReferenceLength) {
        if (cryptoContainer.nonce().length != NONCE_FIELD_LENGTH) {
            throw CryptoException.badNonceLength(cryptoContainer.nonce().length, NONCE_FIELD_LENGTH);
        }
        byte[] encryptedDataKey = cryptoContainer.encryptedDataKey().ciphertext();
        if (encryptedDataKey.length != DATA_KEY_FIELD_LENGTH) {
            throw CryptoException.badKeySize(encryptedDataKey.length, DATA_KEY_FIELD_LENGTH);
        }

        return FORMAT_FIELD_LENGTH +
                KEY_PATH_LENGTH_FIELD_LENGTH + keyReferenceLength +
                KEY_VERSION_FIELD_LENGTH +
                DATA_KEY_FIELD_LENGTH +
                NONCE_FIELD_LENGTH +
                cryptoContainer.ciphertext().length;
    }

    @Override
    public boolean canParse(byte[] dataContainerBytes) {
        return dataContainerBytes.length > 0 && dataContainerBytes[0] == DataFormatIdentifier.KEY_REFERENCE_FORMAT_IDENTIFIER.formatId();
    }
}
