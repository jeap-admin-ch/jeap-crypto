package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles({"single-key-test", "aws-test"})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Slf4j
class AwsKmsStarterSingleKeyIT extends AbstractAwsKmsIntegrationTestBase {

    @Qualifier("theOneCryptoService")
    @Autowired
    private CryptoService qualifiedNameCryptoService;

    @Autowired
    private CryptoService singleBeanCryptoService;

    @Autowired
    private KeyIdCryptoService keyIdCryptoService;

    @Autowired
    private SimpleMeterRegistry meterRegistry;

    @Test
    void testEncryptionRoundtripAndSpringConfiguration() {
        assertThat(qualifiedNameCryptoService)
                .isSameAs(singleBeanCryptoService);

        byte[] helloBytes = "Hello".getBytes(UTF_8);

        byte[] encrypted = singleBeanCryptoService.encrypt(helloBytes);
        byte[] decrypted = singleBeanCryptoService.decrypt(encrypted);

        assertThat(helloBytes)
                .isEqualTo(decrypted);

        Tags encryptTags = Tags.of(Tag.of("key", testKeyArn), Tag.of("escrow", "true"));
        assertThat(meterRegistry.counter("jeap_crypto_key_encrypt", encryptTags).count())
                .isEqualTo(1.0);
        Tags decryptTags = Tags.of(Tag.of("key", testKeyArn));
        assertThat(meterRegistry.counter("jeap_crypto_key_decrypt", decryptTags).count())
                .isEqualTo(1.0);
    }

    @Test
    void testKeyIdEncryptionRoundTrip() {
        final byte[] plainBytes = "Hello".getBytes(UTF_8);
        final KeyId theOneKeyId = KeyId.of("the-one");
        assertThat(keyIdCryptoService.knows(theOneKeyId)).isTrue();

        byte[] encrypted = keyIdCryptoService.encrypt(plainBytes, theOneKeyId);
        byte[] decrypted = keyIdCryptoService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plainBytes);
    }

    @Test
    void testUnknownKeyId() {
        final byte[] helloBytes = "Hello".getBytes(UTF_8);
        final KeyId unknownKeyId = KeyId.of("unknown");
        assertThat(keyIdCryptoService.knows(unknownKeyId)).isFalse();

        assertThatThrownBy(() -> keyIdCryptoService.encrypt(helloBytes, unknownKeyId))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("Key id 'unknown' is unknown");
    }
}
