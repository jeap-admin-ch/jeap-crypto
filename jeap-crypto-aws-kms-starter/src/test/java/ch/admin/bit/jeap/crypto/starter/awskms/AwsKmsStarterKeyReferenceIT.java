package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import ch.admin.bit.jeap.crypto.awskms.key.AwsKmsEncryptedDataKeyFormat;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoMultiKeyDataFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("aws-test")
class AwsKmsStarterKeyReferenceIT extends AbstractAwsKmsIntegrationTestBase {

    @Autowired
    private KeyReferenceCryptoService keyReferenceCryptoService;

    @Test
    void testEncryptionRoundtripAndSpringConfiguration() {
        byte[] dbHelloBytes = "Hello, DB!".getBytes(UTF_8);
        byte[] s3HelloBytes = "Hello, S3!".getBytes(UTF_8);
        KeyReference dbKkeyReference = new KeyReference(testKeyArn);
        KeyReference s3KeyReference = new KeyReference(secondTestKeyArn);

        byte[] encryptedForDb = keyReferenceCryptoService.encrypt(dbHelloBytes, dbKkeyReference);
        byte[] decryptedPlaintextFromDb = keyReferenceCryptoService.decrypt(encryptedForDb);
        byte[] encryptedForS3 = keyReferenceCryptoService.encrypt(s3HelloBytes, s3KeyReference);
        byte[] decryptedPlaintextFromS3 = keyReferenceCryptoService.decrypt(encryptedForS3);

        assertThat(decryptedPlaintextFromDb)
                .isEqualTo(dbHelloBytes);
        assertThat(decryptedPlaintextFromS3)
                .isEqualTo(s3HelloBytes);
        assertThat(keyLocation(encryptedForDb))
                .isEqualTo(testKeyArn);
        assertThat(keyLocation(encryptedForS3))
                .isEqualTo(secondTestKeyArn);
    }

    private static String keyLocation(byte[] encryptedContainer) {
        return new JeapCryptoMultiKeyDataFormat(new AwsKmsEncryptedDataKeyFormat())
                .parse(encryptedContainer)
                .encryptedDataKey()
                .wrappingKeyReference()
                .keyLocation();
    }
}
