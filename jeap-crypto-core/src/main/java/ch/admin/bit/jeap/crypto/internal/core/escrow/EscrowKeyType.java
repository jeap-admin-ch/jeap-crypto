package ch.admin.bit.jeap.crypto.internal.core.escrow;

public enum EscrowKeyType {
    NONE(null),
    RSA_4096("rsa4096");

    private final String keyTypeId;

    EscrowKeyType(String keyTypeId) {
        this.keyTypeId = keyTypeId;
    }

    public String keyTypeId() {
        return keyTypeId;
    }
}
