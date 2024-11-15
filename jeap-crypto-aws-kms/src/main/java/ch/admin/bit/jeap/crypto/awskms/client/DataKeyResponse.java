package ch.admin.bit.jeap.crypto.awskms.client;

public record DataKeyResponse(String keyId,
                              byte[] encryptedDataKey,
                              byte[] dataKey) {
}
