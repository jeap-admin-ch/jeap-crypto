package ch.admin.bit.jeap.crypto.api;

import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType;
import com.amazon.corretto.crypto.provider.SelfTestStatus;

import java.security.GeneralSecurityException;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class CryptoException extends RuntimeException {
    private CryptoException(String message) {
        super(message);
    }

    private CryptoException(String message, Exception cause) {
        super(message, cause);
    }

    public static CryptoException encryptionFailed(GeneralSecurityException e) {
        return new CryptoException("Encryption failed", e);
    }

    public static CryptoException decryptionFailed(GeneralSecurityException e) {
        return new CryptoException("Decryption failed", e);
    }

    public static CryptoException nullKey() {
        throw new CryptoException("Bad key: Key is null");
    }

    public static CryptoException badKeySize(int size, int expectedSize) {
        throw new CryptoException("Bad key: size is %d bytes, expected %d bytes".formatted(size, expectedSize));
    }

    public static CryptoException initializationFailed(SelfTestStatus selfTestStatus) {
        return new CryptoException("Initialization of the JCE crypto provider failed: " + selfTestStatus);
    }

    public static CryptoException badNonceLength(int length, int expectedLength) {
        return new CryptoException("Bad nonce length %d bytes, expected %d bytes".formatted(length, expectedLength));
    }

    public static CryptoException getDataKeyFailed(Exception cause, String keyLocation) {
        return new CryptoException("Failed to create data key using key at " + keyLocation, cause);
    }

    public static CryptoException getDataKeyFailed(String keyLocation) {
        return new CryptoException("Failed to create data key using key at " + keyLocation);
    }

    public static CryptoException decryptDataKeyFailed(Exception cause, String keyLocation) {
        return new CryptoException("Failed to decrypt data key using key  " + keyLocation, cause);
    }

    public static CryptoException badContainerFormatIdentifier(byte formatIdentifier) {
        return new CryptoException("Bad container format identifier %d".formatted((int) formatIdentifier));
    }

    public static CryptoException badFieldLength(String fieldName, int actualLength, int expectedLength) {
        return new CryptoException("Field %s has unexpected length of %d bytes. Expected are %d bytes."
                .formatted(fieldName, actualLength, expectedLength));
    }

    public static CryptoException emptyCiphertext() {
        return new CryptoException("Ciphertext is empty");
    }

    public static CryptoException emptyPlaintext() {
        return new CryptoException("Cannot encrypt empty plaintext");
    }

    public static CryptoException unexpectedBufferSize(int remainingBytes) {
        return new CryptoException(
                ("Unexpected state when reading or writing data container bytes: Expected zero remaining bytes in " +
                        "buffer, but %d bytes are still not read or written.").formatted(remainingBytes));
    }

    public static CryptoException missingWrappingKeyReference() {
        return new CryptoException("Missing wrapping key reference");
    }

    public static CryptoException badKeyReferenceType(String keyLocation, String kmsType) {
        return new CryptoException(
                "Key location %s has the wrong type/prefix for %s".formatted(keyLocation, kmsType));
    }

    public static CryptoException unknownKeyId(KeyId keyId) {
        return new CryptoException("Key id '%s' is unknown".formatted(keyId.id()));
    }

    public static CryptoException unknownProviderId(byte[] keyProviderIdBytes) {
        return new CryptoException("Key provider '%s' is unknown".formatted(new String(keyProviderIdBytes, UTF_8)));
    }

    public static CryptoException badDataKeyFormatIdentifier(int dataFormatId) {
        return new CryptoException("Data key format id '%d' is unknown".formatted(dataFormatId));
    }

    public static CryptoException unknownAlgorithmId(int expected, int actual) {
        return new CryptoException("Unknown algorithm id %d, expected %d".formatted(actual, expected));
    }

    public static CryptoException duplicateKeyId(KeyId keyId) {
        return new CryptoException(
                "The key ID '%s' is used more than once - please make sure to configure unique key IDs for each encryption key."
                        .formatted(keyId.id()));
    }

    public static CryptoException unknownCiphertextFormat() {
        return new CryptoException("Unable to parse ciphertext - unknown format");
    }

    public static CryptoException generalSecurityException(String message, GeneralSecurityException exception) {
        return new CryptoException(message, exception);
    }

    public static CryptoException unknownEscrowKeyType(EscrowKeyType escrowKeyType) {
        return new CryptoException("Unknown escrow key type: " + escrowKeyType);
    }

    public static CryptoException missingEscrowKey(KeyReference wrappingKeyReference) {
        return new CryptoException("Missing a configured escrow key for key " + wrappingKeyReference);
    }
}
