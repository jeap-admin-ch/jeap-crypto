package ch.admin.bit.jeap.crypto.internal.core.model;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public final class DataKey {

    private static final Random SECURE_RANDOM = new SecureRandom();

    private final AtomicLong nonceCounter = new AtomicLong(SECURE_RANDOM.nextLong());

    private final byte[] plaintextDataKey;

    public DataKey(byte[] plaintextDataKey) {
        Objects.requireNonNull(plaintextDataKey, "plaintextDataKey");
        this.plaintextDataKey = plaintextDataKey;
    }

    public byte[] plaintextDataKey() {
        return plaintextDataKey;
    }

    /**
     * Generate a new nonce of the given length for this key without repeating nonces.
     *
     * @param nonceLengthBytes Size of the nonce in bytes. Must be at least 8.
     * @return The nonce
     */
    public final byte[] generateNonce(int nonceLengthBytes) {
        if (nonceLengthBytes < Long.BYTES) {
            throw new IllegalArgumentException("Requested nonce length in bytes must be at least %s (was %s)".formatted(Long.BYTES, nonceLengthBytes));
        }
        long counterValue = nonceCounter.incrementAndGet();
        byte[] nonce = new byte[nonceLengthBytes];
        int length = Long.BYTES;
        int offset = nonceLengthBytes - length;
        ByteBuffer.wrap(nonce, offset, length).putLong(counterValue);
        return nonce;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataKey dataKey = (DataKey) o;
        return Arrays.equals(plaintextDataKey, dataKey.plaintextDataKey);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(plaintextDataKey);
    }
}
