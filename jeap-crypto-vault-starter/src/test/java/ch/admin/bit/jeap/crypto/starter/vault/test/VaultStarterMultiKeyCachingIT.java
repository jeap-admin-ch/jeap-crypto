package ch.admin.bit.jeap.crypto.starter.vault.test;

import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.CachingKeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyCacheStats;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ActiveProfiles({"multi-key-test-caching", "jeap-vault"}) // load configuration from application-multi-key-test.yaml
class VaultStarterMultiKeyCachingIT extends AbstractVaultIntegrationTestBase {

    @Qualifier("gamesDbCryptoService")
    @Autowired
    private CryptoService dbCryptoService;

    @Qualifier("gameReviewObjectStoreCryptoService")
    @Autowired
    private CryptoService s3CryptoService;

    @Autowired
    private CachingKeyManagementService cachingKeyManagementService;

    @SneakyThrows
    @Test
    void testEncryptionRoundtripAndSpringConfiguration() {
        byte[] dbBlobV1Bytes = "db blob v1".getBytes(UTF_8);
        byte[] dbBlobV2Bytes = "db blob v2".getBytes(UTF_8);
        byte[] dbBlobV3Bytes = "db blob v3".getBytes(UTF_8);
        byte[] s3BlobV1Bytes = "S3 blob v1".getBytes(UTF_8);
        byte[] s3BlobV2Bytes = "S3 blob v2".getBytes(UTF_8);
        byte[] s3BlobV3Bytes = "S3 blob v3".getBytes(UTF_8);
        assertCacheHitMissCounts(0,0,0,0);

        // cache load encryption keys
        byte[] encrypted = dbCryptoService.encrypt(dbBlobV1Bytes);
        byte[] s3BlobV1Encrypted = s3CryptoService.encrypt(s3BlobV1Bytes);
        assertCacheHitMissCounts(0,2,0,0);

        // cache hit encryption keys
        byte[] encryptedAgain = dbCryptoService.encrypt(dbBlobV2Bytes);
        assertCacheHitMissCounts(1, 2, 0,  0);
        byte[] s3BlobV2Encrypted = s3CryptoService.encrypt(s3BlobV2Bytes);
        assertCacheHitMissCounts(2, 2, 0,  0);

        // cache load decryption keys
        dbCryptoService.decrypt(encrypted);
        s3CryptoService.decrypt(s3BlobV1Encrypted);
        assertCacheHitMissCounts(2, 2, 0,  2);

        // cache hit decryption keys
        dbCryptoService.decrypt(encryptedAgain);
        assertCacheHitMissCounts(2, 2, 1, 2);
        s3CryptoService.decrypt(s3BlobV2Encrypted);
        assertCacheHitMissCounts(2, 2, 2, 2);

        // wait for the DB cache entries to expire
        Thread.sleep(4000);

        // cache miss encryption key DB, but hit S3 (S3 is on longer default expiry, DB on shorter custom expiry)
        byte[] encryptedBye = dbCryptoService.encrypt(dbBlobV3Bytes);
        assertCacheHitMissCounts(2, 3, 2, 2);
        byte[] s3BlobV3Encrypted = s3CryptoService.encrypt(s3BlobV3Bytes);
        assertCacheHitMissCounts(3, 3, 2, 2);

        // cache miss decryption key DB, but hit S3 (S3 is on longer default expiry, DB on shorter custom expiry)
        dbCryptoService.decrypt(encryptedBye);
        assertCacheHitMissCounts(3, 3, 2, 3);
        s3CryptoService.decrypt(s3BlobV3Encrypted);
        assertCacheHitMissCounts(3, 3, 3, 3);

        // wait for the S3 cache entries to expire
        Thread.sleep(3000);
        s3CryptoService.decrypt(s3BlobV3Encrypted);
        assertCacheHitMissCounts(3, 3, 3, 4);
    }

    private void assertCacheHitMissCounts(long encryptionCacheHitCount, long encryptionCacheMissCount,
                                          long decryptionCacheHitCount, long decryptionCacheMissCount) {
        KeyCacheStats encryptionCacheStats = cachingKeyManagementService.getEncryptionKeyCacheStats();
        assertThat(encryptionCacheStats.hitCount()).isEqualTo(encryptionCacheHitCount);
        assertThat(encryptionCacheStats.missCount()).isEqualTo(encryptionCacheMissCount);
        KeyCacheStats decryptionCacheStats = cachingKeyManagementService.getDecryptionKeyCacheStats();
        assertThat(decryptionCacheStats.hitCount()).isEqualTo(decryptionCacheHitCount);
        assertThat(decryptionCacheStats.missCount()).isEqualTo(decryptionCacheMissCount);        
    }

}
