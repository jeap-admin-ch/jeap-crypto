package ch.admin.bit.jeap.crypto.awskms.key;

import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType;

import java.security.PublicKey;

public record EscrowKeyConfig(
        EscrowKeyType keyType, PublicKey publicKey) {
}
