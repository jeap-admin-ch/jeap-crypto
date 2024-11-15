package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType;
import ch.admin.bit.jeap.crypto.internal.core.escrow.PublicKeyParser;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementCachingConfigProperties;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.security.PublicKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = JeapCryptoAwsKmsConfigProperties.PROPERTY_PREFIX)
class JeapCryptoAwsKmsConfigProperties implements KeyManagementCachingConfigProperties {

    static final String PROPERTY_PREFIX = "jeap.crypto.awskms";

    private Map<String, AwsKmsKeyConfigProperties> keys = Map.of();

    private Duration defaultEncryptionKeyCacheExpiryDuration = Duration.ofHours(1);
    private Duration defaultDecryptionKeyCacheExpiryDuration = Duration.ofHours(6);

    private long encryptionKeyMaxCacheSize = 100;
    private long decryptionKeyMaxCacheSize = 50000;

    private String region;
    private String endpoint;

    private EscrowKeyProperties defaultEscrowKey;

    @Data
    public static class EscrowKeyProperties {
        String publicKey;
        PublicKey parsedPublicKey;
        EscrowKeyType keyType = EscrowKeyType.RSA_4096;
    }

    public URI getEndpoint() {
        return endpoint == null ? null : URI.create(endpoint);
    }

    public Region getRegion() {
        if (region != null) {
            return Region.of(region);
        }

        String regionFromEnv = environment.getProperty("AWS_REGION");
        if (regionFromEnv == null) {
            throw new IllegalStateException("Region not provided in jeap.crypto.awskms.region or in AWS_REGION");
        }
        return Region.of(regionFromEnv);
    }

    @Setter(AccessLevel.NONE)
    private transient Map<KeyReference, Duration> encryptionKeyCacheExpiryDurations = new HashMap<>();
    @Setter(AccessLevel.NONE)
    private transient Map<KeyReference, Duration> decryptionKeyCacheExpiryDurations = new HashMap<>();

    @Autowired
    private transient Environment environment;

    public Duration getEncryptionKeyCacheExpiryDuration(KeyReference keyReference) {
        return encryptionKeyCacheExpiryDurations.getOrDefault(keyReference, defaultEncryptionKeyCacheExpiryDuration);
    }

    public Duration getDecryptionKeyCacheExpiryDuration(KeyReference keyReference) {
        return decryptionKeyCacheExpiryDurations.getOrDefault(keyReference, defaultDecryptionKeyCacheExpiryDuration);
    }

    @Data
    static class AwsKmsKeyConfigProperties {
        private String keyArn;
        private Duration encryptionCacheExpiryDuration;
        private Duration decryptionCacheExpiryDuration;
        private EscrowKeyProperties escrowKey = null;

        public boolean hasEscrowKey() {
            return escrowKey != null && escrowKey.keyType != EscrowKeyType.NONE;
        }

        KeyReference getKeyReference() {
            return new KeyReference(keyArn);
        }
    }

    @PostConstruct
    void postProcessConfiguration() {
        validateAndPrepareDefaultEscrowKey();
        validateAndPrepareKeys();

        log.debug("jEAP Crypto AWS KMS Configuration: " + this);
    }

    private void validateAndPrepareKeys() {
        keys.forEach((keyName, keyProps) -> {
            final KeyReference keyReference = keyProps.getKeyReference();
            Optional.ofNullable(keyProps.getEncryptionCacheExpiryDuration())
                    .ifPresent(duration -> encryptionKeyCacheExpiryDurations.put(keyReference, duration));
            Optional.ofNullable(keyProps.getDecryptionCacheExpiryDuration())
                    .ifPresent(duration -> decryptionKeyCacheExpiryDurations.put(keyReference, duration));
            if (keyProps.hasEscrowKey() && keyProps.getEscrowKey().getPublicKey() == null) {
                throw new IllegalStateException("Encryption key jeap.crypto.awskms.%s has an escrow key set, but no public key configured"
                        .formatted(keyName));
            } else if (keyProps.hasEscrowKey()) {
                keyProps.escrowKey.parsedPublicKey = PublicKeyParser.parsePublicKey(keyProps.escrowKey.publicKey);
            }
        });
    }

    private void validateAndPrepareDefaultEscrowKey() {
        if (defaultEscrowKey != null && defaultEscrowKey.publicKey != null) {
            defaultEscrowKey.parsedPublicKey = PublicKeyParser.parsePublicKey(defaultEscrowKey.publicKey);
        }

        boolean cryptoDisabled = Boolean.parseBoolean(environment.getProperty("jeap.crypto.disabledForTestEnv", "false"));
        boolean anyKeyRequiresDefaultEscrowKey = keys.values().stream()
                .anyMatch(v -> v.getEscrowKey() == null);
        if (!cryptoDisabled && anyKeyRequiresDefaultEscrowKey) {
            if (defaultEscrowKey == null ||
                    defaultEscrowKey.keyType != EscrowKeyType.NONE && defaultEscrowKey.publicKey == null) {
                throw new IllegalStateException("The AWS KMS configuration under jeap.crypto.awskms is missing a default escrow key");
            }
        }
    }

    @Override
    public String toString() {
        return "JeapCryptoAwsKmsConfigProperties{" +
                "keys=" + keys +
                ", defaultEncryptionKeyCacheExpiryDuration=" + defaultEncryptionKeyCacheExpiryDuration +
                ", defaultDecryptionKeyCacheExpiryDuration=" + defaultDecryptionKeyCacheExpiryDuration +
                ", encryptionKeyMaxCacheSize=" + encryptionKeyMaxCacheSize +
                ", decryptionKeyMaxCacheSize=" + decryptionKeyMaxCacheSize +
                ", region='" + region + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
