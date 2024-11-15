package ch.admin.bit.jeap.crypto.internal.core.escrow;

import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;

public class EscrowEncryptedDataKey implements EscrowDataKey {
    private final byte[] ciphertext;
    private final EscrowKeyType escrowKeyType;

    public EscrowEncryptedDataKey(byte[] ciphertext, EscrowKeyType escrowKeyType) {
        this.ciphertext = ciphertext;
        this.escrowKeyType = escrowKeyType;
    }

    @Override
    public byte[] ciphertext() {
        return ciphertext;
    }

    @Override
    public EscrowKeyType escrowKeyType() {
        return escrowKeyType;
    }
}
