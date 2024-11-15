package ch.admin.bit.jeap.crypto.vault.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.CryptoMetricsService;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.vault.core.VaultOperations;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles({"jeap-vault"})
@Testcontainers
@Slf4j
class VaultKeyManagementIT {

    private static final KeyReference KEY_REFERENCE =
            VaultKeyLocation.asKeyReference("transit/jeap", "testapp-encryption-key");

    @Autowired
    private VaultOperations vaultOperations;

    @MockBean
    private CryptoMetricsService cryptoMetricsService;

    @Container
    static public VaultTestContainer vaultContainer = new VaultTestContainer();

    private VaultKeyManagementService vaultKeyManagementService;

    @BeforeEach
    public void beforeEach() {
        vaultKeyManagementService = new VaultKeyManagementService(vaultOperations, cryptoMetricsService);
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("vault.testcontainer.exposed-port", () -> vaultContainer.getMappedPort(8200));
    }

    @BeforeAll
    static void configureVault() throws IOException, InterruptedException {
        ExecResult execResult = vaultContainer.execInContainer("/vault-test-config.sh");
        log.info("Test config stdout: {}", execResult.getStdout());
        log.info("Test config stderr: {}", execResult.getStderr());
        assertThat(execResult.getExitCode())
                .describedAs("Vault config was successful")
                .isZero();
    }

    @Test
    void getDataKey() {
        DataKeyPair dataKeyPair = vaultKeyManagementService.getDataKey(KEY_REFERENCE);
        VaultEncryptedDataKey encryptedDataKey = (VaultEncryptedDataKey) dataKeyPair.encryptedDataKey();
        assertEquals(1, encryptedDataKey.wrappingKeyVersion());
        assertThat(dataKeyPair.encryptedDataKey().ciphertext())
                .isNotNull()
                .hasSize(60);
        assertThat(dataKeyPair.dataKey().plaintextDataKey())
                .isNotNull()
                .hasSize(32);

        verify(cryptoMetricsService).countKeyUsedForEncryption(eq(KEY_REFERENCE), any());
    }

    @Test
    void decryptDataKey() {
        DataKeyPair dataKeyPair = vaultKeyManagementService.getDataKey(KEY_REFERENCE);

        byte[] decryptedDataKey = vaultKeyManagementService.decryptDataKey(KEY_REFERENCE, dataKeyPair.encryptedDataKey());

        assertThat(dataKeyPair.dataKey().plaintextDataKey())
                .isEqualTo(decryptedDataKey);

        verify(cryptoMetricsService).countKeyUsedForDecryption(KEY_REFERENCE);

    }
}
