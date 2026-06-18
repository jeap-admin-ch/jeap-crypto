# Configuration reference

All properties use the prefix `jeap.crypto`. The KMS-specific properties live under
`jeap.crypto.vault.*` (HashiCorp Vault) or `jeap.crypto.awskms.*` (AWS KMS). Durations accept ISO-8601
(`PT1H`) or a value with a unit (`1h`, `30m`).

For data-key caching, escrow encryption and metrics see [Key management](key-management.md).

## Common

| Property                         | Mandatory | Default | Description                                                                                                          |
|----------------------------------|-----------|---------|----------------------------------------------------------------------------------------------------------------------|
| `jeap.crypto.disabledForTestEnv` | No        | `false` | Disable the crypto engine. `encrypt`/`decrypt` then return the input unchanged. Rejected for the `abn`/`prod` profiles |

## Vault (`jeap.crypto.vault`)

| Property                                            | Mandatory | Default                               | Description                                                                       |
|-----------------------------------------------------|-----------|---------------------------------------|-----------------------------------------------------------------------------------|
| `default-secret-engine-path`                        | No        | `transit/${jeap.vault.system-name}`   | Path of the Vault Transit secrets engine used to generate data keys               |
| `default-encryption-key-cache-expiry-duration`      | No        | `1h`                                  | How long data keys are reused for **encryption**                                  |
| `default-decryption-key-cache-expiry-duration`      | No        | `6h`                                  | How long data keys are cached for **decryption**                                  |
| `encryption-key-max-cache-size`                     | No        | `100`                                 | Max number of encryption data keys cached. `0` disables the encryption cache      |
| `decryption-key-max-cache-size`                     | No        | `50000`                               | Max number of decryption data keys cached. `0` disables the decryption cache      |
| `keys.<keyName>`                                    | Yes       | —                                     | Logical name of a wrapping key. The `CryptoService` bean name is `<keyName>CryptoService` |
| `keys.<keyName>.key-name`                           | Yes       | —                                     | Name of the wrapping key in the Vault Transit secrets engine                      |
| `keys.<keyName>.secret-engine-path`                 | No        | `default-secret-engine-path`          | Override the secret-engine path for this wrapping key                             |
| `keys.<keyName>.encryption-cache-expiry-duration`   | No        | the `default-...` value               | Override the encryption cache duration for this key. `0` disables caching         |
| `keys.<keyName>.decryption-cache-expiry-duration`   | No        | the `default-...` value               | Override the decryption cache duration for this key. `0` disables caching         |

```yaml
jeap:
  vault:
    system-name: "testapp"
  crypto:
    vault:
      keys:
        myDb:
          key-name: "testapp-my-database-key"
          encryption-cache-expiry-duration: 2h
          decryption-cache-expiry-duration: 12h
        myObjectStore:
          secret-engine-path: "otherapp/transit"
          key-name: "otherapp-my-objectstore-key"
```

## AWS KMS (`jeap.crypto.awskms`)

| Property                                            | Mandatory | Default                  | Description                                                                                  |
|-----------------------------------------------------|-----------|--------------------------|----------------------------------------------------------------------------------------------|
| `region`                                            | Yes\*     | env var `AWS_REGION`     | AWS region for KMS. Required if `AWS_REGION` is not set                                       |
| `endpoint`                                          | No        | —                        | Override the KMS endpoint URI (e.g. for LocalStack)                                           |
| `default-escrow-key.key-type`                       | No        | `RSA_4096`               | Escrow key type: `RSA_4096` or `NONE`                                                         |
| `default-escrow-key.public-key`                     | Yes\*     | —                        | X.509 PEM public key of the escrow key. Not required if the escrow `key-type` is `NONE`       |
| `default-encryption-key-cache-expiry-duration`      | No        | `1h`                     | How long data keys are reused for **encryption**                                              |
| `default-decryption-key-cache-expiry-duration`      | No        | `6h`                     | How long data keys are cached for **decryption**                                              |
| `encryption-key-max-cache-size`                     | No        | `100`                    | Max number of encryption data keys cached. `0` disables the encryption cache                  |
| `decryption-key-max-cache-size`                     | No        | `50000`                  | Max number of decryption data keys cached. `0` disables the decryption cache                  |
| `keys.<keyName>`                                    | Yes       | —                        | Logical name of a wrapping key. The `CryptoService` bean name is `<keyName>CryptoService`      |
| `keys.<keyName>.key-arn`                            | Yes       | —                        | AWS KMS key ARN, alias, alias ARN or key id                                                   |
| `keys.<keyName>.encryption-cache-expiry-duration`   | No        | the `default-...` value  | Override the encryption cache duration for this key. `0` disables caching                     |
| `keys.<keyName>.decryption-cache-expiry-duration`   | No        | the `default-...` value  | Override the decryption cache duration for this key. `0` disables caching                     |
| `keys.<keyName>.escrow-key.key-type`                | No        | `default-escrow-key`     | Escrow key type for this key, if deviating from the default                                   |
| `keys.<keyName>.escrow-key.public-key`              | No        | `default-escrow-key`     | Escrow public key for this key, if deviating from the default                                 |

\* By default a **default escrow key must be configured** for AWS KMS unless `key-type` is `NONE` or
encryption is disabled — see [Key management](key-management.md#escrow-encryption-aws-kms).

```yaml
jeap:
  crypto:
    awskms:
      region: eu-central-2
      default-escrow-key:
        key-type: RSA_4096
        public-key: |
          -----BEGIN PUBLIC KEY-----
          MIICIjANBgkqh...==
          -----END PUBLIC KEY-----
      keys:
        myDb:
          key-arn: "${test-key-arn}"
          encryption-cache-expiry-duration: 2h
          decryption-cache-expiry-duration: 12h
        myObjectStore:
          key-arn: "${test-key-arn}"
```

## Database columns (`jeap.crypto.db`)

| Property            | Mandatory | Default | Description                                                                  |
|---------------------|-----------|---------|------------------------------------------------------------------------------|
| `jeap.crypto.db.key-id` | Yes\* | —    | Logical key id used by `JeapCryptoStringConverter`. \*Required only when the converter is used |

See [Encrypted database columns](database-columns.md).

## Related

- [Getting started](getting-started.md)
- [Key management](key-management.md)
- [Crypto APIs](crypto-api.md)
- [jeap-crypto](../README.md)
