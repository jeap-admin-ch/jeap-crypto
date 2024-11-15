package ch.admin.bit.jeap.crypto.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JeapCryptoNoopValidationAutoConfigurationTest {

    @Mock
    private Environment environment;

    private JeapCryptoNoopValidationAutoConfiguration testee;

    @BeforeEach
    public void setUp() {
        testee = new JeapCryptoNoopValidationAutoConfiguration(environment);
    }

    @Test()
    void shouldFailOnAbnStartup() {
        when(environment.getActiveProfiles()).thenReturn(List.of("profile", "abn").toArray(String[]::new));
        assertThrows(IllegalStateException.class, () -> testee.assertValidConfiguration());
    }

    @Test
    void shouldFailOnProdStartup() {
        when(environment.getActiveProfiles()).thenReturn(List.of("anything", "prod").toArray(String[]::new));
        assertThrows(IllegalStateException.class, () -> testee.assertValidConfiguration());
    }

    @Test
    void shouldSucceed() {
        when(environment.getActiveProfiles()).thenReturn(List.of("vault", "cloud").toArray(String[]::new));
        testee.assertValidConfiguration();
    }
}
