package ch.admin.bit.jeap.crypto.api;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class CryptoServiceProvider {

    private final List<KeyIdCryptoService> keyIdCryptoServices;

    public CryptoServiceProvider(List<KeyIdCryptoService> keyIdCryptoServices) {
        this.keyIdCryptoServices = keyIdCryptoServices;
    }

    public KeyIdCryptoService getKeyIdCryptoService(KeyId keyId) {
        return keyIdCryptoServices.stream()
                .filter(keyIdCryptoService -> keyIdCryptoService.knows(keyId))
                .findFirst()
                .orElseThrow(() -> CryptoException.unknownKeyId(keyId));
    }

    public KeyIdCryptoService getKeyIdCryptoServiceForDecryption(byte[] originalBytes) {
        return keyIdCryptoServices.stream()
                .filter(keyIdCryptoService -> keyIdCryptoService.canDecrypt(originalBytes))
                .findFirst()
                .orElseThrow(CryptoException::unknownCiphertextFormat);
    }

    public Set<KeyId> configuredKeyIds() {
        return keyIdCryptoServices.stream()
                .flatMap(keyIdCryptoService -> keyIdCryptoService.configuredKeyIds().stream())
                .collect(toSet());
    }
}
