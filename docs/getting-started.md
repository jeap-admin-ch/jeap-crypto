# Getting started

This page shows how to add jEAP Crypto to a Spring Boot service and encrypt and decrypt data. For the
bigger picture see [Architecture](architecture.md); for the full property list see the
[Configuration reference](configuration.md).

jEAP Crypto encrypts byte arrays **client-side** with AES-256-GCM. The data key used for the actual
encryption is wrapped (encrypted) by a wrapping key managed in a Key Management Service (KMS) — either
HashiCorp Vault or AWS KMS — and stored together with the ciphertext. You configure one or more
wrapping keys; the starter then registers a `CryptoService` bean per key.

## 1. Add the starter

Pick the starter for your KMS. For Vault, also add the jEAP Vault starter, which configures the
connection to Vault.

```xml
<!-- HashiCorp Vault -->
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-spring-boot-vault-starter</artifactId>
</dependency>
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-crypto-vault-starter</artifactId>
</dependency>
```

```xml
<!-- AWS KMS (alternative to Vault) -->
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-crypto-aws-kms-starter</artifactId>
</dependency>
```

The version is managed by the jEAP Spring Boot parent. jEAP Crypto with Vault requires the
**Transit secrets engine** to be enabled on the Vault instance.

## 2. Configure a wrapping key

Each wrapping key gets a logical name under `keys.<keyName>`. The example configures one Vault key
named `myDb`. See [Configuration reference](configuration.md) for AWS KMS and all options.

```yaml
jeap:
  vault:
    system-name: "testapp"
  crypto:
    vault:
      # default-secret-engine-path defaults to "transit/${jeap.vault.system-name}"
      keys:
        myDb:
          key-name: "testapp-my-database-key"   # name of the wrapping key in Vault
```

## 3. Encrypt and decrypt

For each configured wrapping key a `CryptoService` bean named `<keyName>CryptoService` is registered.
With a single key you can inject by type; with several keys use a `@Qualifier`.

```java
@Component
@RequiredArgsConstructor
class DocumentStore {
    @Qualifier("myDbCryptoService")
    private final CryptoService cryptoService;

    void store(byte[] plaintext) {
        byte[] container = cryptoService.encrypt(plaintext);   // self-describing crypto container
        storage.store(container);
    }

    byte[] load(String path) {
        byte[] container = storage.read(path);
        return cryptoService.decrypt(container);
    }
}
```

The returned container holds the wrapped data key, the ciphertext and metadata (such as the nonce).
With the `CryptoService` the wrapping key is taken from configuration and not stored in the container —
see [Crypto APIs](crypto-api.md) for `KeyIdCryptoService` and `KeyReferenceCryptoService`, which select
the key at runtime.

## 4. Disable encryption in tests

Set `jeap.crypto.disabledForTestEnv: true` to make `encrypt`/`decrypt` pass the input through
unchanged, so no KMS instance is needed in tests. This is rejected when the `abn` or `prod` Spring
profile is active.

## Related

- [Architecture](architecture.md)
- [Configuration reference](configuration.md)
- [Crypto APIs](crypto-api.md)
- [Key management](key-management.md)
- [jeap-crypto](../README.md)
