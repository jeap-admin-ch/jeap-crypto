package ch.admin.bit.jeap.crypto.internal.core.aes;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.AbstractEncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;

public class EncryptedDataKeyStub extends AbstractEncryptedDataKey {
    public EncryptedDataKeyStub(byte[] ciphertext, KeyReference wrappingKeyReference) {
        super(ciphertext, null, wrappingKeyReference);
    }

    public EncryptedDataKeyStub(byte[] ciphertext, EscrowDataKey escrowDataKey, KeyReference wrappingKeyReference) {
        super(ciphertext, escrowDataKey, wrappingKeyReference);
    }

    public EncryptedDataKeyStub(byte[] ciphertext) {
        super(ciphertext, null, new KeyReference("test"));
    }
}
