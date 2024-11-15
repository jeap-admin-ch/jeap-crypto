package ch.admin.bit.jeap.crypto.vault.keymanagement;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.KeyReference;

public record VaultKeyLocation(String secretEnginePath, String keyName) {

    private static final String VAULT_KEY_LOCATION_PREFIX = "v:";

    static VaultKeyLocation fromKeyReference(KeyReference keyReference) {
        String keyLocation = keyReference.keyLocation();
        if (!keyLocation.startsWith(VAULT_KEY_LOCATION_PREFIX)) {
            throw CryptoException.badKeyReferenceType(keyLocation, "vault");
        }
        int delimiter = keyLocation.indexOf(":", VAULT_KEY_LOCATION_PREFIX.length());
        String keyPath = keyLocation.substring(VAULT_KEY_LOCATION_PREFIX.length(), delimiter);
        String keyName = keyLocation.substring(delimiter + 1);
        return new VaultKeyLocation(keyPath, keyName);
    }

    public static KeyReference asKeyReference(String secretEnginePath, String keyName) {
        String keyLocation = VAULT_KEY_LOCATION_PREFIX + secretEnginePath + ":" + keyName;
        return new KeyReference(keyLocation);
    }
}
