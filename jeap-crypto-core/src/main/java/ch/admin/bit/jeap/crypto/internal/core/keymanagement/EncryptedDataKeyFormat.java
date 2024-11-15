package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;

import java.nio.ByteBuffer;

public interface EncryptedDataKeyFormat {

    EncryptedDataKey parse(ByteBuffer byteBuffer);

    boolean canParse(ByteBuffer byteBuffer);

    byte[] format(EncryptedDataKey encryptedDataKey);

    byte[] format(EscrowDataKey escrowDataKey);
}
