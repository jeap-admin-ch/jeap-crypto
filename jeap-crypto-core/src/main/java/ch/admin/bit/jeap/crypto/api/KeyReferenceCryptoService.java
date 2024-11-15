package ch.admin.bit.jeap.crypto.api;

public interface KeyReferenceCryptoService {

    /**
     * Encrypts plaintext to a jEAP crypto container, using a data key for encryption of the data.
     *
     * @param plaintext            Plaintext bytes to be encrypted
     * @param wrappingKeyReference Reference to a key managed by a {@link ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService},
     *                             which is used for generating data keys
     * @return Encrypted data container, formatted according to a {@link ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat}
     * @throws CryptoException If encryption fails for any reason (empty plaintext, bad configuration, unable to get data key, ...)
     */
    byte[] encrypt(byte[] plaintext, KeyReference wrappingKeyReference);

    /**
     * Decrypts a jEAP crypto container back to the plaintext bytes. The crypto container must contain a reference
     * to the data key's wrapping key, i.e. key name/location.
     *
     * @param ciphertextCryptoContainer Encrypted data container, formatted according to a {@link ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat}
     * @return Decrypted plaintext bytes
     * @throws CryptoException If decryption fails for any reason (bad configuration, unable to decrypt data key, ...)
     */
    byte[] decrypt(byte[] ciphertextCryptoContainer);

    boolean canDecrypt(byte[] ciphertext);
}
