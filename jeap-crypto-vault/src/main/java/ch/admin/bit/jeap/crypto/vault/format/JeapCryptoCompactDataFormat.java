package ch.admin.bit.jeap.crypto.vault.format;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.AbstractJeapCryptoDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.DataFormatIdentifier;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;
import ch.admin.bit.jeap.crypto.vault.keymanagement.VaultEncryptedDataKey;

import java.nio.ByteBuffer;


public class JeapCryptoCompactDataFormat extends AbstractJeapCryptoDataFormat {
    private static final int FORMAT_FIELD_LENGTH = 1;
    private static final int KEY_VERSION_FIELD_LENGTH = 4;

    @Override
    public ByteBuffer formatToByteBuffer(JeapCryptoContainer cryptoContainer) {
        byte[] encryptedDataKey = cryptoContainer.encryptedDataKey().ciphertext();
        byte[] nonce = cryptoContainer.nonce();
        byte[] ciphertext = cryptoContainer.ciphertext();
        assertFieldLength(encryptedDataKey, DATA_KEY_FIELD_LENGTH, "dataKey");
        assertFieldLength(nonce, NONCE_FIELD_LENGTH, "nonce");
        assertCiphertextNotEmpty(ciphertext.length);

        VaultEncryptedDataKey dataKey = (VaultEncryptedDataKey) cryptoContainer.encryptedDataKey();
        int wrappingKeyVersion = dataKey.wrappingKeyVersion();

        int length = calculateSize(cryptoContainer);
        return ByteBuffer.allocate(length)
                .put(DataFormatIdentifier.COMPACT_FORMAT_IDENTIFIER.formatId())
                .putInt(wrappingKeyVersion)
                .put(encryptedDataKey)
                .put(nonce)
                .put(ciphertext);
    }

    @Override
    public JeapCryptoContainer parse(byte[] dataContainerBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(dataContainerBytes);
        assertFormatIdentifierIsForCompactFormat(byteBuffer);
        int wrappingKeyVersion = byteBuffer.getInt();
        byte[] encryptedDataKeyBytes = getEncryptedDataKey(byteBuffer);
        byte[] nonce = getNonce(byteBuffer);
        byte[] ciphertext = getCiphertext(byteBuffer);
        assertBufferHasNoRemainingBytes(byteBuffer);

        EncryptedDataKey encryptedDataKey = new VaultEncryptedDataKey(encryptedDataKeyBytes, null, wrappingKeyVersion);
        return new JeapCryptoContainer(encryptedDataKey, nonce, ciphertext);
    }

    private static void assertFormatIdentifierIsForCompactFormat(ByteBuffer byteBuffer) {
        byte formatIdentifier = byteBuffer.get();
        if (formatIdentifier != DataFormatIdentifier.COMPACT_FORMAT_IDENTIFIER.formatId()) {
            throw CryptoException.badContainerFormatIdentifier(formatIdentifier);
        }
    }

    private int calculateSize(JeapCryptoContainer cryptoContainer) {
        if (cryptoContainer.nonce().length != NONCE_FIELD_LENGTH) {
            throw CryptoException.badNonceLength(cryptoContainer.nonce().length, NONCE_FIELD_LENGTH);
        }
        byte[] encryptedDataKey = cryptoContainer.encryptedDataKey().ciphertext();
        if (encryptedDataKey.length != DATA_KEY_FIELD_LENGTH) {
            throw CryptoException.badKeySize(encryptedDataKey.length, DATA_KEY_FIELD_LENGTH);
        }

        return FORMAT_FIELD_LENGTH +
                KEY_VERSION_FIELD_LENGTH +
                DATA_KEY_FIELD_LENGTH +
                NONCE_FIELD_LENGTH +
                cryptoContainer.ciphertext().length;
    }

    @Override
    public boolean canParse(byte[] dataContainerBytes) {
        return dataContainerBytes.length > 0 && dataContainerBytes[0] == DataFormatIdentifier.COMPACT_FORMAT_IDENTIFIER.formatId();
    }
}
