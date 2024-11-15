package ch.admin.bit.jeap.crypto.awskms.key;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.AbstractEncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;

import java.util.Objects;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class AwsKmsEncryptedDataKey extends AbstractEncryptedDataKey {

    private final String keyId;

    public AwsKmsEncryptedDataKey(byte[] ciphertext, EscrowDataKey escrowDataKey, String keyId) {
        super(ciphertext, escrowDataKey, new KeyReference(keyId));
        this.keyId = keyId;
    }

    String keyId() {
        return keyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AwsKmsEncryptedDataKey that = (AwsKmsEncryptedDataKey) o;
        return Objects.equals(keyId, that.keyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), keyId);
    }
}
