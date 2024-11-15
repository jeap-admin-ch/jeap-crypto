package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.internal.core.dataformat.ByteBufferUtil;
import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowEncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractEncryptedDataKeyFormatTest {

    @Test
    void format() {
        // Given
        AbstractEncryptedDataKeyFormat format = createFormat();

        // When
        EscrowDataKey escrowDataKey = new EscrowEncryptedDataKey("cipher".getBytes(StandardCharsets.UTF_8), EscrowKeyType.RSA_4096);
        byte[] formattedKeyFrame = format.format(escrowDataKey);

        // Then: Parse formatted data key
        ByteBuffer byteBuffer = ByteBuffer.wrap(formattedKeyFrame);
        int frameLength = ByteBufferUtil.readUnsignedShort(byteBuffer);
        int formatId = ByteBufferUtil.readUnsignedShort(byteBuffer);
        int keyTypeLen = ByteBufferUtil.readUnsignedShort(byteBuffer);
        byte[] keyType = new byte[keyTypeLen];
        byteBuffer.get(keyType);
        int ciphertextLength = ByteBufferUtil.readUnsignedShort(byteBuffer);
        byte[] ciphertext = new byte[ciphertextLength];
        byteBuffer.get(ciphertext);

        // Then: Assertions
        assertThat(frameLength)
                .describedAs("Frame length is 4 unsigned short fields plus variable data len")
                .isEqualTo(4 * 2 + keyTypeLen + ciphertextLength);
        assertThat(byteBuffer.position())
                .describedAs("End of buffer reached")
                .isEqualTo(byteBuffer.limit());
        assertThat(formatId)
                .isEqualTo(100);
    }

    private static AbstractEncryptedDataKeyFormat createFormat() {
        return new AbstractEncryptedDataKeyFormat() {
            @Override
            public EncryptedDataKey parse(ByteBuffer byteBuffer) {
                return null;
            }

            @Override
            public boolean canParse(ByteBuffer byteBuffer) {
                return false;
            }

            @Override
            public byte[] format(EncryptedDataKey encryptedDataKey) {
                return new byte[0];
            }
        };
    }
}
