package ch.admin.bit.jeap.crypto.internal.core.noop;

import ch.admin.bit.jeap.crypto.api.CryptoService;

/**
 * Should only be used to bypass encryption and decryption in dev environments. Hence, plaintext and ciphertext are
 * returned untouched. No encryption or decryption is performed.
 */
public class NoopCryptoService implements CryptoService {

    @Override
    public byte[] encrypt(byte[] plaintext) {
        return plaintext;
    }

    @Override
    public byte[] decrypt(byte[] ciphertextCryptoContainer) {
        return ciphertextCryptoContainer;
    }
}
