package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;

import java.time.Duration;

public interface KeyManagementCachingConfigProperties {

    long getEncryptionKeyMaxCacheSize();
    Duration getEncryptionKeyCacheExpiryDuration(KeyReference keyReference);

    long getDecryptionKeyMaxCacheSize();
    Duration getDecryptionKeyCacheExpiryDuration(KeyReference keyReference);

}
