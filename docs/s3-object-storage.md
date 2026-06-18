# S3 object storage

The `jeap-crypto-s3` module provides `JeapCryptoS3Template`, which encrypts objects before storing them
in an S3-compatible object store and decrypts them on retrieval. It builds on the AWS SDK v2
`S3Client`.

## Dependency

```xml
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-crypto-s3</artifactId>
</dependency>
```

## Constructing the template

`JeapCryptoS3Template` is constructed with an `S3Client` plus either a `KeyReferenceCryptoService` and
a `KeyReference`, or a `CryptoService`. Prefer the `KeyReferenceCryptoService` variant: it stores the
used key reference together with the ciphertext, which is safer for disaster recovery where the
information about the encryption key might otherwise be lost.

```java
@PostConstruct
void init() {
    S3Client s3Client = S3Client.builder()./* ... */.build();
    KeyReference keyReference = new KeyReference("arn:aws:kms:eu-central-2:1111:key/abcd-...");
    this.template = new JeapCryptoS3Template(s3Client, keyReferenceCryptoService, keyReference);
    // Alternative: new JeapCryptoS3Template(s3Client, cryptoService);
}
```

## Storing and loading objects

```java
// Encrypt and store. Sets object metadata is_encrypted=true.
template.putObject(bucketName, objectKey, plaintextBytes);
template.putObject(bucketName, objectKey, plaintextBytes, Map.of("author", "alice"));

// Load and decrypt (only when is_encrypted=true; otherwise the content is passed through).
JeapDecryptedS3Object object = template.getObject(bucketName, objectKey);
JeapDecryptedS3Object versioned = template.getObject(bucketName, objectKey, versionId);
byte[] plaintext = object.getDecryptedObjectContent();
```

`putObject` always sets the object metadata `is_encrypted=true`; additional user metadata can be passed
as a `Map<String, String>`. `getObject` decrypts only when `is_encrypted` is `true`, otherwise the raw
content is returned. The result is a `JeapDecryptedS3Object` holding the bucket name, object key,
version id, the object metadata and the decrypted content.

## Related

- [Crypto APIs](crypto-api.md)
- [Key management](key-management.md)
- [Binary container format](data-format.md)
- [jeap-crypto](../README.md)
