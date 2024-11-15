package ch.admin.bit.jeap.crypto.internal.core.aes;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConfiguredKeyAesGcmCryptoServiceTest {

    @Test
    void encrypt() {
        KeyReference keyReference = new KeyReference("fakeLocation");
        KeyManagementServiceStub keyManagementService = new KeyManagementServiceStub();
        ConfiguredKeyAesGcmCryptoService service = new ConfiguredKeyAesGcmCryptoService(
                keyManagementService, keyReference, new JeapCryptoDataFormatStub());
        byte[] plaintextBytes = "Hello".getBytes(UTF_8);

        byte[] ciphertextContainer = service.encrypt(plaintextBytes);
        byte[] decryptedPlaintext = service.decrypt(ciphertextContainer);

        assertThat(decryptedPlaintext)
                .isEqualTo(plaintextBytes);
        assertThat(keyManagementService.getUsedWrappingKeys())
                .containsOnly(keyReference);
    }
}
