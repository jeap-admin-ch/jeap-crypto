package ch.admin.bit.jeap.crypto.test;

import io.floci.testcontainers.FlociContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@Import(AbstractCryptoIntegrationTestBase.TestConfig.class)
@Slf4j
abstract class AbstractCryptoIntegrationTestBase {

    static String testKeyArn;
    static String secondTestKeyArn;

    @Container
    static public FlociContainer floci = createFlociContainer();

    @Container
    static public VaultTestContainer vault = createVaultContainer();

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        AwsCredentialsProvider awsCredentialsProvider() {
            // Provide a credentials provider for the floci AWS emulator for tests
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey()));
        }
    }

    @SuppressWarnings("resource")
    private static FlociContainer createFlociContainer() {
        return new FlociContainer(DockerImageName.parse("floci/floci:1.5.31")
                .asCompatibleSubstituteFor("floci/floci"));
    }

    @BeforeAll
    static void createTestKeys() throws Exception {
        testKeyArn = createTestKey();
        secondTestKeyArn = createTestKey();
        prepareVaultForTest();
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("jeap.crypto.awskms.region", () -> floci.getRegion());
        registry.add("jeap.crypto.awskms.endpoint", () -> floci.getEndpoint());
        registry.add("test-key-arn", () -> testKeyArn);
        registry.add("second-test-key-arn", () -> secondTestKeyArn);
        registry.add("jeap.vault.url", () -> "http://%s:%d".formatted(vault.getHost(), vault.getMappedPort(8200)));
    }

    private static String createTestKey() {
        Region region = Region.of(floci.getRegion());
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey()));
        try (KmsClient kmsClient = KmsClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(floci.getEndpoint()))
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
