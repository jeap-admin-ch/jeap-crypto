package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import ch.admin.bit.jeap.crypto.starter.test.awskms.Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = Application.class)
@ActiveProfiles({"encryption-disabled", "aws-test"})
@Slf4j
class NoopCryptoIT {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private KeyIdCryptoService keyIdCryptoService;

    @Test
    void shouldTransparentlyEncrypt() {
        byte[] input = "helloworld".getBytes(StandardCharsets.UTF_8);
        KeyId keyId = KeyId.of("the-one");
        assertThat(keyIdCryptoService.knows(keyId)).isTrue();

        assertThat(cryptoService.encrypt(input)).isEqualTo(input);
        assertThat(keyIdCryptoService.encrypt(input, keyId)).isEqualTo(input);
    }

    @Test
    void shouldTransparentlyDecrypt() {
        byte[] input = "helloworld".getBytes(StandardCharsets.UTF_8);
        assertThat(cryptoService.decrypt(input)).isEqualTo(input);
        assertThat(keyIdCryptoService.decrypt(input)).isEqualTo(input);
    }
}
