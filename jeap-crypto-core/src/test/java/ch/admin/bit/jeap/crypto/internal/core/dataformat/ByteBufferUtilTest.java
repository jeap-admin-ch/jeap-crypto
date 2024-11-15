package ch.admin.bit.jeap.crypto.internal.core.dataformat;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ByteBufferUtilTest {

    @Test
    void writeAndReadUnsignedShort() {
        assertWrittenValueEqualsReadValue(0);
        assertWrittenValueEqualsReadValue(128);
        assertWrittenValueEqualsReadValue(255);
        assertWrittenValueEqualsReadValue(256);
        assertWrittenValueEqualsReadValue(65535);

        assertThrows(IllegalArgumentException.class, () -> writeAndRead(-1));
        assertThrows(IllegalArgumentException.class, () -> writeAndRead(65536));
    }

    private static void assertWrittenValueEqualsReadValue(int writeValue) {
        int readValue = writeAndRead(writeValue);
        assertEquals(writeValue, readValue);
    }

    private static int writeAndRead(int writeValue) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        ByteBufferUtil.writeUnsignedShort(buffer, writeValue);
        buffer.flip();
        return ByteBufferUtil.readUnsignedShort(buffer);
    }
}
