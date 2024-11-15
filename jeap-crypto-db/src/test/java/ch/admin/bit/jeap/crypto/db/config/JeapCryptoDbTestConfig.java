package ch.admin.bit.jeap.crypto.db.config;

import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class JeapCryptoDbTestConfig {

    public static final String ENCRYPTED_PREFIX = "encrypted_";

    @Bean
    public KeyIdCryptoService testCryptoService(){
        KeyIdCryptoService cryptoService = mock(KeyIdCryptoService.class);
        when(cryptoService.encrypt(any(), any())).thenAnswer(i -> (ENCRYPTED_PREFIX + new String(i.getArgument(0), StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8));
        when(cryptoService.decrypt(any())).thenAnswer(i -> (new String((byte[]) i.getArgument(0))).replace(ENCRYPTED_PREFIX, "").getBytes(StandardCharsets.UTF_8));
        when(cryptoService.knows(any())).thenReturn(true);
        return cryptoService;
    }

}
