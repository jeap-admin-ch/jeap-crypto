package ch.admin.bit.jeap.crypto.test;

import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ActiveProfiles({"encryption-disabled", "vault-kms", "aws-kms", "jeap-vault"})
@TestPropertySource(properties = "spring.cloud.vault.enabled=false")
class MultiKmsEncryptionDisabledIT extends AbstractCryptoIntegrationTestBase {

    @Qualifier("vaultKeyCryptoService")
    @Autowired
    private CryptoService vaultCryptoService;

    @Qualifier("awsKeyCryptoService")
    @Autowired
    private CryptoService awsKmsCryptoService;

    @Qualifier("vault")
    @Autowired
    private KeyIdCryptoService vaultKeyIdCryptoService;

    @Qualifier("awsKms")
    @Autowired
    private KeyIdCryptoService awsKmsKeyIdCryptoService;

    @Test
    void testEncryptionRoundtrip_vault() {
        byte[] helloBytes = "Hello".getBytes(UTF_8);

        byte[] encrypted = vaultCryptoService.encrypt(helloBytes);
        byte[] decrypted = vaultCryptoService.decrypt(encrypted);

        assertThat(helloBytes)
                .isEqualTo(decrypted);
    }

    @Test
    void testEncryptionRoundtrip_awsKms() {
        byte[] helloBytes = "Hello".getBytes(UTF_8);

        byte[] encrypted = awsKmsCryptoService.encrypt(helloBytes);
        byte[] decrypted = awsKmsCryptoService.decrypt(encrypted);

        assertThat(helloBytes)
                .isEqualTo(decrypted);
    }

    @Test
    void testKeyIdEncryptionRoundtrip_vault() {
        final byte[] plainBytes = "Hello".getBytes(UTF_8);
        final KeyId keyId = KeyId.of("vault-key");
        assertThat(vaultKeyIdCryptoService.knows(keyId)).isTrue();

        byte[] encrypted = vaultKeyIdCryptoService.encrypt(plainBytes, keyId);
        byte[] decrypted = vaultKeyIdCryptoService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plainBytes);
    }

    @Test
    void testKeyIdEncryptionRoundtrip_awsKms() {
        final byte[] plainBytes = "Hello".getBytes(UTF_8);
        final KeyId keyId = KeyId.of("aws-key");
        assertThat(awsKmsKeyIdCryptoService.knows(keyId)).isTrue();

        byte[] encrypted = awsKmsKeyIdCryptoService.encrypt(plainBytes, keyId);
        byte[] decrypted = awsKmsKeyIdCryptoService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plainBytes);
    }
}
