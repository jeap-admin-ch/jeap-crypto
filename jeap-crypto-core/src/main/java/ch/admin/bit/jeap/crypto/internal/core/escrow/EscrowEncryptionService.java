package ch.admin.bit.jeap.crypto.internal.core.escrow;

import ch.admin.bit.jeap.crypto.internal.core.model.DataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;

import java.security.PublicKey;

public interface EscrowEncryptionService {

    EscrowDataKey encryptEscrowDataKey(DataKey DataKey, EscrowKeyType escrowKeyType, PublicKey escrowPublicKey);
}
