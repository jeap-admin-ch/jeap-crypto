package ch.admin.bit.jeap.crypto.internal.core.dataformat;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.internal.core.aes.EncryptedDataKeyStub;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.AbstractEncryptedDataKeyFormat;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EncryptedDataKeyFormatStub extends AbstractEncryptedDataKeyFormat {

    @Override
    public EncryptedDataKey parse(ByteBuffer byteBuffer) {
        int keyLocationLength = byteBuffer.getInt();
        byte[] keyLocationBytes = new byte[keyLocationLength];
        byteBuffer.get(keyLocationBytes);
        int ciphertextLength = byteBuffer.getInt();
        byte[] ciphertext = new byte[ciphertextLength];
        byteBuffer.get(ciphertext);

        return new EncryptedDataKeyStub(ciphertext, new KeyReference(new String(keyLocationBytes, UTF_8)));
    }

    @Override
    public boolean canParse(ByteBuffer byteBuffer) {
        return true;
    }

    @Override
    public byte[] format(EncryptedDataKey encryptedDataKey) {
        byte[] keyLocation = encryptedDataKey.wrappingKeyReference().keyLocation().getBytes(UTF_8);
        byte[] bytes = new byte[4 + keyLocation.length + 4 + encryptedDataKey.ciphertext().length];

        return ByteBuffer.wrap(bytes)
                .putInt(keyLocation.length)
                .put(keyLocation)
                .putInt(encryptedDataKey.ciphertext().length)
                .put(encryptedDataKey.ciphertext())
                .array();
    }
}
