package ch.admin.bit.jeap.crypto.internal.core.aes;

import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;

import java.util.Objects;

/**
 * Has a fixed configured key which is used for encryption and decryption. Delegates to the {@link AesGcmCryptoService}
 * internally.
 */
class ConfiguredKeyAesGcmCryptoService implements CryptoService {

    private final AesGcmCryptoService delegate;
    private final KeyReference wrappingKeyReference;

    /**
     * @param keyManagementService Provides data keys for encryption and decrypts data keys
     * @param wrappingKeyReference Reference to the fixed wrapping key used to encrypt data keys
     * @param dataFormat           Serialization format for the crypto container. As this service has a fixed wrapping
     *                             key configured, the format should not include a key reference in the serialized format.
     */
    ConfiguredKeyAesGcmCryptoService(KeyManagementService keyManagementService,
                                     KeyReference wrappingKeyReference,
                                     JeapCryptoDataFormat dataFormat) {
        Objects.requireNonNull(wrappingKeyReference, "wrappingKeyReference");
        this.delegate = new AesGcmCryptoServiceWithConfiguredDecryptionKey(keyManagementService, wrappingKeyReference, dataFormat);
        this.wrappingKeyReference = wrappingKeyReference;
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        return delegate.encrypt(plaintext, wrappingKeyReference);
    }

    @Override
    public byte[] decrypt(byte[] ciphertextCryptoContainer) {
        return delegate.decrypt(ciphertextCryptoContainer);
    }

    /**
     * An {@link AesGcmCryptoService} that does not allow to dynamically reference a decryption key in the encoded
     * {@link JeapCryptoContainer}. A fixed key is always used for decryption.
     */
    private static class AesGcmCryptoServiceWithConfiguredDecryptionKey extends AesGcmCryptoService {
        private final KeyReference wrappingKeyReference;

        private AesGcmCryptoServiceWithConfiguredDecryptionKey(KeyManagementService keyManagementService,
                                                               KeyReference wrappingKeyReference,
                                                               JeapCryptoDataFormat dataFormat) {
            super(keyManagementService, dataFormat);
            this.wrappingKeyReference = wrappingKeyReference;
        }

        @Override
        protected KeyReference getKeyReferenceForDecryption(EncryptedDataKey encryptedDataKey) {
            return wrappingKeyReference;
        }
    }
}
