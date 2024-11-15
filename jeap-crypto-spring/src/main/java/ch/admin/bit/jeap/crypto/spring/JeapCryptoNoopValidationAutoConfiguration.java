package ch.admin.bit.jeap.crypto.spring;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@AutoConfiguration
@RequiredArgsConstructor
@ConditionalOnProperty("jeap.crypto.disabledForTestEnv")
public class JeapCryptoNoopValidationAutoConfiguration {

    private final Environment environment;

    @PostConstruct
    public void assertValidConfiguration() {
        if (isAbnOrProd()) {
            throw new IllegalStateException("Encryption is disabled by property jeap.crypto.disabledForTestEnv. This must not be enabled for abn or prod environment.");
        }
    }

    private boolean isAbnOrProd() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("abn") || profile.equalsIgnoreCase("prod"));
    }

}
