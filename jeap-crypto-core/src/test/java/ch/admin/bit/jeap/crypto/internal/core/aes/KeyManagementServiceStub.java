package ch.admin.bit.jeap.crypto.internal.core.aes;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;

import java.security.SecureRandom;
import java.util.*;

public class KeyManagementServiceStub implements KeyManagementService {

    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private final Map<String, String> fakeKeys = new HashMap<>();
    private final Set<KeyReference> usedKeys = new HashSet<>();

    @Override
    public DataKeyPair getDataKey(KeyReference wrappingKeyReference) {
        int keyBytes = 32; // AES-256, 256 / 8 = 32
        int nonceBytes = 16; // gcm-96, 96 / 8 =
        int tagBytes = 12; // auth tag, 12 bytes

        byte[] fakePlaintextKey = new byte[keyBytes];
        byte[] fakeEncryptedKey = new byte[keyBytes + nonceBytes + tagBytes];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(fakePlaintextKey);
        secureRandom.nextBytes(fakeEncryptedKey);
        String cachingKey = getCachingKey(wrappingKeyReference, fakeEncryptedKey);
        fakeKeys.put(cachingKey, ENCODER.encodeToString(fakePlaintextKey));
        usedKeys.add(wrappingKeyReference);
        DataKey dataKey = new DataKey(fakePlaintextKey);
        EncryptedDataKeyStub encryptedDataKey = new EncryptedDataKeyStub(fakeEncryptedKey, wrappingKeyReference);
        return new DataKeyPair(dataKey, encryptedDataKey);
    }

    @Override
    public byte[] decryptDataKey(KeyReference wrappingKeyReference, EncryptedDataKey encryptedDataKey) {
        String cachingKey = getCachingKey(wrappingKeyReference, encryptedDataKey.ciphertext());
        String fakePlaintextKey = fakeKeys.computeIfAbsent(cachingKey, key -> {
            throw CryptoException.decryptDataKeyFailed(new RuntimeException(), wrappingKeyReference.keyLocation());
        });
        return DECODER.decode(fakePlaintextKey);
    }

    private static String getCachingKey(KeyReference keyReference, byte[] fakeEncryptedKey) {
        return keyReference + ":" + ENCODER.encodeToString(fakeEncryptedKey);
    }

    public Set<KeyReference> getUsedWrappingKeys() {
        return Set.copyOf(usedKeys);
    }
}
