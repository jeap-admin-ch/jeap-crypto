package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ActiveProfiles({"multi-key-test", "aws-test"})
class AwsKmsStarterMultiKeyIT extends AbstractAwsKmsIntegrationTestBase {

    // Inject by qualifier
    @Qualifier("gamesDbCryptoService")
    @Autowired
    private CryptoService dbCryptoService;

    // Inject by field name matching the bean name
    @Autowired
    private CryptoService gamesDbCryptoService;

    @Qualifier("gameReviewObjectStoreCryptoService")
    @Autowired
    private CryptoService s3CryptoService;

    @Autowired
    private CryptoService gameReviewObjectStoreCryptoService;

    @Autowired
    private KeyIdCryptoService keyIdCryptoService;

    @Test
    void testEncryptionRoundtripAndSpringConfiguration() {
        assertThat(dbCryptoService)
                .describedAs("Injection works with qualifier and with bean field name")
                .isSameAs(gamesDbCryptoService);
        assertThat(s3CryptoService)
                .describedAs("Injection works with qualifier and with bean field name")
                .isSameAs(gameReviewObjectStoreCryptoService);

        byte[] helloDbBytes = "Hello DB".getBytes(UTF_8);
        byte[] helloS3Bytes = "Hello S3".getBytes(UTF_8);

        byte[] encryptedForDb = dbCryptoService.encrypt(helloDbBytes);
        byte[] decryptedFromDb = dbCryptoService.decrypt(encryptedForDb);

        byte[] encryptedForS3 = s3CryptoService.encrypt(helloS3Bytes);
        byte[] decryptedFromS3 = s3CryptoService.decrypt(encryptedForS3);

        assertThat(decryptedFromDb)
                .isEqualTo(helloDbBytes);
        assertThat(decryptedFromS3)
                .isEqualTo(helloS3Bytes);
    }

    @Test
    void testKeyIdEncryptionRoundTrip() {
        byte[] plainDbBytes = "Hello DB".getBytes(UTF_8);
        byte[] plainS3Bytes = "Hello S3".getBytes(UTF_8);
        KeyId gamesDbKeyId = KeyId.of("gamesDb");
        KeyId gameReviewObjectStoreKeyId = KeyId.of("gameReviewObjectStore");
        assertThat(keyIdCryptoService.knows(gamesDbKeyId)).isTrue();
        assertThat(keyIdCryptoService.knows(gameReviewObjectStoreKeyId)).isTrue();

        byte[] encryptedForDb = keyIdCryptoService.encrypt(plainDbBytes, gamesDbKeyId);
        byte[] decryptedForDb = keyIdCryptoService.decrypt(encryptedForDb);

        byte[] encryptedForS3 = keyIdCryptoService.encrypt(plainS3Bytes, gameReviewObjectStoreKeyId);
        byte[] decryptedForS3 = keyIdCryptoService.decrypt(encryptedForS3);

        assertThat(decryptedForDb).isEqualTo(plainDbBytes);
        assertThat(decryptedForS3).isEqualTo(plainS3Bytes);
        assertThat(encryptedForDb).isNotEqualTo(encryptedForS3);
    }

    @Test
    void testUnknownKeyId() {
        final byte[] plainBytes = "Hello".getBytes(UTF_8);
        final KeyId unknownKeyId = KeyId.of("unknown");
        assertThat(keyIdCryptoService.knows(unknownKeyId)).isFalse();

        assertThatThrownBy(() -> keyIdCryptoService.encrypt(plainBytes, unknownKeyId))
                .isInstanceOf(CryptoException.class);
    }

}
