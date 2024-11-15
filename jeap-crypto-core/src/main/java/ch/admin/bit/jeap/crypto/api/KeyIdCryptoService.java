package ch.admin.bit.jeap.crypto.api;

import java.util.Set;

public interface KeyIdCryptoService {

    /**
     * Encrypts plaintext to a jEAP crypto container, using the wrapping key identified by the given key id for the encryption of the data.
     *
     * @param plaintext Plaintext bytes to be encrypted
     * @param keyId Identifier for the wrapping key to be used for the encryption
     * @return Encrypted data container, formatted according to a {@link ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat}
     * @throws CryptoException If encryption fails for any reason (empty plaintext, bad configuration, unknown key id, unable to get data key, ...)
     */
    byte[] encrypt(byte[] plaintext, KeyId keyId);

    /**
     * Decrypts a jEAP crypto container back to the plaintext bytes.
     *
     * @param ciphertextCryptoContainer Encrypted data container, formatted according to a {@link ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat}
     * @return Decrypted plaintext bytes
     * @throws CryptoException If decryption fails for any reason (bad configuration, unable to decrypt data key, ...)
     */
    byte[] decrypt(byte[] ciphertextCryptoContainer);

    /**
     * Does this key id crypto service know the given key id and can link it to a wrapping key to be used for encryption?
     * 
     * @param keyId The key id
     * @return <code>true</code> if this service knows the given key id and can link it to a wrapping key to be used for
     * encryption, <code>false</code> otherwise.
     */
    default boolean knows(KeyId keyId) {
        return configuredKeyIds().contains(keyId);
    }

    Set<KeyId> configuredKeyIds();

    boolean canDecrypt(byte[] ciphertext);

}
