package ch.admin.bit.jeap.crypto.db;

import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JeapCryptoStringConverterTest {

    @Mock
    private KeyIdCryptoService keyIdCryptoService;

    @Test
    void jeapCryptoStringConverter_keyIdNotDefined_throwsIllegalStateException() {
        JeapCryptoDbConfigProperties properties = new JeapCryptoDbConfigProperties();
        assertThatThrownBy(() ->
                new JeapCryptoStringConverter(List.of(keyIdCryptoService), properties))
                .hasMessageContaining(
                        "The JeapCryptoStringConverter is configured for one or more attributes but no encryption key id is declared in the application");
    }

    @Test
    void jeapCryptoStringConverter_keyIdNotKnown_throwsIllegalStateException() {
        JeapCryptoDbConfigProperties properties = new JeapCryptoDbConfigProperties();
        properties.setKeyId("fake");
        when(keyIdCryptoService.knows(KeyId.of("fake"))).thenReturn(false);
        assertThatThrownBy(() -> new JeapCryptoStringConverter(List.of(keyIdCryptoService), properties))
                .hasMessageContaining(
                        "Key id 'fake' is unknown");
    }

    @Test
    void jeapCryptoStringConverter_keyIdKnown_converterCreated() {
        JeapCryptoDbConfigProperties properties = new JeapCryptoDbConfigProperties();
        properties.setKeyId("testKey");
        when(keyIdCryptoService.knows(KeyId.of("testKey"))).thenReturn(true);
        JeapCryptoStringConverter jeapCryptoStringConverter = new JeapCryptoStringConverter(
                List.of(keyIdCryptoService), properties);
        assertThat(jeapCryptoStringConverter).isNotNull();
    }

}
