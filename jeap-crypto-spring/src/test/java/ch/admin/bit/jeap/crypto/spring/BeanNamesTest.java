package ch.admin.bit.jeap.crypto.spring;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

class BeanNamesTest {

    @Test
    void toCamelCaseBeanName() {
        AssertionsForClassTypes.assertThat(BeanNames.beanNameFromKeyId("lower", "CryptoService"))
                .isEqualTo("lowerCryptoService");
        AssertionsForClassTypes.assertThat(BeanNames.beanNameFromKeyId("camelCase", "CryptoService"))
                .isEqualTo("camelCaseCryptoService");
        AssertionsForClassTypes.assertThat(BeanNames.beanNameFromKeyId("kebab-case", "CryptoService"))
                .isEqualTo("kebabCaseCryptoService");
        AssertionsForClassTypes.assertThat(BeanNames.beanNameFromKeyId("special*char1", "CryptoService"))
                .isEqualTo("specialChar1CryptoService");
        AssertionsForClassTypes.assertThat(BeanNames.beanNameFromKeyId("123", "CryptoService"))
                .isEqualTo("_123CryptoService");
        AssertionsForClassTypes.assertThat(BeanNames.beanNameFromKeyId("l", "CryptoService"))
                .isEqualTo("lCryptoService");
        AssertionsForClassTypes.assertThat(BeanNames.beanNameFromKeyId("U", "CryptoService"))
                .isEqualTo("uCryptoService");
        AssertionsForClassTypes.assertThat(BeanNames.beanNameFromKeyId("UU", "CryptoService"))
                .isEqualTo("uUCryptoService");
    }
}
