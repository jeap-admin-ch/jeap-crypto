package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.awskms.key.AwsKmsEncryptedDataKeyFormat;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoMultiKeyDataFormat;
import ch.admin.bit.jeap.crypto.spring.AbstractCryptoBeanDefinitionRegistryPostProcessor;
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
class JeapCryptoAwsKmsBeanDefinitionRegistryPostProcessor extends AbstractCryptoBeanDefinitionRegistryPostProcessor {

    private static final String AWS_KMS_KEY_MANAGEMENT_SERVICE = "awsKmsKeyManagementService";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        JeapCryptoAwsKmsConfigProperties properties = new JeapCryptoAwsKmsConfigProperties();
        Bindable<JeapCryptoAwsKmsConfigProperties> propertiesBindable = Bindable.ofInstance(properties);
        Binder.get(environment).bind(JeapCryptoAwsKmsConfigProperties.PROPERTY_PREFIX, propertiesBindable);
        properties.setEnvironment(environment);
        properties.postProcessConfiguration();

        JeapCryptoMultiKeyDataFormat dataFormat = new JeapCryptoMultiKeyDataFormat(new AwsKmsEncryptedDataKeyFormat());
        properties.getKeys().forEach((keyId, keyConfigProperties) ->
                registerBeans(registry, keyId, keyConfigProperties.getKeyReference(), dataFormat, AWS_KMS_KEY_MANAGEMENT_SERVICE));
    }
}
