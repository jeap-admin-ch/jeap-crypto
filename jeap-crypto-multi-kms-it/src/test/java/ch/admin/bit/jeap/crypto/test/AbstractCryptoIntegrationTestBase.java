package ch.admin.bit.jeap.crypto.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.CreateKeyRequest;
import software.amazon.awssdk.services.kms.model.CreateKeyResponse;
import software.amazon.awssdk.services.kms.model.KeySpec;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@Import(AbstractCryptoIntegrationTestBase.TestConfig.class)
@Slf4j
abstract class AbstractCryptoIntegrationTestBase {

    static String testKeyArn;
    static String secondTestKeyArn;

    @Container
    static public LocalStackContainer localStack = createLocalStackContainer();

    @Container
    static public VaultTestContainer vault = createVaultContainer();

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        AwsCredentialsProvider awsCredentialsProvider() {
            // Provide a credentials provider for the local stack AWS simulator for tests
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey()));
        }
    }

    @SuppressWarnings("resource")
    private static LocalStackContainer createLocalStackContainer() {
        return new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.1")
                .asCompatibleSubstituteFor("localstack/localstack"))
                .withEnv("DISABLE_EVENTS", "1") // Disable localstack features that require an internet connection
                .withEnv("SKIP_INFRA_DOWNLOADS", "1")
                .withEnv("SKIP_SSL_CERT_DOWNLOAD", "1");
    }

    @BeforeAll
    static void createTestKeys() throws Exception {
        testKeyArn = createTestKey();
        secondTestKeyArn = createTestKey();
        prepareVaultForTest();
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("jeap.crypto.awskms.region", () -> localStack.getRegion());
        registry.add("jeap.crypto.awskms.endpoint", () -> localStack.getEndpointOverride(LocalStackContainer.Service.KMS));
        registry.add("test-key-arn", () -> testKeyArn);
        registry.add("second-test-key-arn", () -> secondTestKeyArn);
        registry.add("vault.testcontainer.exposed-port", () -> vault.getMappedPort(8200));
    }

    private static String createTestKey() {
        Region region = Region.of(localStack.getRegion());
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey()));
        try (KmsClient kmsClient = KmsClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.KMS))
                .build()) {

            CreateKeyResponse response = kmsClient.createKey(CreateKeyRequest.builder()
                    .keySpec(KeySpec.SYMMETRIC_DEFAULT)
                    .build());

            return response.keyMetadata().arn();
        }
    }

    static VaultTestContainer createVaultContainer() {
        return new VaultTestContainer();
    }

    static void prepareVaultForTest() throws Exception {
        org.testcontainers.containers.Container.ExecResult execResult = vault.execInContainer("/vault-test-config.sh");
        log.info("Test config stdout: {}", execResult.getStdout());
        log.info("Test config stderr: {}", execResult.getStderr());
        assertEquals(0, execResult.getExitCode(), "Vault config was successful");
    }
}
