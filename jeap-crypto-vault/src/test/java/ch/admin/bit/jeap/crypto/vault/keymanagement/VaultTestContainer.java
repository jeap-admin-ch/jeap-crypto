package ch.admin.bit.jeap.crypto.vault.keymanagement;

import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.vault.VaultContainer;

@SuppressWarnings("resource")
class VaultTestContainer extends VaultContainer<VaultTestContainer> {

    private static final DockerImageName IMAGE_NAME = DockerImageName.parse("vault:1.12.0")
            .asCompatibleSubstituteFor("vault");

    VaultTestContainer() {
        super(IMAGE_NAME);

        withVaultToken("secret");
        withExtraHost("vault-server", "127.0.0.1");
        withCopyFileToContainer(MountableFile.forHostPath("../docker/vault-test-config.sh"), "/vault-test-config.sh");
        withCopyFileToContainer(MountableFile.forHostPath("../docker/jeap-crypto-policies.hcl"), "/jeap-crypto-policies.hcl");
    }
}
