package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.internal.core.dataformat.ByteBufferUtil;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class AbstractEncryptedDataKeyFormat implements EncryptedDataKeyFormat {

    private final static int ESCROW_DATA_KEY_FORMAT_ID = 100;

    @Override
    public byte[] format(EscrowDataKey escrowDataKey) {
        byte[] ciphertext = escrowDataKey.ciphertext();
        byte[] escrowKeyType = escrowDataKey.escrowKeyType().keyTypeId().getBytes(StandardCharsets.UTF_8);

        int frameLength = 4 * 2 + // 4 unsigned short fields = 2 bytes each
                escrowKeyType.length + ciphertext.length;

        ByteBuffer byteBuffer = ByteBuffer.allocate(frameLength);
        ByteBufferUtil.writeUnsignedShort(byteBuffer, frameLength);
        ByteBufferUtil.writeUnsignedShort(byteBuffer, ESCROW_DATA_KEY_FORMAT_ID);
        ByteBufferUtil.writeUnsignedShort(byteBuffer, escrowKeyType.length);
        byteBuffer.put(escrowKeyType);
        ByteBufferUtil.writeUnsignedShort(byteBuffer, ciphertext.length);
        byteBuffer.put(ciphertext);

        return byteBuffer.array();
    }
}
