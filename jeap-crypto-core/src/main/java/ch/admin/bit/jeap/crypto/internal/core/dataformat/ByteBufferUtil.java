package ch.admin.bit.jeap.crypto.internal.core.dataformat;

import java.nio.ByteBuffer;

public class ByteBufferUtil {

    public static int readUnsignedShort(ByteBuffer byteBuffer) {
        short aShort = byteBuffer.getShort();
        return Short.toUnsignedInt(aShort);
    }

    public static void writeUnsignedShort(ByteBuffer byteBuffer, int shortValue) {
        if (shortValue < 0 || shortValue > 65535) {
            throw new IllegalArgumentException("Short value must be from 0 to 65535");
        }
        byte firstByte = (byte) ((shortValue & 0xFF00) >> 8);
        byte secondByte = (byte) (shortValue & 0x00FF);
        byteBuffer.put(firstByte);
        byteBuffer.put(secondByte);
    }

}
