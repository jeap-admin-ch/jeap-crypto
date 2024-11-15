package ch.admin.bit.jeap.crypto.starter.vault;

import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import ch.admin.bit.jeap.crypto.internal.core.noop.NoopKeyIdCryptoService;
import ch.admin.bit.jeap.crypto.internal.core.noop.NoopKeyReferenceCryptoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JeapCryptoVaultConfigProperties.class)
@ConditionalOnProperty("jeap.crypto.disabledForTestEnv")
public class JeapCryptoVaultNoopConfiguration {

    @Bean
    public static BeanDefinitionRegistryPostProcessor vaultBeanDefinitionRegistryPostProcessor() {
        return new JeapCryptoVaultNoopBeanDefinitionRegistryPostProcessor();
    }

    @Qualifier("vault")
    @Bean
    public KeyReferenceCryptoService vaultKeyReferenceCryptoService() {
        return new NoopKeyReferenceCryptoService();
    }

    @Qualifier("vault")
    @Bean
    public KeyIdCryptoService vaultKeyIdCryptoService(JeapCryptoVaultConfigProperties properties) {
        return new NoopKeyIdCryptoService(properties.getKeys().keySet());
    }

}
