package ch.admin.bit.jeap.crypto.vault.keymanagement;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VaultKeyLocationTest {

    @Test
    void from() {
        VaultKeyLocation vaultKeyLocation = VaultKeyLocation.fromKeyReference(new KeyReference("v:/path:key-name"));

        assertThat(vaultKeyLocation.secretEnginePath())
                .isEqualTo("/path");
        assertThat(vaultKeyLocation.keyName())
                .isEqualTo("key-name");
    }

    @Test
    void toKeyReference() {
        KeyReference keyReference = VaultKeyLocation.asKeyReference("/path", "key-name");

        assertThat(keyReference.keyLocation())
                .isEqualTo("v:/path:key-name");
    }
}