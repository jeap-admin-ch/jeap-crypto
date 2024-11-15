package ch.admin.bit.jeap.crypto.starter.awskms;

import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType;
import ch.admin.bit.jeap.crypto.starter.awskms.JeapCryptoAwsKmsConfigProperties.AwsKmsKeyConfigProperties;
import ch.admin.bit.jeap.crypto.starter.awskms.JeapCryptoAwsKmsConfigProperties.EscrowKeyProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JeapCryptoAwsKmsConfigPropertiesTest {

    static final String PUBLIC_KEY = """
            -----BEGIN PUBLIC KEY-----
            MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAsiyfAzSDVkxtKPF7AyjV
            VAEqa+7fYUMxqpiRc/lHhofiNo4Wk9mPYmQffOxULDbKtv2fCeeLD3tez6Y1ccz4
            rwcPPTnQb0xo/NP6dyxkqeQ36IWH9UXm9Cjl1Ei/U/6DK5TDZ9OPYJVlPBvGusVU
            SzF0N6aacZYFNq48Cc7IWy4ICnlIdKb204AXm5of9CffWqe/CfNTboTaDASOBxmF
            LtJIhOqH3Y9bPSS9H+iFO52aUeSCPCiL5/wtS6R+5fJe1bth9srfHdWfCfO0fybr
            pmi0DJ9aE0oihteuxGcbm4JY92nLl67PLCbdVu/EiiTLDIqKG0w9k9y2MlBblG9p
            5QMcJI0ZNyPqUOQd/ibXhJZeOThyuDsoXUrSHF2dNo/ATe1+QCdVkgJfOeflG/rX
            TUwYYAPW52SkmedV9aLEMf6S0jOQEuKcb5e8m88JakDBCn1Zh076E9zCCGBNN9hX
            6RQloJ1KS6ppdDtv3+CtkhVVqtfG41ZUbDluFGp6j2gkc11QhsupxidDJNBxkmFH
            UH4EUFEZ/u6+h2HTgp1idgoAF7LeEy3W8UBzXrVXqKhoU4GhPmD6UiQ81ldh85/h
            NxtwdN8hN/dJ68IP6lcCLgdfz9HmiOJPz6cug4Q/6OTl2jTc/cdH8D+rMPgGNfka
            NRBxkFm+tjh7kEeujE7X1XkCAwEAAQ==
            -----END PUBLIC KEY-----
            """;

    @Test
    void validate_noDefaultEscrowKey() {
        JeapCryptoAwsKmsConfigProperties props = new JeapCryptoAwsKmsConfigProperties();
        props.setEnvironment(new MockEnvironment());
        AwsKmsKeyConfigProperties keyProps = new AwsKmsKeyConfigProperties();
        keyProps.setKeyArn("key-arn");
        props.setKeys(Map.of("my-key", keyProps));

        assertThatThrownBy(props::postProcessConfiguration)
                .hasMessageContaining("The AWS KMS configuration under jeap.crypto.awskms is missing a default escrow key");
    }

    @Test
    void validate_noDefaultEscrowKey_okWhenNoKeyIsConfigured() {
        JeapCryptoAwsKmsConfigProperties props = new JeapCryptoAwsKmsConfigProperties();
        props.setEnvironment(new MockEnvironment());

        assertDoesNotThrow(props::postProcessConfiguration);
    }

    @Test
    void validate_noDefaultEscrowKey_butCryptoDisabled() {
        JeapCryptoAwsKmsConfigProperties props = new JeapCryptoAwsKmsConfigProperties();
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("jeap.crypto.disabledForTestEnv", "true");
        props.setEnvironment(environment);

        assertDoesNotThrow(props::postProcessConfiguration);
    }

    @Test
    void validate_defaultEscrowKeyTypeNone() {
        JeapCryptoAwsKmsConfigProperties props = new JeapCryptoAwsKmsConfigProperties();
        props.setEnvironment(new MockEnvironment());

        EscrowKeyProperties defaultEscrowKey = new EscrowKeyProperties();
        defaultEscrowKey.setKeyType(EscrowKeyType.NONE);
        props.setDefaultEscrowKey(defaultEscrowKey);

        assertDoesNotThrow(props::postProcessConfiguration);
    }

    @Test
    void validate_ok() {
        JeapCryptoAwsKmsConfigProperties props = new JeapCryptoAwsKmsConfigProperties();
        props.setEnvironment(new MockEnvironment());

        EscrowKeyProperties defaultEscrowKey = new EscrowKeyProperties();
        defaultEscrowKey.setKeyType(EscrowKeyType.RSA_4096);
        defaultEscrowKey.setPublicKey(PUBLIC_KEY);
        props.setDefaultEscrowKey(defaultEscrowKey);

        assertDoesNotThrow(props::postProcessConfiguration);
    }

    @Test
    void validate_withKey_ok() {
        JeapCryptoAwsKmsConfigProperties props = new JeapCryptoAwsKmsConfigProperties();
        props.setEnvironment(new MockEnvironment());

        EscrowKeyProperties escrowKeyProperties = new EscrowKeyProperties();
        escrowKeyProperties.setKeyType(EscrowKeyType.RSA_4096);
        escrowKeyProperties.setPublicKey(PUBLIC_KEY);
        props.setDefaultEscrowKey(escrowKeyProperties);

        EscrowKeyProperties escrowKeyProps = new EscrowKeyProperties();
        escrowKeyProps.setPublicKey(PUBLIC_KEY);
        AwsKmsKeyConfigProperties keyProps = new AwsKmsKeyConfigProperties();
        keyProps.setEscrowKey(escrowKeyProps);
        keyProps.setKeyArn("key-arn");
        props.setKeys(Map.of("my-key", keyProps));

        assertDoesNotThrow(props::postProcessConfiguration);

        assertThat(props.getDefaultEscrowKey().getParsedPublicKey())
                .isNotNull();
        assertThat(props.getKeys().get("my-key").getEscrowKey().getParsedPublicKey())
                .isNotNull();
    }
}
