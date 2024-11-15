package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import ch.admin.bit.jeap.crypto.internal.core.noop.NoopKeyIdCryptoService;
import ch.admin.bit.jeap.crypto.internal.core.noop.NoopKeyReferenceCryptoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JeapCryptoAwsKmsConfigProperties.class)
@ConditionalOnExpression("${jeap.crypto.disabledForTestEnv:false} and ${jeap.crypto.awskms.enabled:true}")
public class JeapCryptoAwsKmsNoopConfiguration {

    @Bean
    public static JeapCryptoAwsKmsNoopBeanDefinitionRegistryPostProcessor awsKmsBeanDefinitionRegistryPostProcessor() {
        return new JeapCryptoAwsKmsNoopBeanDefinitionRegistryPostProcessor();
    }

    @Qualifier("awsKms")
    @Bean
    public KeyReferenceCryptoService awsKmsKeyReferenceCryptoService() {
        return new NoopKeyReferenceCryptoService();
    }

    @Qualifier("awsKms")
    @Bean
    public KeyIdCryptoService awsKmsKeyIdCryptoService(JeapCryptoAwsKmsConfigProperties properties) {
        return new NoopKeyIdCryptoService(properties.getKeys().keySet());
    }
}
