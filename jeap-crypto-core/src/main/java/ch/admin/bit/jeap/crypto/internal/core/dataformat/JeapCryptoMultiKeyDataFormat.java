package ch.admin.bit.jeap.crypto.internal.core.dataformat;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.EncryptedDataKeyFormat;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;

import java.nio.ByteBuffer;
import java.util.List;

public class JeapCryptoMultiKeyDataFormat extends AbstractJeapCryptoDataFormat {

    private static final int HEADER_LENGTH = 2;
    private static final int EXTENSION_BLOCK_LENGTH = 2;
    private static final byte[] ZERO_EXTENSION_BLOCK_LENGTH = new byte[]{0x00, 0x00};
    private static final int AES_256_GCM_ALGO_ID = 1;
    private static final byte[] AES_256_GCM_ALGO_ID_BYTES = new byte[]{0x00, AES_256_GCM_ALGO_ID};
    private final EncryptedDataKeyFormat encryptedDataKeyFormat;

    public JeapCryptoMultiKeyDataFormat(EncryptedDataKeyFormat encryptedDataKeyFormat) {
        this.encryptedDataKeyFormat = encryptedDataKeyFormat;
    }

    @Override
    public ByteBuffer formatToByteBuffer(JeapCryptoContainer cryptoContainer) {
        List<byte[]> dataKeyFrames = createDataKeyFrames(cryptoContainer.encryptedDataKey());
        byte numberOfDataKeys = (byte) dataKeyFrames.size();

        byte[] nonce = cryptoContainer.nonce();
        byte[] ciphertext = cryptoContainer.ciphertext();

        int dataKeyFramesLength = dataKeyFrames.stream().mapToInt(f -> f.length).sum();
        int payloadLength = nonce.length + ciphertext.length;
        int length = HEADER_LENGTH +
                dataKeyFramesLength +
                EXTENSION_BLOCK_LENGTH +
                AES_256_GCM_ALGO_ID_BYTES.length +
                payloadLength;

        assertFieldLength(nonce, NONCE_FIELD_LENGTH, "nonce");
        assertCiphertextNotEmpty(ciphertext.length);

        ByteBuffer byteBuffer = ByteBuffer.allocate(length)
                .put(DataFormatIdentifier.MULTI_KEY_FORMAT_IDENTIFIER.formatId())
                .put(numberOfDataKeys);
        dataKeyFrames.forEach(byteBuffer::put);
        byteBuffer.put(ZERO_EXTENSION_BLOCK_LENGTH)
                .put(AES_256_GCM_ALGO_ID_BYTES)
                .put(nonce)
                .put(ciphertext);

        return byteBuffer;
    }

    private List<byte[]> createDataKeyFrames(EncryptedDataKey encryptedDataKey) {
        byte[] dataKeyFrame = encryptedDataKeyFormat.format(encryptedDataKey);
        if (encryptedDataKey.escrowDataKey().isEmpty()) {
            return List.of(dataKeyFrame);
        } else {
            EscrowDataKey escrowDataKey = encryptedDataKey.escrowDataKey().get();
            byte[] escrowDataKeyFrame = encryptedDataKeyFormat.format(escrowDataKey);
            return List.of(dataKeyFrame, escrowDataKeyFrame);
        }
    }

    @Override
    public JeapCryptoContainer parse(byte[] dataContainerBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(dataContainerBytes);
        assertFormatIdentifierIsForMultiKeyFormat(byteBuffer);

        byte numberOfDataKeys = byteBuffer.get();
        EncryptedDataKey encryptedDataKey = readEncryptedDataKey(byteBuffer);
        skipDataKeys(byteBuffer, numberOfDataKeys - 1);
        skipExtensionBlock(byteBuffer);

        int algorithmId = ByteBufferUtil.readUnsignedShort(byteBuffer);
        if (algorithmId != AES_256_GCM_ALGO_ID) {
            throw CryptoException.unknownAlgorithmId(AES_256_GCM_ALGO_ID, algorithmId);
        }

        byte[] nonce = new byte[12];
        byteBuffer.get(nonce);
        int remainingBytes = dataContainerBytes.length - byteBuffer.position();
        byte[] ciphertext = new byte[remainingBytes];
        byteBuffer.get(ciphertext);

        return new JeapCryptoContainer(encryptedDataKey, nonce, ciphertext);
    }

    private static void skipExtensionBlock(ByteBuffer byteBuffer) {
        int extensionBlockLength = ByteBufferUtil.readUnsignedShort(byteBuffer);
        byteBuffer.position(byteBuffer.position() + extensionBlockLength);
    }

    private EncryptedDataKey readEncryptedDataKey(ByteBuffer byteBuffer) {
        return encryptedDataKeyFormat.parse(byteBuffer);
    }

    private void skipDataKeys(ByteBuffer byteBuffer, int keysToSkip) {
        for (int i = 0; i < keysToSkip; i++) {
            int frameLength = ByteBufferUtil.readUnsignedShort(byteBuffer);
            byteBuffer.position(byteBuffer.position() + frameLength - 2);
        }
    }

    private static void assertFormatIdentifierIsForMultiKeyFormat(ByteBuffer byteBuffer) {
        byte formatIdentifier = byteBuffer.get();
        if (formatIdentifier != DataFormatIdentifier.MULTI_KEY_FORMAT_IDENTIFIER.formatId()) {
            throw CryptoException.badContainerFormatIdentifier(formatIdentifier);
        }
    }

    @Override
    public boolean canParse(byte[] dataContainerBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(dataContainerBytes);
        byte formatIdentifier = byteBuffer.get();
        if (formatIdentifier != DataFormatIdentifier.MULTI_KEY_FORMAT_IDENTIFIER.formatId()) {
            return false;
        }
        byte numberOfDataKeys = byteBuffer.get();
        if (numberOfDataKeys < 1) {
            return false;
        }

        return encryptedDataKeyFormat.canParse(byteBuffer);
    }
}
