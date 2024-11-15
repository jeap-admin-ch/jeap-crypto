package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MicrometerMetricsService implements CryptoMetricsService {

    private final static Tag WITH_ESCROW_TAG = Tag.of("escrow", "true");
    private final static Tag WITHOUT_ESCROW_TAG = Tag.of("escrow", "false");

    private final MeterRegistry meterRegistry;

    public <K, V> void enableCacheMetrics(Cache<K, V> cache, String cacheName) {
        CaffeineCacheMetrics.monitor(meterRegistry, cache, cacheName);
    }

    public void enableNoopEncryptionMetric(boolean isNoop) {
        meterRegistry.gauge("jeap_crypto_noop", isNoop ? 1 : 0);
    }

    @Override
    public void countKeyUsedForEncryption(KeyReference key, DataKeyPair dataKeyPair) {
        boolean escrow = dataKeyPair.encryptedDataKey().escrowDataKey().isPresent();
        Tags tags = Tags.of(Tag.of("key", key.keyLocation()), escrow ? WITH_ESCROW_TAG : WITHOUT_ESCROW_TAG);
        meterRegistry
                .counter("jeap_crypto_key_encrypt", tags)
                .increment();
    }

    @Override
    public void countKeyUsedForDecryption(KeyReference key) {
        Tags tags = Tags.of(Tag.of("key", key.keyLocation()));
        meterRegistry
                .counter("jeap_crypto_key_decrypt", tags)
                .increment();
    }
}
