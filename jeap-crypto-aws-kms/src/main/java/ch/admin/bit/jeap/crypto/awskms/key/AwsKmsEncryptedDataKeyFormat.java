package ch.admin.bit.jeap.crypto.awskms.key;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.ByteBufferUtil;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.AbstractEncryptedDataKeyFormat;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AwsKmsEncryptedDataKeyFormat extends AbstractEncryptedDataKeyFormat {

    private static final byte[] AWS_KMS_AES_256_PROVIDER_ID = "aka256".getBytes(UTF_8);
    private static final int AWS_KMS_DATA_KEY_FORMAT_ID = 1;
    private static final int AWS_KMS_DATA_KEY_FORMAT_LENGTH = 2;

    @Override
    public byte[] format(EncryptedDataKey encryptedDataKey) {
        AwsKmsEncryptedDataKey awsKmsEncryptedDataKey = (AwsKmsEncryptedDataKey) encryptedDataKey;

        byte[] keyIdBytes = awsKmsEncryptedDataKey.keyId().getBytes(UTF_8);
        byte[] ciphertext = awsKmsEncryptedDataKey.ciphertext();
        int frameLength = frameLength(keyIdBytes, ciphertext);

        ByteBuffer byteBuffer = ByteBuffer.allocate(frameLength);
        ByteBufferUtil.writeUnsignedShort(byteBuffer, frameLength);
        ByteBufferUtil.writeUnsignedShort(byteBuffer, AWS_KMS_DATA_KEY_FORMAT_ID);
        ByteBufferUtil.writeUnsignedShort(byteBuffer, AWS_KMS_AES_256_PROVIDER_ID.length);
        byteBuffer.put(AWS_KMS_AES_256_PROVIDER_ID);
        ByteBufferUtil.writeUnsignedShort(byteBuffer, keyIdBytes.length);
        byteBuffer.put(keyIdBytes);
        ByteBufferUtil.writeUnsignedShort(byteBuffer, ciphertext.length);
        byteBuffer.put(ciphertext);

        return byteBuffer.array();
    }

    @Override
    public AwsKmsEncryptedDataKey parse(ByteBuffer byteBuffer) {
        int frameLength = ByteBufferUtil.readUnsignedShort(byteBuffer);
        int dataFormatId = ByteBufferUtil.readUnsignedShort(byteBuffer);
        assertDataFormatId(dataFormatId);
        int keyProviderIdLength = ByteBufferUtil.readUnsignedShort(byteBuffer);
        byte[] keyProviderIdBytes = new byte[keyProviderIdLength];
        byteBuffer.get(keyProviderIdBytes);
        assertKeyProviderId(keyProviderIdBytes);

        int keyIdLength = ByteBufferUtil.readUnsignedShort(byteBuffer);
        byte[] keyIdBytes = new byte[keyIdLength];
        byteBuffer.get(keyIdBytes);

        int ciphertextLength = ByteBufferUtil.readUnsignedShort(byteBuffer);
        byte[] ciphertext = new byte[ciphertextLength];
        assertCorrectFrameLength(keyIdBytes, ciphertext, frameLength);
        byteBuffer.get(ciphertext);
        String keyId = new String(keyIdBytes, UTF_8);

        return new AwsKmsEncryptedDataKey(ciphertext, null, keyId);
    }

    private void assertDataFormatId(int dataFormatId) {
        if (dataFormatId != AWS_KMS_DATA_KEY_FORMAT_ID) {
            throw CryptoException.badDataKeyFormatIdentifier(dataFormatId);
        }
    }

    private static void assertCorrectFrameLength(byte[] keyIdBytes, byte[] ciphertext, int frameLength) {
        int expectedFrameLength = frameLength(keyIdBytes, ciphertext);
        if (frameLength != expectedFrameLength) {
            throw CryptoException.badFieldLength("data key frame length", frameLength, expectedFrameLength);
        }
    }

    private static int frameLength(byte[] keyIdBytes, byte[] ciphertext) {
        return 4 * 2 + // 4 length fields, 2 bytes each (unsigned short with 16 bytes for each field)
                AWS_KMS_DATA_KEY_FORMAT_LENGTH +
                AWS_KMS_AES_256_PROVIDER_ID.length + keyIdBytes.length + ciphertext.length;
    }

    private static void assertKeyProviderId(byte[] keyProviderIdBytes) {
        if (!Arrays.equals(AWS_KMS_AES_256_PROVIDER_ID, keyProviderIdBytes)) {
            throw CryptoException.unknownProviderId(keyProviderIdBytes);
        }
    }

    @Override
    public boolean canParse(ByteBuffer byteBuffer) {
        // 4: 2 bytes data key frame length + 2 bytes data key format ID
        if (byteBuffer.remaining() < 4) {
            return false;
        }

        // Skip data key frame length field
        byteBuffer.position(byteBuffer.position() + 2);

        int formatId = ByteBufferUtil.readUnsignedShort(byteBuffer);
        return formatId == AWS_KMS_DATA_KEY_FORMAT_ID;
    }
}
