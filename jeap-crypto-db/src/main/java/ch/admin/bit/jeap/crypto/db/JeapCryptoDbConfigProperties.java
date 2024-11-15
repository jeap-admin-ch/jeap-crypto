package ch.admin.bit.jeap.crypto.db;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = JeapCryptoDbConfigProperties.PROPERTY_PREFIX)
class JeapCryptoDbConfigProperties {

    static final String PROPERTY_PREFIX = "jeap.crypto.db";

    private String keyId;

}
