package ch.admin.bit.jeap.crypto.spring;

import ch.admin.bit.jeap.crypto.internal.core.keymanagement.CryptoMetricsService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.MicrometerMetricsService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.NoMetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@AutoConfiguration
public class JeapCryptoMetricsAutoConfiguration {

    @Value("${jeap.crypto.disabledForTestEnv:false}")
    private boolean cryptoDisabledForTestEnv;

    @Bean
    public CryptoMetricsService cryptoMetricsService(Optional<MeterRegistry> meterRegistry) {
        CryptoMetricsService cryptoMetricsService = meterRegistry
                .<CryptoMetricsService>map(MicrometerMetricsService::new)
                .orElseGet(NoMetricsService::new);
        cryptoMetricsService.enableNoopEncryptionMetric(cryptoDisabledForTestEnv);
        return cryptoMetricsService;
    }
}
