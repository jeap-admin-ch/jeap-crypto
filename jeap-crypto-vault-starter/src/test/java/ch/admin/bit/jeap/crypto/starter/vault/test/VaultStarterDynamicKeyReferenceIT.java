package ch.admin.bit.jeap.crypto.starter.vault.test;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import ch.admin.bit.jeap.crypto.vault.format.JeapCryptoKeyReferenceDataFormat;
import ch.admin.bit.jeap.crypto.vault.keymanagement.VaultKeyLocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("jeap-vault")
class VaultStarterDynamicKeyReferenceIT extends AbstractVaultIntegrationTestBase {

    @Autowired
    private KeyReferenceCryptoService keyReferenceCryptoService;

    @Test
    void testEncryptionRoundtripAndSpringConfiguration() {
        byte[] dbHelloBytes = "Hello, DB!".getBytes(UTF_8);
        byte[] s3HelloBytes = "Hello, S3!".getBytes(UTF_8);
        KeyReference dbKkeyReference = VaultKeyLocation.asKeyReference("transit/jeap", "testapp-database-key");
        KeyReference s3KeyReference = VaultKeyLocation.asKeyReference("transit/otherapp", "otherapp-s3-key");

        byte[] encryptedForDb = keyReferenceCryptoService.encrypt(dbHelloBytes, dbKkeyReference);
        byte[] decryptedPlaintextFromDb = keyReferenceCryptoService.decrypt(encryptedForDb);
        byte[] encryptedForS3 = keyReferenceCryptoService.encrypt(s3HelloBytes, s3KeyReference);
        byte[] decryptedPlaintextFromS3 = keyReferenceCryptoService.decrypt(encryptedForS3);

        assertThat(decryptedPlaintextFromDb)
                .isEqualTo(dbHelloBytes);
        assertThat(decryptedPlaintextFromS3)
                .isEqualTo(s3HelloBytes);
        assertThat(keyLocation(encryptedForDb))
                .isEqualTo("v:transit/jeap:testapp-database-key");
        assertThat(keyLocation(encryptedForS3))
                .isEqualTo("v:transit/otherapp:otherapp-s3-key");
    }

    private static String keyLocation(byte[] encryptedContainer) {
        return new JeapCryptoKeyReferenceDataFormat()
                .parse(encryptedContainer)
                .encryptedDataKey()
                .wrappingKeyReference()
                .keyLocation();
    }
}
