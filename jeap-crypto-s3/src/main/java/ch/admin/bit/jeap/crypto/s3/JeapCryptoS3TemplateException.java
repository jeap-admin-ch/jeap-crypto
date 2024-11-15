package ch.admin.bit.jeap.crypto.s3;

import java.io.IOException;

public final class JeapCryptoS3TemplateException extends RuntimeException {

    private JeapCryptoS3TemplateException(String message, Exception cause) {
        super(message, cause);
    }
    public static JeapCryptoS3TemplateException putObjectFailed(String bucketName, String objectKey, IOException ex) {
        return new JeapCryptoS3TemplateException(
                "Could not store Object %s on Bucket %s.".formatted(objectKey, bucketName), ex);
    }

    public static JeapCryptoS3TemplateException getObjectFailed(String bucketName, String objectKey, IOException ex) {
        return new JeapCryptoS3TemplateException(
                "Could not retrieve Object %s from Bucket %s.".formatted(objectKey, bucketName), ex);
    }
}
