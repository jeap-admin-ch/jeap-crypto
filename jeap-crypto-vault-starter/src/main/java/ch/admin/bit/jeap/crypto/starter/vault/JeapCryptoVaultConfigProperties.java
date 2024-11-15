package ch.admin.bit.jeap.crypto.starter.vault;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementCachingConfigProperties;
import ch.admin.bit.jeap.crypto.vault.keymanagement.VaultKeyLocation;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = JeapCryptoVaultConfigProperties.PROPERTY_PREFIX)
class JeapCryptoVaultConfigProperties implements KeyManagementCachingConfigProperties {

    static final String PROPERTY_PREFIX = "jeap.crypto.vault";

    private String defaultSecretEnginePath;
    private Map<String, VaultKeyConfigProperties> keys = Map.of();

    private Duration defaultEncryptionKeyCacheExpiryDuration = Duration.ofHours(1);
    private Duration defaultDecryptionKeyCacheExpiryDuration = Duration.ofHours(6);
    private long encryptionKeyMaxCacheSize = 100;
    private long decryptionKeyMaxCacheSize = 50000;

    @Setter(AccessLevel.NONE)
    private transient Map<KeyReference, Duration> encryptionKeyCacheExpiryDurations = new HashMap<>();
    @Setter(AccessLevel.NONE)
    private transient Map<KeyReference, Duration> decryptionKeyCacheExpiryDurations = new HashMap<>();

    @Autowired
    @Setter(AccessLevel.NONE)
    private transient Environment environment; // needed for post construct initialization

    public Duration getEncryptionKeyCacheExpiryDuration(KeyReference keyReference) {
        return encryptionKeyCacheExpiryDurations.getOrDefault(keyReference, defaultEncryptionKeyCacheExpiryDuration);
    }

    public Duration getDecryptionKeyCacheExpiryDuration(KeyReference keyReference) {
        return decryptionKeyCacheExpiryDurations.getOrDefault(keyReference, defaultDecryptionKeyCacheExpiryDuration);
    }

    @Data
    static class VaultKeyConfigProperties {
        private String keyName;
        private String secretEnginePath;
        private Duration encryptionCacheExpiryDuration;
        private Duration decryptionCacheExpiryDuration;
        KeyReference getKeyReference() {
            return VaultKeyLocation.asKeyReference(secretEnginePath, keyName);
        }
    }

    @PostConstruct
    void init() {
        // Spring does not allow for @PostContruct methods to have parameters
        postProcessConfiguration(environment);
    }

    void postProcessConfiguration(Environment environment) {
        if (defaultSecretEnginePath == null) {
            setDefaultSecretEnginePathFromVaultSystemName(environment);
        }
        keys.values().forEach(vaultKeyConfigProperties -> {
            if (vaultKeyConfigProperties.secretEnginePath == null) {
                assertDefaultSecretEnginePathAvailable();
                vaultKeyConfigProperties.secretEnginePath = defaultSecretEnginePath;
            }
            final KeyReference keyReference = vaultKeyConfigProperties.getKeyReference();
            Optional.ofNullable(vaultKeyConfigProperties.getEncryptionCacheExpiryDuration())
                    .ifPresent(duration -> encryptionKeyCacheExpiryDurations.put(keyReference, duration));
            Optional.ofNullable(vaultKeyConfigProperties.getDecryptionCacheExpiryDuration())
                    .ifPresent(duration -> decryptionKeyCacheExpiryDurations.put(keyReference, duration));
        });
        log.debug("jEAP Crypto Vault Configuration: " + this);
    }

    private void setDefaultSecretEnginePathFromVaultSystemName(Environment environment) {
        String vaultSystemName = environment.getProperty("jeap.vault.system-name");
        if (vaultSystemName != null) {
            defaultSecretEnginePath = "transit/" + vaultSystemName;
        }
    }

    private void assertDefaultSecretEnginePathAvailable() {
        if (defaultSecretEnginePath == null) {
            throw new IllegalArgumentException(
                    "Vault transit secret engine path must be set using the property %s.<key>.secret-engine-path or %s.default-secret-engine-path"
                            .formatted(PROPERTY_PREFIX, PROPERTY_PREFIX));
        }
    }

    @Override
    public String toString() {
        return "JeapCryptoVaultConfigProperties{" +
                "defaultSecretEnginePath='" + defaultSecretEnginePath + '\'' +
                ", keys=" + keys +
                ", defaultEncryptionKeyCacheExpiryDuration=" + defaultEncryptionKeyCacheExpiryDuration +
                ", defaultDecryptionKeyCacheExpiryDuration=" + defaultDecryptionKeyCacheExpiryDuration +
                ", encryptionKeyMaxCacheSize=" + encryptionKeyMaxCacheSize +
                ", decryptionKeyMaxCacheSize=" + decryptionKeyMaxCacheSize +
                ", encryptionKeyCacheExpiryDurations=" + encryptionKeyCacheExpiryDurations +
                ", decryptionKeyCacheExpiryDurations=" + decryptionKeyCacheExpiryDurations +
                '}';
    }
}
