package ch.admin.bit.jeap.crypto.api;

public interface CryptoService {

    /**
     * Encrypts plaintext to a jEAP crypto container, using a data key for encryption of the data.
     *
     * @param plaintext Plaintext bytes to be encrypted
     * @return Encrypted data container, formatted according to a {@link ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat}
     * @throws CryptoException If encryption fails for any reason (empty plaintext, bad configuration, unable to get data key, ...)
     */
    byte[] encrypt(byte[] plaintext);

    /**
     * Decrypts a jEAP crypto container back to the plaintext bytes.
     *
     * @param ciphertextCryptoContainer Encrypted data container, formatted according to a {@link ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat}
     * @return Decrypted plaintext bytes
     * @throws CryptoException If decryption fails for any reason (bad configuration, unable to decrypt data key, ...)
     */
    byte[] decrypt(byte[] ciphertextCryptoContainer);
}
