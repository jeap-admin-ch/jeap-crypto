package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import ch.admin.bit.jeap.crypto.starter.test.awskms.Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class, properties = "jeap.crypto.awskms.enabled=false")
@Slf4j
class AwsKmsCryptoDisabledIT {

    @Autowired
    private ObjectProvider<CryptoService> cryptoService;

    @Autowired
    private ObjectProvider<KeyIdCryptoService> keyIdCryptoService;

    @Test
    void shouldNotAutoconfigureCryptoBeans() {
        assertThat(cryptoService.getIfAvailable())
                .isNull();
        assertThat(keyIdCryptoService.getIfAvailable())
                .isNull();
    }
}
