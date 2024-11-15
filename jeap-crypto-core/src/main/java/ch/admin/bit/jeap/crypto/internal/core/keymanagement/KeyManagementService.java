package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;

public interface KeyManagementService {

    DataKeyPair getDataKey(KeyReference wrappingKeyReference);

    byte[] decryptDataKey(KeyReference wrappingKeyReference, EncryptedDataKey dataKey);
}
