package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractEncryptedDataKey implements EncryptedDataKey {

    private final byte[] ciphertext;
    private final EscrowDataKey escrowDataKey;
    private final KeyReference wrappingKeyReference;

    public AbstractEncryptedDataKey(byte[] ciphertext, EscrowDataKey escrowDataKey, KeyReference wrappingKeyReference) {
        this.ciphertext = ciphertext;
        this.escrowDataKey = escrowDataKey;
        this.wrappingKeyReference = wrappingKeyReference;
    }

    @Override
    public final byte[] ciphertext() {
        return ciphertext;
    }

    @Override
    public KeyReference wrappingKeyReference() {
        return wrappingKeyReference;
    }

    @Override
    public Optional<EscrowDataKey> escrowDataKey() {
        return Optional.ofNullable(escrowDataKey);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "wrappingKeyReference=" + wrappingKeyReference +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEncryptedDataKey that = (AbstractEncryptedDataKey) o;
        return Arrays.equals(ciphertext, that.ciphertext) && Objects.equals(wrappingKeyReference, that.wrappingKeyReference);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(wrappingKeyReference);
        result = 31 * result + Arrays.hashCode(ciphertext);
        return result;
    }
}
