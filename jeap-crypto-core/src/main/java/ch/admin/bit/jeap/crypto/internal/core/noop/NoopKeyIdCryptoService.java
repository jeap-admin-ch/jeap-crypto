package ch.admin.bit.jeap.crypto.internal.core.noop;

import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Should only be used to bypass encryption and decryption in dev environments. Hence, plaintext and ciphertext are
 * returned untouched. No encryption or decryption is performed.
 */
public class NoopKeyIdCryptoService implements KeyIdCryptoService {

    private final Set<KeyId> knownKeyIds;

    public NoopKeyIdCryptoService(Set<String> knownKeyIds) {
        this.knownKeyIds = knownKeyIds.stream()
                .map(KeyId::of)
                .collect(toSet());
    }

    @Override
    public byte[] encrypt(byte[] plaintext, KeyId keyId) {
        return plaintext;
    }

    @Override
    public byte[] decrypt(byte[] ciphertextCryptoContainer) {
        return ciphertextCryptoContainer;
    }

    @Override
    public Set<KeyId> configuredKeyIds() {
        return knownKeyIds;
    }

    @Override
    public boolean canDecrypt(byte[] ciphertext) {
        return true;
    }
}
