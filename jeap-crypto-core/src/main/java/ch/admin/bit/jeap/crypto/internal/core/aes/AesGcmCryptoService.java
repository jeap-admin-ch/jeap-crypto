package ch.admin.bit.jeap.crypto.internal.core.aes;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.jca.CryptoAdapter;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public class AesGcmCryptoService implements KeyReferenceCryptoService {

    // 256bit key (AES-256)
    private static final int KEY_SIZE_BYTES = 256 / 8;
    // 16-byte auth tag
    private static final int TAG_LENGTH_BYTES = 16;
    // 12-byte nonce / initialization vector
    private static final int NONCE_LENGTH_BYTES = 12;
    // Using AES-256 with GCM-96
    private static final String AES = "AES";
    private static final String CRYPTO_ALGO = AES + "/GCM/NoPadding";

    private final KeyManagementService keyManagementService;
    private final JeapCryptoDataFormat dataFormat;

    public AesGcmCryptoService(KeyManagementService keyManagementService,
                               JeapCryptoDataFormat dataFormat) {
        this.keyManagementService = keyManagementService;
        this.dataFormat = dataFormat;
    }

    @Override
    public byte[] encrypt(byte[] plaintext, KeyReference wrappingKeyReference) {
        if (plaintext.length == 0) {
            throw CryptoException.emptyPlaintext();
        }

        JeapCryptoContainer container = encryptToContainer(plaintext, wrappingKeyReference);
        return dataFormat.format(container);
    }

    private JeapCryptoContainer encryptToContainer(byte[] data, KeyReference keyReference) {
        DataKeyPair dataKeyPair = keyManagementService.getDataKey(keyReference);
        SecretKey secretKey = createSecretKeyFromPlaintextKey(dataKeyPair.dataKey());

        byte[] nonce = dataKeyPair.dataKey().generateNonce(NONCE_LENGTH_BYTES);
        byte[] ciphertext = encrypt(data, secretKey, nonce);
        return new JeapCryptoContainer(dataKeyPair.encryptedDataKey(), nonce, ciphertext);
    }

    @Override
    public byte[] decrypt(byte[] cryptoContainerBytes) {
        JeapCryptoContainer cryptoContainer = dataFormat.parse(cryptoContainerBytes);
        return decryptFromContainer(cryptoContainer);
    }

    private byte[] decryptFromContainer(JeapCryptoContainer cryptoContainer) {
        byte[] nonce = cryptoContainer.nonce();

        KeyReference keyReference = getKeyReferenceForDecryption(cryptoContainer.encryptedDataKey());

        SecretKey secretKey = createSecretKeyFromEncryptedDataKey(keyReference, cryptoContainer.encryptedDataKey());
        return decrypt(secretKey, nonce, cryptoContainer.ciphertext());
    }

    protected KeyReference getKeyReferenceForDecryption(EncryptedDataKey encryptedDataKey) {
        return encryptedDataKey.requireWrappingKeyReference();
    }

    private SecretKey createSecretKeyFromPlaintextKey(DataKey dataKey) {
        byte[] plaintextDataKey = dataKey.plaintextDataKey();
        return createSecretKey(plaintextDataKey);
    }

    private SecretKey createSecretKeyFromEncryptedDataKey(KeyReference keyReference, EncryptedDataKey dataKey) {
        byte[] plaintextDataKey = keyManagementService.decryptDataKey(keyReference, dataKey);
        return createSecretKey(plaintextDataKey);
    }

    private static SecretKeySpec createSecretKey(byte[] plaintextDataKey) {
        // AES-256 --> 256bit Key, /8 --> in bytes
        if (plaintextDataKey == null) {
            throw CryptoException.nullKey();
        }
        if (plaintextDataKey.length != KEY_SIZE_BYTES) {
            throw CryptoException.badKeySize(plaintextDataKey.length, KEY_SIZE_BYTES);
        }
        return new SecretKeySpec(plaintextDataKey, AES);
    }

    private static byte[] encrypt(byte[] data, SecretKey dataKey, byte[] nonce) {
        try {
            GCMParameterSpec params = getGcmParameterSpec(nonce);
            Cipher cipher = CryptoAdapter.createCipher(CRYPTO_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, dataKey, params);
            return cipher.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw CryptoException.encryptionFailed(e);
        }
    }

    private static GCMParameterSpec getGcmParameterSpec(byte[] nonce) {
        // Tag length parameter is in bits, hence '*8'
        return new GCMParameterSpec(TAG_LENGTH_BYTES * 8, nonce);
    }

    private byte[] decrypt(SecretKey key, byte[] nonce, byte[] cipherText) {
        try {
            Cipher cipher = CryptoAdapter.createCipher(CRYPTO_ALGO);
            GCMParameterSpec params = getGcmParameterSpec(nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, params);
            return cipher.doFinal(cipherText);
        } catch (GeneralSecurityException e) {
            throw CryptoException.decryptionFailed(e);
        }
    }

    @Override
    public boolean canDecrypt(byte[] ciphertext) {
        return dataFormat.canParse(ciphertext);
    }
}
