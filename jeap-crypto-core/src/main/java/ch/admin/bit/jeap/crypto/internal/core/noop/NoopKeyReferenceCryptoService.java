package ch.admin.bit.jeap.crypto.internal.core.noop;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;

/**
 * Should only be used to bypass encryption and decryption in dev environments. Hence, plaintext and ciphertext are
 * returned untouched. No encryption or decryption is performed.
 */
public class NoopKeyReferenceCryptoService implements KeyReferenceCryptoService {

    @Override
    public byte[] encrypt(byte[] plaintext, KeyReference wrappingKeyReference) {
        return plaintext;
    }

    @Override
    public byte[] decrypt(byte[] ciphertextCryptoContainer) {
        return ciphertextCryptoContainer;
    }

    @Override
    public boolean canDecrypt(byte[] ciphertext) {
        return true;
    }
}
