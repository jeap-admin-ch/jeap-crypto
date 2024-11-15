package ch.admin.bit.jeap.crypto.starter.vault;

import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import ch.admin.bit.jeap.crypto.spring.AbstractNoopBeanDefinitionRegistryPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;

/**
 * A {@link BeanDefinitionRegistryPostProcessor} that registers a no-op bean per configured encryption key:
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
class JeapCryptoVaultNoopBeanDefinitionRegistryPostProcessor extends AbstractNoopBeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        JeapCryptoVaultConfigProperties properties = new JeapCryptoVaultConfigProperties();
        Bindable<JeapCryptoVaultConfigProperties> propertiesBindable = Bindable.ofInstance(properties);
        Binder.get(environment).bind(JeapCryptoVaultConfigProperties.PROPERTY_PREFIX, propertiesBindable);
        properties.postProcessConfiguration(environment);

        properties.getKeys().forEach((keyId, keyConfigProperties) ->
                registerBeans(registry, keyId));
    }
}
