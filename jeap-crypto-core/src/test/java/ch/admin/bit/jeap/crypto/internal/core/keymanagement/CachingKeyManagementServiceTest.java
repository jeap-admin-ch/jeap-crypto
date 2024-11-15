package ch.admin.bit.jeap.crypto.internal.core.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.aes.EncryptedDataKeyStub;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import com.google.common.testing.FakeTicker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CachingKeyManagementServiceTest {
    private static final Duration KEY_REF_DEFAULT_EXPIRY = Duration.ofHours(3);

    private static final String KEY_REF_A_NAME = "key-ref-a";
    private static final KeyReference KEY_REF_A = new KeyReference(KEY_REF_A_NAME);
    private static final DataKeyPair KEY_REF_A_DATA_KEY_1 = createDataKeyPair(1, KEY_REF_A_NAME);
    private static final EncryptedDataKey KEY_REF_A_ENCRYPTED_DATA_KEY_1 = KEY_REF_A_DATA_KEY_1.encryptedDataKey();
    private static final DataKeyPair KEY_REF_A_DATA_KEY_2 = createDataKeyPair(2, KEY_REF_A_NAME);

    private static final String KEY_REF_B_NAME = "key-ref-b";
    private static final KeyReference KEY_REF_B = new KeyReference(KEY_REF_B_NAME);
    private static final DataKeyPair KEY_REF_B_DATA_KEY_1 = createDataKeyPair(1, KEY_REF_B_NAME);
    private static final EncryptedDataKey KEY_REF_B_ENCRYPTED_DATA_KEY_1 = KEY_REF_B_DATA_KEY_1.encryptedDataKey();
    private static final DataKeyPair KEY_REF_B_DATA_KEY_2 = createDataKeyPair(2, KEY_REF_B_NAME);
    private CryptoMetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = mock(CryptoMetricsService.class);
    }

    @Test
    void testGetDataKey_DefaultExpiry() {
        final KeyManagementService keyManagementService = mock(KeyManagementService.class);
        when(keyManagementService.getDataKey(KEY_REF_A)).thenReturn(KEY_REF_A_DATA_KEY_1, KEY_REF_A_DATA_KEY_2);
        final FakeTicker ticker = new FakeTicker();

        final CachingKeyManagementService ckms = createCachingKeyManagementService(keyManagementService, ticker::read, Map.of());

        // loading cache with keys (t=0)
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_A);

        // advancing time just before default expiry should result in a cache hit (t=default-1s)
        ticker.advance(KEY_REF_DEFAULT_EXPIRY.minusSeconds(1));
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_A);

        // advancing time just after default expiry should result in a cache miss (t=default+1s)
        ticker.advance(Duration.ofSeconds(2));
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_2);
        verify(keyManagementService, times(2)).getDataKey(KEY_REF_A);

        // advancing time some more should result in a cache hit again (t=1.5*default+1s)
        ticker.advance(KEY_REF_DEFAULT_EXPIRY.dividedBy(2));
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_2);
        verify(keyManagementService, times(2)).getDataKey(KEY_REF_A);

        // make sure only key usage from cache are counted in the caching KMS service. the delegate KMS service
        // will count key usage as well when not using a key from the cache.
        verify(metricsService, times(2)).countKeyUsedForEncryption(eq(KEY_REF_A), any());
    }

    @Test
    void testGetDataKey_CustomExpiries() {
        final KeyManagementService keyManagementService = mock(KeyManagementService.class);
        when(keyManagementService.getDataKey(KEY_REF_A)).thenReturn(KEY_REF_A_DATA_KEY_1, KEY_REF_A_DATA_KEY_2);
        when(keyManagementService.getDataKey(KEY_REF_B)).thenReturn(KEY_REF_B_DATA_KEY_1, KEY_REF_B_DATA_KEY_2);
        final FakeTicker ticker = new FakeTicker();

        final CachingKeyManagementService ckms = createCachingKeyManagementService(keyManagementService, ticker::read,
                Map.of(KEY_REF_A, Duration.ofHours(1), KEY_REF_B, Duration.ofHours(2)));

        // loading cache with keys (t=0)
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_A);
        assertGetDataKey(ckms, KEY_REF_B, KEY_REF_B_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_B);

        // advancing time just before the lower of the two custom expiries should result in cache hits (t=1h-1s)
        ticker.advance(Duration.ofHours(1).minusSeconds(1));
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_A);
        assertGetDataKey(ckms, KEY_REF_B, KEY_REF_B_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_B);

        // advancing time just after the lower of the two custom expiries should result in a cache miss for A and a hit for B (t=1h+1s)
        ticker.advance(Duration.ofSeconds(2));
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_2);
        verify(keyManagementService, times(2)).getDataKey(KEY_REF_A);
        assertGetDataKey(ckms, KEY_REF_B, KEY_REF_B_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_B);

        // advancing time just before the higher of the two custom expiries should result in cache hits (t=2h-1s)
        ticker.advance(Duration.ofHours(1).minusSeconds(2));
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_2);
        verify(keyManagementService, times(2)).getDataKey(KEY_REF_A);
        assertGetDataKey(ckms, KEY_REF_B, KEY_REF_B_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_B);

        // advancing time just after the higher of the two custom expiries should result in cache misses (t=2h+1s)
        ticker.advance(Duration.ofSeconds(2));
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_2);
        verify(keyManagementService, times(3)).getDataKey(KEY_REF_A);
        assertGetDataKey(ckms, KEY_REF_B, KEY_REF_B_DATA_KEY_2);
        verify(keyManagementService, times(2)).getDataKey(KEY_REF_B);

        // advancing time somemore should result in cache hits again (t=2h+30m+1s)
        ticker.advance(Duration.ofMinutes(30));
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_2);
        verify(keyManagementService, times(3)).getDataKey(KEY_REF_A);
        assertGetDataKey(ckms, KEY_REF_B, KEY_REF_B_DATA_KEY_2);
        verify(keyManagementService, times(2)).getDataKey(KEY_REF_B);
    }

    @Test
    void testDecryptDataKey_DefaultExpiry() {
        final KeyManagementService keyManagementService = mock(KeyManagementService.class);
        when(keyManagementService.decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1)).thenReturn(KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        final FakeTicker ticker = new FakeTicker();

        final CachingKeyManagementService ckms = createCachingKeyManagementService(keyManagementService, ticker::read, Map.of());

        // loading cache with keys (t=0)
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);

        // advancing time just before default expiry should result in a cache hit (t=default-1s)
        ticker.advance(KEY_REF_DEFAULT_EXPIRY.minusSeconds(1));
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);

        // advancing time just after default expiry should result in a cache miss (t=default+1s)
        ticker.advance(Duration.ofSeconds(2));
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(2)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);

        // advancing time some more should result in a cache hit again (t=1.5*default+1s)
        ticker.advance(Duration.ofMinutes(30));
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(2)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);

        // make sure only key usage from cache are counted in the caching KMS service. the delegate KMS service
        // will count key usage as well when not using a key from the cache.
        verify(metricsService, times(2)).countKeyUsedForDecryption(KEY_REF_A);
    }

    @Test
    void testDecryptDataKey_CustomExpiries() {
        final KeyManagementService keyManagementService = mock(KeyManagementService.class);
        when(keyManagementService.decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1)).thenReturn(KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        when(keyManagementService.decryptDataKey(KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1)).thenReturn(KEY_REF_B_DATA_KEY_1.dataKey().plaintextDataKey());
        final FakeTicker ticker = new FakeTicker();

        final CachingKeyManagementService ckms = createCachingKeyManagementService(keyManagementService, ticker::read,
                Map.of(KEY_REF_A, Duration.ofHours(1), KEY_REF_B, Duration.ofHours(2)));

        // loading cache with keys (t=0)
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);
        assertDecryptDataKey(ckms, KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1, KEY_REF_B_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1);

        // advancing time just before the lower of the two custom expiries should result in cache hits (t=1h-1s)
        ticker.advance(Duration.ofHours(1).minusSeconds(1));
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);
        assertDecryptDataKey(ckms, KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1, KEY_REF_B_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1);

        // advancing time just after the lower of the two custom expiries should result in a cache miss for A and cache hit for B (t=1h+1s)
        ticker.advance(Duration.ofSeconds(2));
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(2)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);
        assertDecryptDataKey(ckms, KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1, KEY_REF_B_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1);

        // advancing time just before the higher of the two custom expiries should result in a cache misses (t=2h-1s)
        ticker.advance(Duration.ofHours(1).minusSeconds(2));
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(2)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);
        assertDecryptDataKey(ckms, KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1, KEY_REF_B_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1);

        // advancing time just after the higher of the two custom expiries should result in a cache misses (t=2h+1s)
        ticker.advance(Duration.ofSeconds(2));
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(3)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);
        assertDecryptDataKey(ckms, KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1, KEY_REF_B_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(2)).decryptDataKey(KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1);

        // advancing time some more should result in a cache hits again (t=2h+30m+1s)
        ticker.advance(Duration.ofMinutes(30));
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(3)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);
        assertDecryptDataKey(ckms, KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1, KEY_REF_B_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(2)).decryptDataKey(KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1);
    }

    @Test
    void test_CacheOff() {
        final KeyManagementService keyManagementService = mock(KeyManagementService.class);
        when(keyManagementService.getDataKey(KEY_REF_A)).thenReturn(KEY_REF_A_DATA_KEY_1, KEY_REF_A_DATA_KEY_2);
        when(keyManagementService.decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1)).thenReturn(KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        final FakeTicker ticker = new FakeTicker();

        // disable caching by setting the cache sizes to zero
        final CachingKeyManagementService ckms = createCachingKeyManagementService(keyManagementService, ticker::read, Map.of(), 0, 0);

        // initial access -> keys fetched
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_A);
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);

        // second access -> keys fetched again, no caching
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_2);
        verify(keyManagementService, times(2)).getDataKey(KEY_REF_A);
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(2)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);
    }

    @Test
    void test_NoCachingForCertainKeyReference() {
        final KeyManagementService keyManagementService = mock(KeyManagementService.class);
        when(keyManagementService.getDataKey(KEY_REF_A)).thenReturn(KEY_REF_A_DATA_KEY_1, KEY_REF_A_DATA_KEY_2);
        when(keyManagementService.getDataKey(KEY_REF_B)).thenReturn(KEY_REF_B_DATA_KEY_1, KEY_REF_B_DATA_KEY_2);
        when(keyManagementService.decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1)).thenReturn(KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        when(keyManagementService.decryptDataKey(KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1)).thenReturn(KEY_REF_B_DATA_KEY_1.dataKey().plaintextDataKey());
        final FakeTicker ticker = new FakeTicker();

        // disable caching for key reference A by setting key for A to expire immediately (expiry duration zero)
        final CachingKeyManagementService ckms = createCachingKeyManagementService(keyManagementService, ticker::read, Map.of(KEY_REF_A, Duration.ofHours(0), KEY_REF_B, KEY_REF_DEFAULT_EXPIRY));

        // initial access -> keys fetched for A and B
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_A);
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);
        assertGetDataKey(ckms, KEY_REF_B, KEY_REF_B_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_B);
        assertDecryptDataKey(ckms, KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1, KEY_REF_B_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1);

        // second access -> keys fetched again for A, but not B
        assertGetDataKey(ckms, KEY_REF_A, KEY_REF_A_DATA_KEY_2);
        verify(keyManagementService, times(2)).getDataKey(KEY_REF_A);
        assertDecryptDataKey(ckms, KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1, KEY_REF_A_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(2)).decryptDataKey(KEY_REF_A, KEY_REF_A_ENCRYPTED_DATA_KEY_1);
        assertGetDataKey(ckms, KEY_REF_B, KEY_REF_B_DATA_KEY_1);
        verify(keyManagementService, times(1)).getDataKey(KEY_REF_B);
        assertDecryptDataKey(ckms, KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1, KEY_REF_B_DATA_KEY_1.dataKey().plaintextDataKey());
        verify(keyManagementService, times(1)).decryptDataKey(KEY_REF_B, KEY_REF_B_ENCRYPTED_DATA_KEY_1);
    }

    private void assertGetDataKey(KeyManagementService keyManagementService, KeyReference keyReference, DataKeyPair expectedDataKey) {
        final DataKeyPair dataKey = keyManagementService.getDataKey(keyReference);
        assertThat(dataKey).isEqualTo(expectedDataKey);
    }

    private void assertDecryptDataKey(KeyManagementService keyManagementService, KeyReference keyReference, EncryptedDataKey encryptedDataKey, byte[] expectedDataKey) {
        final byte[] dataKey = keyManagementService.decryptDataKey(keyReference, encryptedDataKey);
        assertThat(dataKey).isEqualTo(expectedDataKey);
    }

    private CachingKeyManagementService createCachingKeyManagementService(KeyManagementService keyManagementService,
                                                                          Supplier<Long> ticker,
                                                                          Map<KeyReference, Duration> expiryDurations) {
        return createCachingKeyManagementService(keyManagementService, ticker, expiryDurations, 1000, 1000);
    }

    private CachingKeyManagementService createCachingKeyManagementService(KeyManagementService keyManagementService, Supplier<Long> ticker, Map<KeyReference, Duration> expiryDurations,
                                                                          long encryptionMaxCacheSize, long decryptionMaxCacheSize) {
        KeyManagementCachingConfigProperties properties = mockKeyManagementCachingConfigProperties(expiryDurations, encryptionMaxCacheSize, decryptionMaxCacheSize);
        return new CachingKeyManagementService(keyManagementService, properties, metricsService, ticker);
    }

    private KeyManagementCachingConfigProperties mockKeyManagementCachingConfigProperties(Map<KeyReference, Duration> expiryDurations, long encryptionMaxCacheSize, long decryptionMaxCacheSize) {
        KeyManagementCachingConfigProperties props = mock(KeyManagementCachingConfigProperties.class);
        when(props.getEncryptionKeyMaxCacheSize()).thenReturn(encryptionMaxCacheSize);
        when(props.getDecryptionKeyMaxCacheSize()).thenReturn(decryptionMaxCacheSize);
        if (expiryDurations.isEmpty()) {
            when(props.getEncryptionKeyCacheExpiryDuration(any())).thenReturn(KEY_REF_DEFAULT_EXPIRY);
            when(props.getDecryptionKeyCacheExpiryDuration(any())).thenReturn(KEY_REF_DEFAULT_EXPIRY);
        } else {
            expiryDurations.forEach(((keyReference, duration) -> {
                when(props.getEncryptionKeyCacheExpiryDuration(keyReference)).thenReturn(duration);
                when(props.getDecryptionKeyCacheExpiryDuration(keyReference)).thenReturn(duration);
            }));
        }
        return props;
    }

    private static DataKeyPair createDataKeyPair(int version, String key) {
        byte[] plaintext = key.concat("-plain").getBytes(StandardCharsets.UTF_8);
        byte[] fakeCiphertext = key.concat("-encrypted").getBytes(StandardCharsets.UTF_8);
        return new DataKeyPair(new DataKey(plaintext), new EncryptedDataKeyStub(fakeCiphertext));
    }
}
