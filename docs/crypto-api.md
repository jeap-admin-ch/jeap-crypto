# Crypto APIs

jEAP Crypto exposes three interfaces in package `ch.admin.bit.jeap.crypto.api`. They differ in how the
wrapping key is selected and whether a reference to it is stored in the container. All of them encrypt
to and decrypt from the jEAP [crypto container format](data-format.md). `CryptoException` is thrown on
any failure (empty plaintext, bad configuration, unknown key, KMS error, ...).

## `CryptoService` — fixed key from configuration

One `CryptoService` bean is registered per configured wrapping key. The wrapping key is taken from
configuration and **not** stored in the container.

```java
public interface CryptoService {
    byte[] encrypt(byte[] plaintext);
    byte[] decrypt(byte[] ciphertextCryptoContainer);
}
```

Inject by type when a single key is configured, or by bean name / `@Qualifier` when several are. The
bean name is the camel-cased key name plus `CryptoService` (e.g. key `myDb` → `myDbCryptoService`).

```java
@Qualifier("myDbCryptoService")
@Autowired
private CryptoService cryptoService;
```

## `KeyIdCryptoService` — choose key by logical id at runtime

Selects the wrapping key at runtime by its configured **logical name** (`KeyId`). A single
`KeyIdCryptoService` bean knows all configured keys of its KMS, so no qualifier is needed.

```java
public interface KeyIdCryptoService {
    byte[] encrypt(byte[] plaintext, KeyId keyId);
    byte[] decrypt(byte[] ciphertextCryptoContainer);
    boolean knows(KeyId keyId);
    Set<KeyId> configuredKeyIds();
    boolean canDecrypt(byte[] ciphertext);
}
```

```java
@Autowired
private KeyIdCryptoService keyIdCryptoService;

byte[] container = keyIdCryptoService.encrypt(plaintext, KeyId.of("myDb"));
```

`KeyId` is a record created with `KeyId.of("<keyName>")`, where `<keyName>` is a `keys.<keyName>` entry
from the configuration. `CryptoServiceProvider` can pick the right `KeyIdCryptoService` across multiple
KMS backends, e.g. via `getKeyIdCryptoServiceForDecryption(byte[])`.

## `KeyReferenceCryptoService` — choose key by KMS reference

Selects the wrapping key at runtime by a KMS-specific `KeyReference`. The reference is **stored in the
container**, so decryption needs no out-of-band knowledge of the key. Useful for cross-application data
exchange, where the key depends on sender/recipient.

```java
public interface KeyReferenceCryptoService {
    byte[] encrypt(byte[] plaintext, KeyReference wrappingKeyReference);
    byte[] decrypt(byte[] ciphertextCryptoContainer);
    boolean canDecrypt(byte[] ciphertext);
}
```

```java
@Autowired
private KeyReferenceCryptoService keyReferenceCryptoService;

// Vault: build the reference from secret-engine path + key name
KeyReference vaultKey = VaultKeyLocation.asKeyReference("transit/jme", "jme-crypto-example-database-key");
// AWS KMS: the reference is the ARN, alias or key id
KeyReference awsKey = new KeyReference("arn:aws:kms:us-west-2:111122223333:key/1234abcd-...");

byte[] container = keyReferenceCryptoService.encrypt(plaintext, vaultKey);
byte[] plaintext = keyReferenceCryptoService.decrypt(container);
```

> Storing the key reference adds a few bytes per container (for Vault roughly 30+ bytes, depending on
> the secret-engine path and key name length). AWS KMS always uses the multi-key format that includes
> the reference.

## Related

- [Getting started](getting-started.md)
- [Key management](key-management.md)
- [Binary container format](data-format.md)
- [jeap-crypto](../README.md)
