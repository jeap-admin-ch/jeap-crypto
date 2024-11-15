package ch.admin.bit.jeap.crypto.internal.core.model;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.KeyReference;

import java.util.Optional;

public interface EncryptedDataKey {

    byte[] ciphertext();

    Optional<EscrowDataKey> escrowDataKey();

    KeyReference wrappingKeyReference();

    default KeyReference requireWrappingKeyReference() {
        if (wrappingKeyReference() == null) {
            throw CryptoException.missingWrappingKeyReference();
        }
        return wrappingKeyReference();
    }
}
