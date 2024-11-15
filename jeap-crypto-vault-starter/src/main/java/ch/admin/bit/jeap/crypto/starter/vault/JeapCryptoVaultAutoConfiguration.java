package ch.admin.bit.jeap.crypto.starter.vault;

import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import ch.admin.bit.jeap.crypto.internal.core.aes.AesGcmCryptoService;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.CachingKeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.CryptoMetricsService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import ch.admin.bit.jeap.crypto.vault.format.JeapCryptoKeyReferenceDataFormat;
import ch.admin.bit.jeap.crypto.vault.keymanagement.VaultKeyManagementService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

@AutoConfiguration
@EnableConfigurationProperties(JeapCryptoVaultConfigProperties.class)
@ConditionalOnProperty(value = "spring.cloud.vault.enabled", matchIfMissing = true)
public class JeapCryptoVaultAutoConfiguration {

    @Bean
    public static BeanDefinitionRegistryPostProcessor vaultBeanDefinitionRegistryPostProcessor() {
        return new JeapCryptoVaultBeanDefinitionRegistryPostProcessor();
    }

    @Qualifier("vault")
    @Bean
    public KeyManagementService vaultKeyManagementService(VaultOperations vaultOperations,
                                                          JeapCryptoVaultConfigProperties jeapCryptoVaultConfigProperties,
                                                          CryptoMetricsService cryptoMetricsService) {
        VaultKeyManagementService keyManagementService = new VaultKeyManagementService(vaultOperations, cryptoMetricsService);
        if ((jeapCryptoVaultConfigProperties.getDecryptionKeyMaxCacheSize() == 0) && (jeapCryptoVaultConfigProperties.getEncryptionKeyMaxCacheSize() == 0)) {
            return keyManagementService;
        } else {
            return new CachingKeyManagementService(keyManagementService, jeapCryptoVaultConfigProperties, cryptoMetricsService);
        }
    }

    @Qualifier("vault")
    @Bean
    public KeyReferenceCryptoService vaultKeyReferenceCryptoService(@Qualifier("vault") KeyManagementService keyManagementService) {
        JeapCryptoDataFormat keyReferenceDataFormat = new JeapCryptoKeyReferenceDataFormat();
        return new AesGcmCryptoService(keyManagementService, keyReferenceDataFormat);
    }

    @Qualifier("vault")
    @Bean
    public KeyIdCryptoService vaultKeyIdCryptoService(JeapCryptoVaultConfigProperties jeapCryptoVaultConfigProperties,
                                                      @Qualifier("vault") KeyReferenceCryptoService keyReferenceCryptoService) {
        return new JeapVaultKeyIdCryptoService(jeapCryptoVaultConfigProperties, keyReferenceCryptoService);
    }
}
