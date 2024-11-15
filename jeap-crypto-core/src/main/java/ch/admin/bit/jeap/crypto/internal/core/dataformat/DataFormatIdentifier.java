package ch.admin.bit.jeap.crypto.internal.core.dataformat;

public enum DataFormatIdentifier {
    COMPACT_FORMAT_IDENTIFIER(1),
    KEY_REFERENCE_FORMAT_IDENTIFIER(2),
    MULTI_KEY_FORMAT_IDENTIFIER(3);

    private final byte formatId;

    DataFormatIdentifier(int formatId) {
        this.formatId = (byte) formatId;
    }

    public byte formatId() {
        return formatId;
    }
}
