package ch.admin.bit.jeap.crypto.test;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ActiveProfiles({"jeap-vault", "aws-kms"})
@TestPropertySource(properties = "spring.cloud.vault.enabled=false")
@Slf4j
class MultiKmsOnlyAwsKmsKeyIT extends AbstractCryptoIntegrationTestBase {

    @Qualifier("awsKeyCryptoService")
    @Autowired
    private CryptoService qualifiedNameCryptoService;

    @Autowired
    private CryptoService singleBeanCryptoService;

    @Qualifier("awsKms")
    @Autowired
    private KeyIdCryptoService keyIdCryptoService;

    @Test
    void testEncryptionRoundtripAndSpringConfiguration() {
        assertThat(qualifiedNameCryptoService)
                .isSameAs(singleBeanCryptoService);

        byte[] helloBytes = "Hello".getBytes(UTF_8);

        byte[] encrypted = singleBeanCryptoService.encrypt(helloBytes);
        byte[] decrypted = singleBeanCryptoService.decrypt(encrypted);

        assertThat(helloBytes)
                .isEqualTo(decrypted);
    }

    @Test
    void testKeyIdEncryptionRoundTrip() {
        final byte[] plainBytes = "Hello".getBytes(UTF_8);
        final KeyId keyId = KeyId.of("aws-key");
        assertThat(keyIdCryptoService.knows(keyId)).isTrue();

        byte[] encrypted = keyIdCryptoService.encrypt(plainBytes, keyId);
        byte[] decrypted = keyIdCryptoService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plainBytes);
    }

    @Test
    void testUnknownKeyId() {
        final byte[] helloBytes = "Hello".getBytes(UTF_8);
        final KeyId unknownKeyId = KeyId.of("unknown");
        assertThat(keyIdCryptoService.knows(unknownKeyId)).isFalse();

        assertThatThrownBy(() -> keyIdCryptoService.encrypt(helloBytes, unknownKeyId))
                .isInstanceOf(CryptoException.class);
    }

}
