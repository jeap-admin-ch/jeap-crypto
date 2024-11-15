package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import com.github.benmanes.caffeine.cache.Cache;

public interface CryptoMetricsService {

    <K, V> void enableCacheMetrics(Cache<K, V> cache, String cacheName);

    void enableNoopEncryptionMetric(boolean isNoop);

    void countKeyUsedForEncryption(KeyReference key, DataKeyPair dataKeyPair);

    void countKeyUsedForDecryption(KeyReference key);
}
