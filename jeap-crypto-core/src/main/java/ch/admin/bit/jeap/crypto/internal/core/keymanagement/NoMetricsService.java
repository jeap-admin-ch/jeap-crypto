package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import com.github.benmanes.caffeine.cache.Cache;

public class NoMetricsService implements CryptoMetricsService {
    @Override
    public <K, V> void enableCacheMetrics(Cache<K, V> cache, String cacheName) {
        // noop
    }

    @Override
    public void enableNoopEncryptionMetric(boolean isNoop) {
        // noop
    }

    @Override
    public void countKeyUsedForEncryption(KeyReference key, DataKeyPair dataKeyPair) {
        // noop
    }

    @Override
    public void countKeyUsedForDecryption(KeyReference key) {
        // noop
    }
}
