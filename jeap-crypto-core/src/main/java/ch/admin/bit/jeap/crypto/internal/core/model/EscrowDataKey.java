package ch.admin.bit.jeap.crypto.internal.core.model;

import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType;

/**
 * A data key, encrypted with an escrow key to be able to decrypt data without access to the original wrapping key.
 * The escrow key is typically an asymmetric encryption key used with an encryption scheme such as RSA or ECIES.
 */
public interface EscrowDataKey {
    byte[] ciphertext();

    EscrowKeyType escrowKeyType();
}
