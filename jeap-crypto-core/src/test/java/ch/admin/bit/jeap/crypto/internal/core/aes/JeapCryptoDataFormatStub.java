package ch.admin.bit.jeap.crypto.internal.core.aes;

import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;
import junit.framework.AssertionFailedError;

import java.util.HashMap;
import java.util.Map;

public class JeapCryptoDataFormatStub implements JeapCryptoDataFormat {

    private final Map<byte[], JeapCryptoContainer> encryptedToContainer = new HashMap<>();

    @Override
    public byte[] format(JeapCryptoContainer cryptoContainer) {
        byte[] encrypted = new byte[5];
        encryptedToContainer.put(encrypted, cryptoContainer);
        return encrypted;
    }

    @Override
    public JeapCryptoContainer parse(byte[] dataContainerBytes) {
        return encryptedToContainer.computeIfAbsent(dataContainerBytes, ignored -> {
            throw new AssertionFailedError("Not a previously encrypted container in stub");
        });
    }

    @Override
    public boolean canParse(byte[] dataContainerBytes) {
        return encryptedToContainer.containsKey(dataContainerBytes);
    }
}
