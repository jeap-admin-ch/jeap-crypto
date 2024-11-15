package ch.admin.bit.jeap.crypto.internal.core.aes;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.CachingKeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementCachingConfigProperties;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.NoMetricsService;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AesGcmCryptoServiceTest {

    private final KeyManagementService keyManagementService = new KeyManagementServiceStub();
    private final JeapCryptoDataFormat dataFormat = new JeapCryptoDataFormatStub();
    private final KeyReference keyReference = new KeyReference("fakeKey");
    private final AesGcmCryptoService cryptoService = new AesGcmCryptoService(keyManagementService, dataFormat);

    @Test
    void encryptDecryptRoundtrip_expectedEncryptedTextToBeDecrypted() {
        String plaintextMessage = "Hello, Bob";

        byte[] plaintext = bytes(plaintextMessage);
        byte[] ciphertextContainer = cryptoService.encrypt(plaintext, keyReference);
        JeapCryptoContainer encrypted = dataFormat.parse(ciphertextContainer);

        assertThat(encrypted.ciphertext())
                .describedAs("Ciphertext length expected to equal plaintext length plus 16 bytes auth tag")
                .hasSize(plaintext.length + 16);
        assertThat(encrypted.nonce())
                .describedAs("Nonce length expected to be 12 bytes")
                .hasSize(12);

        byte[] decryptedPlaintext = cryptoService.decrypt(ciphertextContainer);
        assertThat(decryptedPlaintext)
                .isEqualTo(plaintext);
    }

    @Test
    void encrypt_expectNoncesToBeDifferent() {
        String plaintextMessage = "Hello, Bob";
        byte[] plaintext = bytes(plaintextMessage);
        JeapCryptoContainer encrypted1 = dataFormat.parse(cryptoService.encrypt(plaintext, keyReference));
        JeapCryptoContainer encrypted2 = dataFormat.parse(cryptoService.encrypt(plaintext, keyReference));
        long nonce1 = ByteBuffer.wrap(encrypted1.nonce(), 4, 8).getLong();
        long nonce2 = ByteBuffer.wrap(encrypted2.nonce(), 4, 8).getLong();

        assertThat(nonce1).isNotEqualTo(nonce2);
    }

    @Test
    void encrypt_expectNoncesOfCachedKeysToIncrease() {
        KeyManagementCachingConfigProperties cacheProps = getKeyManagementCachingConfigProperties();
        CachingKeyManagementService cachingKeyManagementService = new CachingKeyManagementService(keyManagementService, cacheProps, new NoMetricsService());
        AesGcmCryptoService cachingCryptoService = new AesGcmCryptoService(cachingKeyManagementService, dataFormat);

        String plaintextMessage = "Hello, Bob";
        byte[] plaintext = bytes(plaintextMessage);
        JeapCryptoContainer encrypted1 = dataFormat.parse(cachingCryptoService.encrypt(plaintext, keyReference));
        JeapCryptoContainer encrypted2 = dataFormat.parse(cachingCryptoService.encrypt(plaintext, keyReference));
        JeapCryptoContainer encrypted3 = dataFormat.parse(cachingCryptoService.encrypt(plaintext, keyReference));
        long nonce1 = ByteBuffer.wrap(encrypted1.nonce(), 4, 8).getLong();
        long nonce2 = ByteBuffer.wrap(encrypted2.nonce(), 4, 8).getLong();
        long nonce3 = ByteBuffer.wrap(encrypted3.nonce(), 4, 8).getLong();

        assertThat(nonce2 - nonce1)
                .describedAs("Nonce must use an incrementing counter between invocations")
                .isOne();
        assertThat(nonce3 - nonce2)
                .describedAs("Nonce must use an incrementing counter between invocations")
                .isOne();
    }

    private KeyManagementCachingConfigProperties getKeyManagementCachingConfigProperties() {
        KeyManagementCachingConfigProperties props = mock(KeyManagementCachingConfigProperties.class);
        when(props.getDecryptionKeyMaxCacheSize()).thenReturn(100L);
        when(props.getEncryptionKeyMaxCacheSize()).thenReturn(100L);
        when(props.getDecryptionKeyCacheExpiryDuration(any())).thenReturn(Duration.ofHours(1));
        when(props.getEncryptionKeyCacheExpiryDuration(any())).thenReturn(Duration.ofHours(1));
        return props;
    }

    @Test
    void encrypt_expectExceptionOnEmptyPlaintext() {
        byte[] emptyPlaintext = new byte[0];

        assertThatThrownBy(() -> cryptoService.encrypt(emptyPlaintext, keyReference))
                .isInstanceOf(CryptoException.class);
    }

    @Test
    void decryptBadData_expectFailureOnBadNonce() {
        JeapCryptoContainer encrypted = encryptHelloBob();
        JeapCryptoContainer badNonceContainer =
                new JeapCryptoContainer(encrypted.encryptedDataKey(), bytes("badnonce1234"), encrypted.ciphertext());
        byte[] badNonceBytes = dataFormat.format(badNonceContainer);

        assertThatThrownBy(() -> cryptoService.decrypt(badNonceBytes))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    void decryptBadData_expectFailureOnBadCiphertext() {
        JeapCryptoContainer encrypted = encryptHelloBob();
        JeapCryptoContainer badCiphertextContainer =
                new JeapCryptoContainer(encrypted.encryptedDataKey(), encrypted.nonce(), bytes("badciphertext"));
        byte[] badCiphertextBytes = dataFormat.format(badCiphertextContainer);

        assertThatThrownBy(() -> cryptoService.decrypt(badCiphertextBytes))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    void decryptBadData_expectFailureOnBadDataKey() {
        JeapCryptoContainer encrypted = encryptHelloBob();
        byte[] badKey = new byte[60];
        new SecureRandom().nextBytes(badKey);
        EncryptedDataKey badDataKey = new EncryptedDataKeyStub(badKey);
        JeapCryptoContainer badDatakeyContainer =
                new JeapCryptoContainer(badDataKey, encrypted.nonce(), encrypted.ciphertext());
        byte[] badDatakeyBytes = dataFormat.format(badDatakeyContainer);

        assertThatThrownBy(() -> cryptoService.decrypt(badDatakeyBytes))
                .isInstanceOf(CryptoException.class);
    }

    private JeapCryptoContainer encryptHelloBob() {
        String plaintextMessage = "Hello, Bob";
        byte[] ciphertextContainer = cryptoService.encrypt(bytes(plaintextMessage), keyReference);
        return dataFormat.parse(ciphertextContainer);
    }

    private static byte[] bytes(String str) {
        return str.getBytes(UTF_8);
    }
}
