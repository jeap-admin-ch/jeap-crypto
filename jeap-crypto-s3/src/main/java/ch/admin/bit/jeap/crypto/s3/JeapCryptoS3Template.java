package ch.admin.bit.jeap.crypto.s3;

import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The JeapCryptoS3Template can be used to put or get objects to/from S3 without
 * concerning about encryption or decryption.
 */
public class JeapCryptoS3Template {

    private static final String METADATA_KEY_IS_ENCRYPTED = "is_encrypted";

    private final S3Client s3Client;
    private final Function<byte[], byte[]> encryptionFunction;
    private final Function<byte[], byte[]> decryptionFunction;

    /**
     * Construct a new JeapCryptoS3Template, given a AmazonS3-Client and a KeyReferenceCryptoService. Implementations
     * will typically store a reference to the used encryption key together with the ciphertext. As S3 objects are
     * usually not sensitive to a few more bytes being stored, prefer this constructor as it provides the benefit of
     * additional safety concerning decryption in a disaster-recovery scenario where the information about the key used
     * for encryption might be damaged.
     *
     * @param s3Client                  the AmazonS3-Client where the Objects are stored
     * @param keyReferenceCryptoService handles the encryption and decryption
     * @param keyReference              the key to use for encryption
     */
    public JeapCryptoS3Template(S3Client s3Client, KeyReferenceCryptoService keyReferenceCryptoService, KeyReference keyReference) {
        this.s3Client = s3Client;
        this.encryptionFunction = plaintext -> keyReferenceCryptoService.encrypt(plaintext, keyReference);
        this.decryptionFunction = keyReferenceCryptoService::decrypt;
    }

    /**
     * Construct a new JeapCryptoS3Template, given a AmazonS3-Client and a CryptoService.
     *
     * @param s3Client      the AmazonS3-Client where the Objects are stored
     * @param cryptoService which handles the encryption and decryption
     */
    public JeapCryptoS3Template(S3Client s3Client, CryptoService cryptoService) {
        this.s3Client = s3Client;
        this.encryptionFunction = cryptoService::encrypt;
        this.decryptionFunction = cryptoService::decrypt;
    }

    /**
     * Uploads a new object to the specified Amazon S3 bucket.
     * Encrypts the Content and set a userMetaData 'is_encrypted: true'.
     *
     * @param bucketName         - The name of an existing bucket, to which you have Permission.Write permission
     * @param objectKey          - The key under which to store the specified file.
     * @param plainTextByteArray - Plaintext as byte[], which will be encrypted
     */
    public void putObject(String bucketName, String objectKey, byte[] plainTextByteArray) {
        this.putObject(bucketName, objectKey, plainTextByteArray, Map.of());
    }

    /**
     * Uploads a new object to the specified Amazon S3 bucket.
     * Encrypts the Content and add the userMetaDataMap to the userMetaData.
     * additionally the userMetaData 'is_encrypted: true' is set.
     *
     * @param bucketName         - The name of an existing bucket, to which you have Permission.Write permission
     * @param keyName            - The key under which to store the specified file.
     * @param plainTextByteArray - Plaintext as byte[], which will be encrypted
     * @param userMetaDataMap    Additional metaData as Map<String, String>. For example 'author'
     */
    public void putObject(String bucketName, String keyName, byte[] plainTextByteArray, Map<String, String> userMetaDataMap) {
        byte[] encryptedText = encryptionFunction.apply(plainTextByteArray);
        Map<String, String> metadata = new HashMap<>();
        metadata.put(METADATA_KEY_IS_ENCRYPTED, Boolean.TRUE.toString());
        metadata.putAll(userMetaDataMap);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .metadata(metadata)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(encryptedText));
    }

    /**
     * Retrieves objects from S3 and decrypt the content if userMetaData 'is_encrypted: true' is set.
     * If userMetaData 'is_encrypted: true' is not set, the content will not be decrypted.
     *
     * @param bucketName - The name of the bucket containing the object to retrieve.
     * @param keyName    - The key of the object to retrieve.
     * @return JeapDecryptedS3Object - holds the decrypted content of an object
     */
    public JeapDecryptedS3Object getObject(String bucketName, String keyName) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .key(keyName)
                .bucket(bucketName)
                .build();

        Map<String, String> metadata = s3Client.headObject(headObjectRequest).metadata();
        boolean isEncrypted = isObjectEncrypted(metadata);

        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(keyName)
                .bucket(bucketName)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
        byte[] objectContent = objectBytes.asByteArray();

        byte[] decryptedText;
        if (isEncrypted) {
            decryptedText = decryptionFunction.apply(objectContent);
        } else {
            // if the ObjectMetadata is not marked as encrypted, just use the content as it is
            decryptedText = objectContent;
        }

        return JeapDecryptedS3Object.of(bucketName, keyName, metadata, decryptedText);
    }

    private static boolean isObjectEncrypted(Map<String, String> metadata) {
        if (metadata.containsKey(METADATA_KEY_IS_ENCRYPTED)) {
            return Boolean.parseBoolean(metadata.get(METADATA_KEY_IS_ENCRYPTED));
        }
        return false;
    }

}
