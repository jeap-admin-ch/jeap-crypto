package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Ticker;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class CachingKeyManagementService implements KeyManagementService {

    private final KeyManagementService keyManagementService;
    private final KeyManagementCachingConfigProperties configProperties;
    private final CryptoMetricsService cryptoMetricsService;
    private Cache<KeyReference, DataKeyPair> encryptionKeyCache;
    private Cache<DecryptionDataKeyCacheKey, byte[]> decryptionKeyCache;

    public CachingKeyManagementService(KeyManagementService keyManagementService,
                                       KeyManagementCachingConfigProperties configProperties,
                                       CryptoMetricsService cryptoMetricsService) {
        this(keyManagementService, configProperties, cryptoMetricsService, Ticker.systemTicker()::read);
    }

    CachingKeyManagementService(KeyManagementService keyManagementService,
                                KeyManagementCachingConfigProperties configProperties,
                                CryptoMetricsService cryptoMetricsService,
                                Supplier<Long> ticker) {
        this.keyManagementService = keyManagementService;
        this.configProperties = configProperties;
        this.cryptoMetricsService = cryptoMetricsService;
        initializeEncryptionKeyCache(ticker::get);
        initializeDecryptionKeyCache(ticker::get);
        initializeCachingMetrics(cryptoMetricsService);
    }

    @Override
    public DataKeyPair getDataKey(KeyReference wrappingKeyReference) {
        if ((encryptionKeyCache == null) || getEncryptionKeyExpiry(wrappingKeyReference).isZero()) {
            return keyManagementService.getDataKey(wrappingKeyReference);
        } else {
            return getDataKeyIfNotCached(wrappingKeyReference);
        }
    }

    private DataKeyPair getDataKeyIfNotCached(KeyReference wrappingKeyReference) {
        DataKeyPair cachedKeyPair = encryptionKeyCache.getIfPresent(wrappingKeyReference);
        if (cachedKeyPair != null) {
            cryptoMetricsService.countKeyUsedForEncryption(wrappingKeyReference, cachedKeyPair);
            return cachedKeyPair;
        }
        return getNewDataKey(wrappingKeyReference);
    }

    private DataKeyPair getNewDataKey(KeyReference wrappingKeyReference) {
        DataKeyPair keyPair = keyManagementService.getDataKey(wrappingKeyReference);
        encryptionKeyCache.put(wrappingKeyReference, keyPair);
        return keyPair;
    }

    @Override
    public byte[] decryptDataKey(KeyReference wrappingKeyReference, EncryptedDataKey dataKey) {
        if ((decryptionKeyCache == null) || getDecryptionKeyExpiry(wrappingKeyReference).isZero()) {
            return keyManagementService.decryptDataKey(wrappingKeyReference, dataKey);
        } else {
            return decryptDataKeyIfNotCached(wrappingKeyReference, dataKey);
        }
    }

    private byte[] decryptDataKeyIfNotCached(KeyReference wrappingKeyReference, EncryptedDataKey dataKey) {
        DecryptionDataKeyCacheKey cacheKey = new DecryptionDataKeyCacheKey(wrappingKeyReference, dataKey);
        byte[] cachedDecryptedKey = decryptionKeyCache.getIfPresent(cacheKey);
        if (cachedDecryptedKey != null) {
            cryptoMetricsService.countKeyUsedForDecryption(wrappingKeyReference);
            return cachedDecryptedKey;
        }
        return decryptDataKey(wrappingKeyReference, dataKey, cacheKey);
    }

    private byte[] decryptDataKey(KeyReference wrappingKeyReference, EncryptedDataKey dataKey, DecryptionDataKeyCacheKey cacheKey) {
        byte[] plaintext = keyManagementService.decryptDataKey(wrappingKeyReference, dataKey);
        decryptionKeyCache.put(cacheKey, plaintext);
        return plaintext;
    }

    public KeyCacheStats getEncryptionKeyCacheStats() {
        return getKeyCacheStats(encryptionKeyCache);
    }

    public KeyCacheStats getDecryptionKeyCacheStats() {
        return getKeyCacheStats(decryptionKeyCache);
    }

    private <K, V> KeyCacheStats getKeyCacheStats(Cache<K, V> cache) {
        final CacheStats stats = cache.stats();
        return new KeyCacheStats(stats.hitCount(), stats.missCount(), stats.evictionCount());
    }

    private void initializeEncryptionKeyCache(Ticker ticker) {
        if (configProperties.getEncryptionKeyMaxCacheSize() > 0) {
            encryptionKeyCache = Caffeine.newBuilder()
                    .maximumSize(configProperties.getEncryptionKeyMaxCacheSize())
                    .expireAfter(new EncryptionKeyExpiry(this::getEncryptionKeyExpiry))
                    .recordStats()
                    .ticker(ticker)
                    .build();
        }
    }

    private void initializeDecryptionKeyCache(Ticker ticker) {
        if (configProperties.getDecryptionKeyMaxCacheSize() > 0) {
            decryptionKeyCache = Caffeine.newBuilder()
                    .maximumSize(configProperties.getDecryptionKeyMaxCacheSize())
                    .expireAfter(new DecryptionKeyExpiry(this::getDecryptionKeyExpiry))
                    .recordStats()
                    .ticker(ticker)
                    .build();
        }
    }

    private void initializeCachingMetrics(CryptoMetricsService cryptoMetricsService) {
        cryptoMetricsService.enableCacheMetrics(encryptionKeyCache, "jeap-crypto-encryption-keys");
        cryptoMetricsService.enableCacheMetrics(decryptionKeyCache, "jeap-crypto-decryption-keys");
    }

    private Duration getEncryptionKeyExpiry(KeyReference keyReference) {
        return configProperties.getEncryptionKeyCacheExpiryDuration(keyReference);
    }

    private Duration getDecryptionKeyExpiry(KeyReference keyReference) {
        return configProperties.getDecryptionKeyCacheExpiryDuration(keyReference);
    }

    private record EncryptionKeyExpiry(Function<KeyReference, Duration> expireAfterCreateDurationFunction)
            implements Expiry<KeyReference, DataKeyPair> {
        @Override
        public long expireAfterCreate(KeyReference keyReference, DataKeyPair value, long currentTime) {
            return expireAfterCreateDurationFunction.apply(keyReference).toNanos();
        }

        @Override
        public long expireAfterUpdate(KeyReference key, DataKeyPair value, long currentTime, long currentDuration) {
            return currentDuration;
        }

        @Override
        public long expireAfterRead(KeyReference key, DataKeyPair value, long currentTime, long currentDuration) {
            return currentDuration;
        }
    }

    private record DecryptionKeyExpiry(Function<KeyReference, Duration> expireAfterCreateDurationFunction)
            implements Expiry<DecryptionDataKeyCacheKey, byte[]> {
        @Override
        public long expireAfterCreate(DecryptionDataKeyCacheKey decryptDataKeyCacheKey, byte[] value, long currentTime) {
            return expireAfterCreateDurationFunction.apply(decryptDataKeyCacheKey.keyReference()).toNanos();
        }

        @Override
        public long expireAfterUpdate(DecryptionDataKeyCacheKey key, byte[] value, long currentTime, long currentDuration) {
            return currentDuration;
        }

        @Override
        public long expireAfterRead(DecryptionDataKeyCacheKey key, byte[] value, long currentTime, long currentDuration) {
            return currentDuration;
        }
    }

    private record DecryptionDataKeyCacheKey(KeyReference keyReference, EncryptedDataKey dataKey) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DecryptionDataKeyCacheKey that = (DecryptionDataKeyCacheKey) o;
            return Objects.equals(keyReference, that.keyReference) && Arrays.equals(dataKey.ciphertext(), that.dataKey.ciphertext());
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(keyReference);
            result = 31 * result + Arrays.hashCode(dataKey.ciphertext());
            return result;
        }
    }
}
