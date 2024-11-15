package ch.admin.bit.jeap.crypto.starter.vault.test;

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
@ActiveProfiles({"multi-key-test", "jeap-vault"}) // load configuration from application-multi-key-test.yaml
class VaultStarterMultiKeyIT extends AbstractVaultIntegrationTestBase {

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

        byte[] helloBytes = "Hello".getBytes(UTF_8);

        byte[] encryptedForDb = dbCryptoService.encrypt(helloBytes);
        byte[] decryptedFromDb = dbCryptoService.decrypt(encryptedForDb);

        byte[] encryptedForS3 = s3CryptoService.encrypt(helloBytes);
        byte[] decryptedFromS3 = s3CryptoService.decrypt(encryptedForS3);

        assertThatThrownBy(() -> s3CryptoService.decrypt(encryptedForDb))
                .describedAs("should not decrypt data encrypted with different key")
                .isInstanceOf(CryptoException.class);

        assertThat(decryptedFromDb)
                .isEqualTo(helloBytes);
        assertThat(decryptedFromS3)
                .isEqualTo(helloBytes);
    }

    @Test
    void testKeyIdEncryptionRoundTrip() {
        final byte[] plainBytes = "Hello".getBytes(UTF_8);
        final KeyId gamesDbKeyId = KeyId.of("gamesDb");
        final KeyId gameReviewObjectStoreKeyId = KeyId.of("gameReviewObjectStore");
        assertThat(keyIdCryptoService.knows(gamesDbKeyId)).isTrue();
        assertThat(keyIdCryptoService.knows(gameReviewObjectStoreKeyId)).isTrue();

        byte[] encryptedForDb = keyIdCryptoService.encrypt(plainBytes, gamesDbKeyId);
        byte[] decryptedForDb = keyIdCryptoService.decrypt(encryptedForDb);

        byte[] encryptedForS3 = keyIdCryptoService.encrypt(plainBytes, gameReviewObjectStoreKeyId);
        byte[] decryptedForS3 = keyIdCryptoService.decrypt(encryptedForS3);

        assertThat(decryptedForDb).isEqualTo(plainBytes);
        assertThat(decryptedForS3).isEqualTo(plainBytes);
        assertThat(encryptedForDb).isNotEqualTo(encryptedForS3);
    }

    @Test
    void testUnknownKeyId() {
        final byte[] plainBytes = "Hello".getBytes(UTF_8);
        final KeyId unknownKeyId = KeyId.of("unknown");
        assertThat(keyIdCryptoService.knows(unknownKeyId)).isFalse();

        assertThatThrownBy( () -> keyIdCryptoService.encrypt(plainBytes, unknownKeyId))
                .isInstanceOf(CryptoException.class);
    }

}
