package ch.admin.bit.jeap.crypto.db;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Converter
public class JeapCryptoStringConverter implements AttributeConverter<String, byte[]> {

    private final KeyIdCryptoService keyIdCryptoService;

    private final KeyId keyId;

    public JeapCryptoStringConverter(List<KeyIdCryptoService> keyIdCryptoServices, JeapCryptoDbConfigProperties properties) {
        if (properties.getKeyId() == null) {
            throw new IllegalStateException("The JeapCryptoStringConverter is configured for one or more attributes but"
                    + " no encryption key id is declared in the application");
        }

        keyId = KeyId.of(properties.getKeyId());
        keyIdCryptoService = keyIdCryptoServices.stream()
                .filter(keyIdCryptoService -> keyIdCryptoService.knows(keyId))
                .findFirst()
                .orElseThrow(() -> CryptoException.unknownKeyId(keyId));

        if (!this.keyIdCryptoService.knows(keyId)) {
            throw new IllegalStateException("The configured key id in the application '" + properties.getKeyId() + "'" +
                    " is not known to the configured key id crypto service instance");
        }
    }

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (attribute != null) {
            if (!attribute.isEmpty()) {
                return keyIdCryptoService.encrypt(attribute.getBytes(StandardCharsets.UTF_8), keyId);
            } else {
                return new byte[0];
            }
        }
        return null;
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData != null) {
            if (dbData.length > 0) {
                return new String(keyIdCryptoService.decrypt(dbData), StandardCharsets.UTF_8);
            } else {
                return "";
            }
        }
        return null;
    }
}
