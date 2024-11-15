package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import ch.admin.bit.jeap.crypto.awskms.client.AwsKmsClient;
import ch.admin.bit.jeap.crypto.awskms.key.AwsKmsEncryptedDataKeyFormat;
import ch.admin.bit.jeap.crypto.awskms.key.EscrowKeyConfig;
import ch.admin.bit.jeap.crypto.awskms.service.AwsKeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.aes.AesGcmCryptoService;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoMultiKeyDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.escrow.AsymmetricEscrowEncryptionService;
import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowEncryptionService;
import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.CachingKeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.CryptoMetricsService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import ch.admin.bit.jeap.crypto.starter.awskms.JeapCryptoAwsKmsConfigProperties.AwsKmsKeyConfigProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AutoConfiguration
@EnableConfigurationProperties(JeapCryptoAwsKmsConfigProperties.class)
@ConditionalOnExpression("!${jeap.crypto.disabledForTestEnv:false} and ${jeap.crypto.awskms.enabled:true}")
public class JeapCryptoAwsKmsAutoConfiguration {

    @Bean
    public static BeanDefinitionRegistryPostProcessor awsKmsBeanDefinitionRegistryPostProcessor() {
        return new JeapCryptoAwsKmsBeanDefinitionRegistryPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    @ConditionalOnMissingBean
    public AwsKmsClient awsKmsClient(JeapCryptoAwsKmsConfigProperties jeapCryptoAwsKmsConfigProperties,
                                     AwsCredentialsProvider awsCredentialsProvider) {
        Region region = jeapCryptoAwsKmsConfigProperties.getRegion();
        URI endpointUrlOverride = jeapCryptoAwsKmsConfigProperties.getEndpoint();
        if (endpointUrlOverride != null) {
            return new AwsKmsClient(awsCredentialsProvider, region, endpointUrlOverride);
        }
        return new AwsKmsClient(awsCredentialsProvider, region);
    }

    @Qualifier("awsKms")
    @Bean
    public KeyManagementService awsKmsKeyManagementService(JeapCryptoAwsKmsConfigProperties jeapCryptoAwsKmsConfigProperties,
                                                           AwsKmsClient awsKmsClient,
                                                           CryptoMetricsService cryptoMetricsService) {
        EscrowEncryptionService escrowEncryptionService = new AsymmetricEscrowEncryptionService();
        Map<KeyReference, EscrowKeyConfig> escrowKeys = getEscrowKeyConfigMap(jeapCryptoAwsKmsConfigProperties);
        EscrowKeyConfig defaultEscrowKey = getDefaultEscrowKey(jeapCryptoAwsKmsConfigProperties);

        AwsKeyManagementService awsKeyManagementService =
                new AwsKeyManagementService(awsKmsClient, defaultEscrowKey, escrowKeys, escrowEncryptionService, cryptoMetricsService);
        if ((jeapCryptoAwsKmsConfigProperties.getDecryptionKeyMaxCacheSize() == 0) && (jeapCryptoAwsKmsConfigProperties.getEncryptionKeyMaxCacheSize() == 0)) {
            return awsKeyManagementService;
        } else {
            return new CachingKeyManagementService(awsKeyManagementService, jeapCryptoAwsKmsConfigProperties, cryptoMetricsService);
        }
    }

    private EscrowKeyConfig getDefaultEscrowKey(JeapCryptoAwsKmsConfigProperties configProperties) {
        return Optional.ofNullable(configProperties.getDefaultEscrowKey())
                .map(escrowKey -> new EscrowKeyConfig(escrowKey.getKeyType(), escrowKey.getParsedPublicKey()))
                .orElse(null);
    }

    private Map<KeyReference, EscrowKeyConfig> getEscrowKeyConfigMap(JeapCryptoAwsKmsConfigProperties jeapCryptoAwsKmsConfigProperties) {
        return jeapCryptoAwsKmsConfigProperties
                .getKeys().values()
                .stream()
                .filter(e -> e.getEscrowKey() != null)
                .collect(Collectors.toMap(AwsKmsKeyConfigProperties::getKeyReference, this::toEscrowKeyConfig));
    }

    private EscrowKeyConfig toEscrowKeyConfig(AwsKmsKeyConfigProperties props) {
        EscrowKeyType keyType = props.getEscrowKey().getKeyType();
        PublicKey publicKey = props.getEscrowKey().getParsedPublicKey();
        return new EscrowKeyConfig(keyType, publicKey);
    }

    @Qualifier("awsKms")
    @Bean
    public KeyReferenceCryptoService awsKmsKeyReferenceCryptoService(@Qualifier("awsKms") KeyManagementService keyManagementService) {
        JeapCryptoDataFormat keyReferenceDataFormat = new JeapCryptoMultiKeyDataFormat(new AwsKmsEncryptedDataKeyFormat());
        return new AesGcmCryptoService(keyManagementService, keyReferenceDataFormat);
    }

    @Qualifier("awsKms")
    @Bean
    public KeyIdCryptoService awsKmsKeyIdCryptoService(JeapCryptoAwsKmsConfigProperties jeapCryptoAwsKmsConfigProperties,
                                                       @Qualifier("awsKms") KeyReferenceCryptoService keyReferenceCryptoService) {
        return new JeapAwsKmsKeyIdCryptoService(jeapCryptoAwsKmsConfigProperties, keyReferenceCryptoService);
    }
}
