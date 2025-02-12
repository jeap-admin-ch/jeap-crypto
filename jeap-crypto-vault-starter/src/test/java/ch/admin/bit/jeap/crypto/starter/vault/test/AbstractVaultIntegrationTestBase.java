package ch.admin.bit.jeap.crypto.starter.vault.test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static ch.admin.bit.jeap.crypto.starter.vault.test.AbstractVaultIntegrationTestBase.TestConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@Slf4j
@Import(TestConfig.class)
abstract class AbstractVaultIntegrationTestBase {

    @TestConfiguration
    static
    class TestConfig {
        @Bean
        SimpleMeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @Container
    static VaultTestContainer vault = new VaultTestContainer();

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("jeap.vault.url", () -> "http://%s:%d".formatted(vault.getHost(), vault.getMappedPort(8200)));
    }

    @BeforeAll
    static void configureVault() throws Exception {
        prepareVaultForTest();
    }

    static void prepareVaultForTest() throws Exception {
        org.testcontainers.containers.Container.ExecResult execResult = vault.execInContainer("/vault-test-config.sh");
        log.info("Test config stdout: {}", execResult.getStdout());
        log.info("Test config stderr: {}", execResult.getStderr());
        assertEquals(0, execResult.getExitCode(), "Vault config was successful");
    }
}
