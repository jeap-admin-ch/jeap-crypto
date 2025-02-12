package ch.admin.bit.jeap.crypto.internal.core.escrow;

import ch.admin.bit.jeap.crypto.internal.core.jca.CryptoAdapter;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class AsymmetricEscrowEncryptionServiceTest {

    private KeyPair pair;
    private String fakeKeyString;
    private byte[] fakeKeyBytes;

    @BeforeEach
    void prepare() throws NoSuchAlgorithmException {
        pair = generateRsa4096KeyPair();
        fakeKeyString = "fakekey-fakekey-fakekey-fakekey.";
        fakeKeyBytes = fakeKeyString.getBytes(UTF_8);
        assertThat(fakeKeyBytes)
                .hasSize(256 / 8); // AES-256 / 8 bits per byte
    }

    @Test
    void encryptEscrowDataKey() throws Exception {
        AsymmetricEscrowEncryptionService service = new AsymmetricEscrowEncryptionService();

        EscrowDataKey escrowDataKey =
                service.encryptEscrowDataKey(new DataKey(fakeKeyBytes), EscrowKeyType.RSA_4096, pair.getPublic());

        String decryptedEscrowDataKey = decrypt(escrowDataKey, pair.getPrivate());
        assertThat(decryptedEscrowDataKey)
                .isEqualTo(fakeKeyString);
    }

    @Test
    void encryptEscrowDataKey_none() throws Exception {
        AsymmetricEscrowEncryptionService service = new AsymmetricEscrowEncryptionService();

        EscrowDataKey escrowDataKey =
                service.encryptEscrowDataKey(new DataKey(fakeKeyBytes), EscrowKeyType.NONE, null);

        assertThat(escrowDataKey)
                .isNull();
    }

    private static KeyPair generateRsa4096KeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(4096);
        return generator.generateKeyPair();
    }

    private String decrypt(EscrowDataKey escrowDataKey, PrivateKey privateKey) throws Exception {
        Cipher cipher = CryptoAdapter.createCipher(AsymmetricEscrowEncryptionService.RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        cipher.update(escrowDataKey.ciphertext());
        return new String(cipher.doFinal(), UTF_8);
    }
}
