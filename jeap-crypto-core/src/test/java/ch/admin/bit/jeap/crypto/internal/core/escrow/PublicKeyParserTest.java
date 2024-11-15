package ch.admin.bit.jeap.crypto.internal.core.escrow;

import org.junit.jupiter.api.Test;

import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;

class PublicKeyParserTest {

    @Test
    void parsePublicKey() {
        PublicKey publicKey = PublicKeyParser.parsePublicKey("""
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
                """);
        assertThat(publicKey.getAlgorithm())
                .isEqualTo("RSA");
    }
}
