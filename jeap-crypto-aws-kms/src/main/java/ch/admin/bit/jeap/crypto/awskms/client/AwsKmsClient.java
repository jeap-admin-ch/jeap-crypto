package ch.admin.bit.jeap.crypto.awskms.client;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyResponse;

import java.net.URI;

@Slf4j
public class AwsKmsClient {

    private final KmsClient kmsClient;

    public AwsKmsClient(AwsCredentialsProvider awsCredentialsProvider, Region region) {
        this.kmsClient = KmsClient.builder()
                .region(region)
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    public AwsKmsClient(AwsCredentialsProvider awsCredentialsProvider, Region region, URI endpointOverride) {
        this.kmsClient = KmsClient.builder()
                .region(region)
                .credentialsProvider(awsCredentialsProvider)
                .endpointOverride(endpointOverride)
                .build();
    }

    public AwsKmsClient(AwsCredentialsProvider awsCredentialsProvider, Region region, URI endpointOverride, SdkHttpClient httpClient) {
        this.kmsClient = KmsClient.builder()
                .region(region)
                .httpClient(httpClient)
                .credentialsProvider(awsCredentialsProvider)
                .endpointOverride(endpointOverride)
                .build();
    }

    @PreDestroy
    void close() {
        kmsClient.close();
    }

    /**
     * See <a href="https://docs.aws.amazon.com/kms/latest/APIReference/API_GenerateDataKey.html">API_GenerateDataKey</a>
     *
     * @param keyId Specifies the symmetric encryption KMS key that encrypts the data key. You cannot specify an
     *              asymmetric KMS key or a KMS key in a custom key store. To specify a KMS key, use its key ID, key ARN,
     *              alias name, or alias ARN. When using an alias name, prefix it with "alias/". To specify a KMS key in
     *              a different AWS account, you must use the key ARN or alias ARN.
     *              For example:
     *              <ul>
     *              <li>Key ID: 1234abcd-12ab-34cd-56ef-1234567890ab</li>
     *              <li>Key ARN: arn:aws:kms:us-east-2:111122223333:key/1234abcd-12ab-34cd-56ef-1234567890ab</li>
     *              <li>Alias name: alias/ExampleAlias</li>
     *              <li>Alias ARN: arn:aws:kms:us-east-2:111122223333:alias/ExampleAlias</li>
     *              </ul>
     */
    public DataKeyResponse createDataKey(String keyId) {
        GenerateDataKeyRequest request = GenerateDataKeyRequest.builder()
                .keyId(keyId)
                .keySpec("AES_256")
                .build();

        GenerateDataKeyResponse response = kmsClient.generateDataKey(request);
        log.debug("Created a data key for key id '{}'", response.keyId());

        return new DataKeyResponse(response.keyId(),
                response.ciphertextBlob().asByteArray(),
                response.plaintext().asByteArray());
    }

    public byte[] decryptDataKey(byte[] ciphertextBlob) {
        DecryptRequest request = DecryptRequest.builder()
                .ciphertextBlob(SdkBytes.fromByteArray(ciphertextBlob))
                .build();

        DecryptResponse response = kmsClient.decrypt(request);

        return response.plaintext().asByteArray();
    }
}
