package ch.admin.bit.jeap.crypto.internal.core.dataformat;

import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;

public interface JeapCryptoDataFormat {
    byte[] format(JeapCryptoContainer cryptoContainer);

    JeapCryptoContainer parse(byte[] dataContainerBytes);

    boolean canParse(byte[] dataContainerBytes);
}
