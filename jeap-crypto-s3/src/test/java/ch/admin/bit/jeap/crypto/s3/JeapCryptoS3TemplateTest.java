package ch.admin.bit.jeap.crypto.s3;

import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JeapCryptoS3TemplateTest {

    private static final String BUCKET_NAME = "testBucketName";
    private static final String OBJECT_KEY_NAME = "testObjectKeyName";
    private static final String PLAIN_TEXT = "Hello Test";

    @Test
    void putObject_with_no_metadata() {
        // given
        CryptoService cryptoServiceMock = mock(CryptoService.class);
        S3Client s3ClientMock = mock(S3Client.class);
        JeapCryptoS3Template jeapCryptoS3Template = new JeapCryptoS3Template(s3ClientMock, cryptoServiceMock);

        byte[] encryptedByteArray = "XYZ".getBytes(StandardCharsets.UTF_8);
        when(cryptoServiceMock.encrypt(any(byte[].class))).thenReturn(encryptedByteArray);

        byte[] plainTextByteArray = PLAIN_TEXT.getBytes(StandardCharsets.UTF_8);

        // when
        jeapCryptoS3Template.putObject(BUCKET_NAME, OBJECT_KEY_NAME, plainTextByteArray);

        // then
        verify(s3ClientMock, times(1)).putObject(ArgumentMatchers.any(PutObjectRequest.class), any(RequestBody.class));
        ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3ClientMock).putObject(argument.capture(), any(RequestBody.class));
        assertEquals(BUCKET_NAME, argument.getValue().bucket());
        assertEquals(OBJECT_KEY_NAME, argument.getValue().key());
        assertEquals("true", argument.getValue().metadata().get("is_encrypted"));
    }

    @Test
    void putObject_with_metadata() {
        // given
        CryptoService cryptoServiceMock = mock(CryptoService.class);
        S3Client s3ClientMock = mock(S3Client.class);

        byte[] encryptedByteArray = "XYZ".getBytes(StandardCharsets.UTF_8);
        when(cryptoServiceMock.encrypt(any(byte[].class))).thenReturn(encryptedByteArray);

        byte[] plainTextByteArray = PLAIN_TEXT.getBytes(StandardCharsets.UTF_8);

        Map<String, String> userMetaDataMap = Map.of(
                "author", "Max Muster",
                "key2", "key2_value"
        );

        // when
        JeapCryptoS3Template jeapCryptoS3Template = new JeapCryptoS3Template(s3ClientMock, cryptoServiceMock);
        jeapCryptoS3Template.putObject(BUCKET_NAME, OBJECT_KEY_NAME, plainTextByteArray, userMetaDataMap);

        // then
        verify(s3ClientMock, times(1)).putObject(ArgumentMatchers.any(PutObjectRequest.class), any(RequestBody.class));
        ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3ClientMock).putObject(argument.capture(), any(RequestBody.class));
        assertEquals(BUCKET_NAME, argument.getValue().bucket());
        assertEquals(OBJECT_KEY_NAME, argument.getValue().key());
        assertEquals("true", argument.getValue().metadata().get("is_encrypted"));
        assertEquals("Max Muster", argument.getValue().metadata().get("author"));
        assertEquals("key2_value", argument.getValue().metadata().get("key2"));
    }

    @Test
    void getObject_with_decryption() {
        // given
        CryptoService cryptoServiceMock = mock(CryptoService.class);
        S3Client s3ClientMock = mock(S3Client.class);
        JeapCryptoS3Template jeapCryptoS3Template = new JeapCryptoS3Template(s3ClientMock, cryptoServiceMock);

        byte[] decryptedByteArray = PLAIN_TEXT.getBytes(StandardCharsets.UTF_8);
        when(cryptoServiceMock.decrypt(any(byte[].class))).thenReturn(decryptedByteArray);
        GetObjectResponse response = GetObjectResponse.builder()
                .build();
        ResponseBytes<GetObjectResponse> objectBytes = ResponseBytes.fromByteArray(response, "Hello Test".getBytes(StandardCharsets.UTF_8));
        when(s3ClientMock.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(objectBytes);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("is_encrypted", "true");
        HeadObjectResponse headObjectResponse = HeadObjectResponse.builder()
                .metadata(metadata)
                .build();
        when(s3ClientMock.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);

        // when
        JeapDecryptedS3Object jeapDecryptedS3Object = jeapCryptoS3Template.getObject(BUCKET_NAME, OBJECT_KEY_NAME);

        // then
        verify(s3ClientMock, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
        verify(cryptoServiceMock, times(1)).decrypt(any());
        assertEquals(BUCKET_NAME, jeapDecryptedS3Object.getBucketName());
        assertEquals(OBJECT_KEY_NAME, jeapDecryptedS3Object.getObjectKey());
        assertEquals(PLAIN_TEXT, new String(jeapDecryptedS3Object.getDecryptedObjectContent(), StandardCharsets.UTF_8));
    }

    @Test
    void getObject_with_no_decryption() {
        // given
        CryptoService cryptoServiceMock = mock(CryptoService.class);
        S3Client s3ClientMock = mock(S3Client.class);
        JeapCryptoS3Template jeapCryptoS3Template = new JeapCryptoS3Template(s3ClientMock, cryptoServiceMock);

        byte[] decryptedByteArray = PLAIN_TEXT.getBytes(StandardCharsets.UTF_8);
        when(cryptoServiceMock.decrypt(any(byte[].class))).thenReturn(decryptedByteArray);
        GetObjectResponse response = GetObjectResponse.builder()
                .build();
        ResponseBytes<GetObjectResponse> objectBytes = ResponseBytes.fromByteArray(response, "Hello Test".getBytes(StandardCharsets.UTF_8));
        when(s3ClientMock.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(objectBytes);
        Map<String, String> metadata = new HashMap<>();
        HeadObjectResponse headObjectResponse = HeadObjectResponse.builder()
                .metadata(metadata)
                .build();
        when(s3ClientMock.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);

        // when
        JeapDecryptedS3Object jeapDecryptedS3Object = jeapCryptoS3Template.getObject(BUCKET_NAME, OBJECT_KEY_NAME);

        // then
        verify(s3ClientMock, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
        verify(cryptoServiceMock, never()).decrypt(any());
        assertEquals(BUCKET_NAME, jeapDecryptedS3Object.getBucketName());
        assertEquals(OBJECT_KEY_NAME, jeapDecryptedS3Object.getObjectKey());
        assertEquals(PLAIN_TEXT, new String(jeapDecryptedS3Object.getDecryptedObjectContent(), StandardCharsets.UTF_8));
    }

    @Test
    void putObject_with_key_reference() {
        // given
        KeyReferenceCryptoService cryptoServiceMock = mock(KeyReferenceCryptoService.class);
        S3Client s3ClientMock = mock(S3Client.class);
        KeyReference keyReference = new KeyReference("test");
        JeapCryptoS3Template jeapCryptoS3Template = new JeapCryptoS3Template(s3ClientMock, cryptoServiceMock, keyReference);

        byte[] encryptedByteArray = "XYZ".getBytes(StandardCharsets.UTF_8);
        when(cryptoServiceMock.encrypt(any(byte[].class), eq(keyReference))).thenReturn(encryptedByteArray);

        byte[] plainTextByteArray = PLAIN_TEXT.getBytes(StandardCharsets.UTF_8);

        // when
        jeapCryptoS3Template.putObject(BUCKET_NAME, OBJECT_KEY_NAME, plainTextByteArray);

        // then
        verify(s3ClientMock, times(1)).putObject(ArgumentMatchers.any(PutObjectRequest.class), any(RequestBody.class));
        ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3ClientMock).putObject(argument.capture(), any(RequestBody.class));
        assertEquals(BUCKET_NAME, argument.getValue().bucket());
        assertEquals(OBJECT_KEY_NAME, argument.getValue().key());
        assertEquals("true", argument.getValue().metadata().get("is_encrypted"));
    }

    @Test
    void getObject_with_key_reference_crypto_service() {
        // given
        KeyReferenceCryptoService cryptoServiceMock = mock(KeyReferenceCryptoService.class);
        S3Client s3ClientMock = mock(S3Client.class);
        KeyReference keyReference = new KeyReference("test");
        JeapCryptoS3Template jeapCryptoS3Template = new JeapCryptoS3Template(s3ClientMock, cryptoServiceMock, keyReference);

        byte[] decryptedByteArray = PLAIN_TEXT.getBytes(StandardCharsets.UTF_8);
        when(cryptoServiceMock.decrypt(any(byte[].class))).thenReturn(decryptedByteArray);
        GetObjectResponse response = GetObjectResponse.builder()
                .build();
        ResponseBytes<GetObjectResponse> objectBytes = ResponseBytes.fromByteArray(response, "Hello Test".getBytes(StandardCharsets.UTF_8));
        when(s3ClientMock.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(objectBytes);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("is_encrypted", "true");
        HeadObjectResponse headObjectResponse = HeadObjectResponse.builder()
                .metadata(metadata)
                .build();
        when(s3ClientMock.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);

        // when
        JeapDecryptedS3Object jeapDecryptedS3Object = jeapCryptoS3Template.getObject(BUCKET_NAME, OBJECT_KEY_NAME);

        // then
        verify(s3ClientMock, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
        verify(cryptoServiceMock, times(1)).decrypt(any());
        assertEquals(BUCKET_NAME, jeapDecryptedS3Object.getBucketName());
        assertEquals(OBJECT_KEY_NAME, jeapDecryptedS3Object.getObjectKey());
        assertEquals(PLAIN_TEXT, new String(jeapDecryptedS3Object.getDecryptedObjectContent(), StandardCharsets.UTF_8));
    }
}
