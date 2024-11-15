package ch.admin.bit.jeap.crypto.s3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class JeapDecryptedS3Object {

    private String bucketName;
    private String objectKey;
    private Map<String, String> metadata = new HashMap<>();
    private byte[] decryptedObjectContent;

}
