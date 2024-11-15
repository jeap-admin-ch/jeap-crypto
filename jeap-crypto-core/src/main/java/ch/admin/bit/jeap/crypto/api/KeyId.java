package ch.admin.bit.jeap.crypto.api;

import java.util.Objects;

/**
 * Logical identifier for a wrapping key.
 *
 * @param id The id string identifying the wrapping key
 */
public record KeyId(String id){

    public KeyId {
        Objects.requireNonNull(id, "id must be provided");
    }

    public static KeyId of(String id) {
        return new KeyId(id);
    }
}
