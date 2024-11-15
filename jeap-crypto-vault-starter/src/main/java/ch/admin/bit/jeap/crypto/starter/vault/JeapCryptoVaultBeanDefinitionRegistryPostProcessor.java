package ch.admin.bit.jeap.crypto.starter.vault;

import ch.admin.bit.jeap.crypto.spring.AbstractCryptoBeanDefinitionRegistryPostProcessor;
import ch.admin.bit.jeap.crypto.vault.format.JeapCryptoCompactDataFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;

/**
 * A {@link BeanDefinitionRegistryPostProcessor} that registers two beans per configured encrpytion key:
 * <ul>
 *     <li>A {@link ch.admin.bit.jeap.crypto.api.CryptoService} named _keyName_CryptoService</li>
 *     <li>A {@link ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService} named _keyName_KeyManagementService</li>
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
class JeapCryptoVaultBeanDefinitionRegistryPostProcessor extends AbstractCryptoBeanDefinitionRegistryPostProcessor {

    private static final String VAULT_KEY_MANAGEMENT_SERVICE = "vaultKeyManagementService";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        JeapCryptoVaultConfigProperties properties = new JeapCryptoVaultConfigProperties();
        Bindable<JeapCryptoVaultConfigProperties> propertiesBindable = Bindable.ofInstance(properties);
        Binder.get(environment).bind(JeapCryptoVaultConfigProperties.PROPERTY_PREFIX, propertiesBindable);
        properties.postProcessConfiguration(environment);

        properties.getKeys().forEach((keyId, keyConfigProperties) ->
                registerBeans(registry, keyId, keyConfigProperties.getKeyReference(), new JeapCryptoCompactDataFormat(), VAULT_KEY_MANAGEMENT_SERVICE));
    }
}
