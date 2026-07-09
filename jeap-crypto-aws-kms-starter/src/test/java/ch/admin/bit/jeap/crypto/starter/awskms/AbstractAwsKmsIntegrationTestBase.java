package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.starter.test.awskms.Application;
import io.floci.testcontainers.FlociContainer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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

import static ch.admin.bit.jeap.crypto.starter.awskms.AbstractAwsKmsIntegrationTestBase.TestConfig;

@SpringBootTest(classes = Application.class)
@Import(TestConfig.class)
@Testcontainers
@Slf4j
abstract class AbstractAwsKmsIntegrationTestBase {

    static String testKeyArn;
    static String secondTestKeyArn;

    @Container
    static public FlociContainer floci = createFlociContainer();

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        AwsCredentialsProvider awsCredentialsProvider() {
            // Provide a credentials provider for the floci AWS emulator for tests
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey()));
        }

        @Bean
        SimpleMeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @SuppressWarnings("resource")
    private static FlociContainer createFlociContainer() {
        return new FlociContainer(DockerImageName.parse("floci/floci:1.5.31")
                .asCompatibleSubstituteFor("floci/floci"));
    }

    @BeforeAll
    static void createTestKeys() {
        testKeyArn = createTestKey();
        secondTestKeyArn = createTestKey();
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("jeap.crypto.awskms.region", () -> floci.getRegion());
        registry.add("jeap.crypto.awskms.endpoint", () -> floci.getEndpoint());
        registry.add("test-key-arn", () -> testKeyArn);
        registry.add("second-test-key-arn", () -> secondTestKeyArn);
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
}
