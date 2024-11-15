package ch.admin.bit.jeap.crypto.spring;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * A {@link BeanDefinitionRegistryPostProcessor} that registers two beans per configured encrpytion key:
 * <ul>
 *     <li>A {@link ch.admin.bit.jeap.crypto.api.CryptoService} named _keyName_CryptoService</li>
 *     <li>A {@link KeyManagementService} named _keyName_KeyManagementService</li>
 * </ul>
 * <p>
 * This allows for injection of the crypto service for a specific encryption key using the bean name or a qualifier:
 * <pre>
 *     &#64;Qualifier("myKeyCryptoService")
 *     &#64;Autowired
 *     CryptoService cryptoService;
 * </pre>
 */
@Slf4j
public abstract class AbstractCryptoBeanDefinitionRegistryPostProcessor implements EnvironmentAware, BeanDefinitionRegistryPostProcessor {

    static final String CRYPTO_SERVICE_BEAN_NAME_POSTFIX = "CryptoService";

    protected Environment environment;

    protected void registerBeans(BeanDefinitionRegistry registry, String keyId, KeyReference keyReference,
                                 JeapCryptoDataFormat dataFormat, String kmsBeanName) {
        String cryptoServiceBeanName = BeanNames.beanNameFromKeyId(keyId, CRYPTO_SERVICE_BEAN_NAME_POSTFIX);
        log.info("Registering crypto service bean {} for key id {} at {}", cryptoServiceBeanName, keyId, keyReference);
        registerCryptoServiceBean(cryptoServiceBeanName, registry, keyReference, dataFormat, kmsBeanName);
    }

    private static void registerCryptoServiceBean(String beanName, BeanDefinitionRegistry registry, KeyReference keyReference,
                                                  JeapCryptoDataFormat dataFormat, String kmsBeanName) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClassName("ch.admin.bit.jeap.crypto.internal.core.aes.ConfiguredKeyAesGcmCryptoService");
        ConstructorArgumentValues constructorArgs = new ConstructorArgumentValues();
        constructorArgs.addGenericArgumentValue(new RuntimeBeanReference(kmsBeanName));
        constructorArgs.addGenericArgumentValue(keyReference);
        constructorArgs.addGenericArgumentValue(dataFormat);
        beanDefinition.setConstructorArgumentValues(constructorArgs);
        beanDefinition.setLazyInit(true);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
