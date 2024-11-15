package ch.admin.bit.jeap.crypto.internal.core.model;

import java.util.Arrays;
import java.util.Objects;

public record JeapCryptoContainer(
        EncryptedDataKey encryptedDataKey,
        byte[] nonce,
        byte[] ciphertext) {

    @Override
    public String toString() {
        return "JeapCryptoContainer{" +
                "encryptedDataKey=" + encryptedDataKey +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JeapCryptoContainer that = (JeapCryptoContainer) o;
        return Objects.equals(encryptedDataKey, that.encryptedDataKey) && Arrays.equals(nonce, that.nonce) && Arrays.equals(ciphertext, that.ciphertext);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(encryptedDataKey);
        result = 31 * result + Arrays.hashCode(nonce);
        result = 31 * result + Arrays.hashCode(ciphertext);
        return result;
    }
}
