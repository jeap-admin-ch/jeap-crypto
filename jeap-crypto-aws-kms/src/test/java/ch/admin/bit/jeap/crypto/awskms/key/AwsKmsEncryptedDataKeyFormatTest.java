package ch.admin.bit.jeap.crypto.awskms.key;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

class AwsKmsEncryptedDataKeyFormatTest {

    @Test
    void format() {
        AwsKmsEncryptedDataKeyFormat format = new AwsKmsEncryptedDataKeyFormat();
        SecureRandom secureRandom = new SecureRandom();
        byte[] ciphertext = new byte[256 / 8];
        secureRandom.nextBytes(ciphertext);
        AwsKmsEncryptedDataKey encryptedDataKey = new AwsKmsEncryptedDataKey(ciphertext, null, "test-key-id");

        byte[] serializedDataKeyFrame = format.format(encryptedDataKey);
        AwsKmsEncryptedDataKey parsedKey = format.parse(ByteBuffer.wrap(serializedDataKeyFrame));

        assertThat(parsedKey)
                .isEqualTo(encryptedDataKey);
    }
}
