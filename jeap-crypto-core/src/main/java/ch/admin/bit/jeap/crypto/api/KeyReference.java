package ch.admin.bit.jeap.crypto.api;

import java.util.Objects;

/**
 * References a specific key managed by a {@link ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService}
 *
 * @param keyLocation A key location which is a URI-like string. The actual format is specific to the key management system used.
 */
public record KeyReference(String keyLocation) {

    public KeyReference {
        Objects.requireNonNull(keyLocation, "keyLocation");
    }
}
