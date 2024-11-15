package ch.admin.bit.jeap.crypto.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import static ch.admin.bit.jeap.crypto.spring.AbstractCryptoBeanDefinitionRegistryPostProcessor.CRYPTO_SERVICE_BEAN_NAME_POSTFIX;

@Slf4j
public abstract class AbstractNoopBeanDefinitionRegistryPostProcessor implements EnvironmentAware,
        BeanDefinitionRegistryPostProcessor {

    protected Environment environment;

    protected void registerBeans(BeanDefinitionRegistry registry, String keyId) {
        String cryptoServiceBeanName = BeanNames.beanNameFromKeyId(keyId, CRYPTO_SERVICE_BEAN_NAME_POSTFIX);
        log.info("Registering noop crypto service bean {} for key id {}", cryptoServiceBeanName, keyId);
        registerNoopCryptoServiceBean(cryptoServiceBeanName, registry);
    }

    private static void registerNoopCryptoServiceBean(String beanName, BeanDefinitionRegistry registry) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClassName("ch.admin.bit.jeap.crypto.internal.core.noop.NoopCryptoService");
        ConstructorArgumentValues constructorArgs = new ConstructorArgumentValues();
        beanDefinition.setConstructorArgumentValues(constructorArgs);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
