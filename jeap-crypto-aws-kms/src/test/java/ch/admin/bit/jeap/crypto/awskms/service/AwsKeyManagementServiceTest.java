package ch.admin.bit.jeap.crypto.awskms.service;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.awskms.client.AwsKmsClient;
import ch.admin.bit.jeap.crypto.awskms.key.EscrowKeyConfig;
import ch.admin.bit.jeap.crypto.internal.core.escrow.AsymmetricEscrowEncryptionService;
import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.NoMetricsService;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.urlconnection.ProxyConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.CreateKeyRequest;
import software.amazon.awssdk.services.kms.model.CreateKeyResponse;
import software.amazon.awssdk.services.kms.model.KeySpec;

import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class AwsKeyManagementServiceTest {

    @Container
    private static final LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.1")
                    .asCompatibleSubstituteFor("localstack/localstack"))
                    .withEnv("DISABLE_EVENTS", "1") // Disable localstack features that require an internet connection
                    .withEnv("SKIP_INFRA_DOWNLOADS", "1")
                    .withEnv("SKIP_SSL_CERT_DOWNLOAD", "1");
    private AwsKeyManagementService awsKeyManagementService;
    private String keyArn;

    @Test
    void getDataKey() {
        KeyReference wrappingKeyReference = new KeyReference(keyArn);

        DataKeyPair dataKeyPair = awsKeyManagementService.getDataKey(wrappingKeyReference);

        assertThat(dataKeyPair.dataKey().plaintextDataKey())
                .describedAs("Key has 256 bits")
                .hasSize(256 / 8);

        byte[] decryptedDataKey = awsKeyManagementService.decryptDataKey(wrappingKeyReference, dataKeyPair.encryptedDataKey());

        assertThat(decryptedDataKey)
                .describedAs("Can decrypt encrypted data key")
                .isEqualTo(dataKeyPair.dataKey().plaintextDataKey());
    }

    @BeforeEach
    void prepareKms() throws Exception {
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey()));
        Region region = Region.of(localStack.getRegion());
        SdkHttpClient sdkHttpClient = UrlConnectionHttpClient.builder()
                .proxyConfiguration(ProxyConfiguration.builder()
                        .useSystemPropertyValues(false)
                        .useEnvironmentVariablesValues(false)
                        .build()).build();
        AwsKmsClient kmsClient = new AwsKmsClient(credentialsProvider, region, localStack.getEndpointOverride(Service.KMS), sdkHttpClient);
        keyArn = createTestKey(credentialsProvider, region);
        Map<KeyReference, EscrowKeyConfig> escrowKeys = Map.of(
                new KeyReference(keyArn), new EscrowKeyConfig(EscrowKeyType.RSA_4096, generatePublicKey()));
        awsKeyManagementService = new AwsKeyManagementService(kmsClient,
                new EscrowKeyConfig(EscrowKeyType.RSA_4096, generatePublicKey()),
                escrowKeys,
                new AsymmetricEscrowEncryptionService(),
                new NoMetricsService());
    }

    private PublicKey generatePublicKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(4096);
        return generator.generateKeyPair().getPublic();
    }

    private String createTestKey(AwsCredentialsProvider credentialsProvider, Region region) {
        try (KmsClient kmsClient = KmsClient.builder()
                .region(region)
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .proxyConfiguration(ProxyConfiguration.builder()
                                .useSystemPropertyValues(false)
                                .useEnvironmentVariablesValues(false)
                                .build()))
                .credentialsProvider(credentialsProvider)
                .endpointOverride(localStack.getEndpointOverride(Service.KMS))
                .build()) {

            CreateKeyResponse response = kmsClient.createKey(CreateKeyRequest.builder()
                    .keySpec(KeySpec.SYMMETRIC_DEFAULT)
                    .build());

            return response.keyMetadata().arn();
        }
    }
}
