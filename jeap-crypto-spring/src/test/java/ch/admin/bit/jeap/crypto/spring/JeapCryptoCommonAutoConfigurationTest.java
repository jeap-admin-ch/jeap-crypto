package ch.admin.bit.jeap.crypto.spring;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class JeapCryptoCommonAutoConfigurationTest {

    @Test
    void validateNoDuplicateKeyIds() {
        KeyIdCryptoService service = mock(KeyIdCryptoService.class);
        doReturn(Set.of(KeyId.of("test-1"))).when(service).configuredKeyIds();

        JeapCryptoCommonAutoConfiguration configuration = new JeapCryptoCommonAutoConfiguration(List.of(service));

        assertDoesNotThrow(() -> configuration.validateNoDuplicateKeyIds());
    }

    @Test
    void validateNoDuplicateKeyIds_throws_on_duplicate_key_id() {
        KeyIdCryptoService service1 = mock(KeyIdCryptoService.class);
        doReturn(Set.of(KeyId.of("test-1"))).when(service1).configuredKeyIds();
        KeyIdCryptoService service2 = mock(KeyIdCryptoService.class);
        doReturn(Set.of(KeyId.of("test-1"))).when(service2).configuredKeyIds();

        JeapCryptoCommonAutoConfiguration configuration = new JeapCryptoCommonAutoConfiguration(List.of(service1, service2));

        assertThatThrownBy(() -> configuration.validateNoDuplicateKeyIds())
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("The key ID 'test-1' is used more than once");
    }
}
