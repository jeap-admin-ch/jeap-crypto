package ch.admin.bit.jeap.crypto.vault.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.AbstractEncryptedDataKey;

import java.util.Objects;

public class VaultEncryptedDataKey extends AbstractEncryptedDataKey {

    private final int wrappingKeyVersion;

    public VaultEncryptedDataKey(byte[] ciphertext, KeyReference keyReference, int wrappingKeyVersion) {
        super(ciphertext, null, keyReference);
        this.wrappingKeyVersion = wrappingKeyVersion;
    }

    public int wrappingKeyVersion() {
        return wrappingKeyVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VaultEncryptedDataKey that = (VaultEncryptedDataKey) o;
        return wrappingKeyVersion == that.wrappingKeyVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), wrappingKeyVersion);
    }
}
