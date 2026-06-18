# Encrypted database columns

The `jeap-crypto-db` module provides a JPA `AttributeConverter` that transparently encrypts and
decrypts individual `String` entity attributes. The plaintext is encrypted to a jEAP
[crypto container](data-format.md) and stored as a `byte[]` column; on read it is decrypted back to a
`String`.

## Dependency

```xml
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-crypto-db</artifactId>
</dependency>
```

A KMS starter (`jeap-crypto-vault-starter` or `jeap-crypto-aws-kms-starter`) and at least one
configured wrapping key are required, since the converter resolves a `KeyIdCryptoService`.

## Configuration

Declare which logical key id the converter uses:

```yaml
jeap:
  crypto:
    db:
      key-id: myDb   # a keys.<keyName> entry from the KMS configuration
```

`JeapCryptoDbConfigProperties` requires `jeap.crypto.db.key-id` to be set; the converter fails fast at
startup if it is missing or unknown to the configured `KeyIdCryptoService`.

## Usage

Annotate the attribute with `@Convert(converter = JeapCryptoStringConverter.class)`:

```java
@Entity
public class Document {

    @Id
    private String id;

    @Convert(converter = JeapCryptoStringConverter.class)
    private String secret;
}
```

The column must accept binary data (`byte[]`). `null` is mapped to `null` and an empty string to an
empty byte array (and vice versa); non-empty values are encrypted/decrypted via the configured key.

## Related

- [Crypto APIs](crypto-api.md)
- [Key management](key-management.md)
- [Configuration reference](configuration.md)
- [jeap-crypto](../README.md)
