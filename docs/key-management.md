# Key management

jEAP Crypto wraps data keys with a long-lived **wrapping key** held in a Key Management Service (KMS).
Two backends are supported, selected by the starter on the classpath. The wrapping key never leaves
the KMS.

## Backends

| KMS            | Starter                        | Key reference / location                                   |
|----------------|--------------------------------|------------------------------------------------------------|
| HashiCorp Vault| `jeap-crypto-vault-starter`    | Transit secrets engine; reference = secret-engine path + key name |
| AWS KMS        | `jeap-crypto-aws-kms-starter`  | reference = key ARN, alias, alias ARN or key id            |

Vault requires the **Transit secrets engine** to be enabled. With a managed Vault (e.g. Managed
Application Vault), confirm with the provider that Transit is available. The Vault starter expects a
configured Spring Vault connection, usually provided by the `jeap-spring-boot-vault-starter`.

For configuration properties of both backends see the [Configuration reference](configuration.md).

## Data-key caching

Requesting a fresh data key from the KMS for every value creates load on the KMS and adds a round-trip
to each operation. jEAP Crypto therefore caches data keys, separately for encryption and decryption,
**per wrapping key**:

- **Encryption cache** — the same data key is reused for a configurable duration, so it encrypts
  several values. Tuned via `default-encryption-key-cache-expiry-duration`, `encryption-key-max-cache-size`
  and per-key `encryption-cache-expiry-duration`.
- **Decryption cache** — unwrapped data keys are kept in memory for a configurable duration; a cache
  hit avoids a KMS round-trip. Tuned via `default-decryption-key-cache-expiry-duration`,
  `decryption-key-max-cache-size` and per-key `decryption-cache-expiry-duration`.

A max cache size of `0` disables the respective cache. Caching is a trade-off between security and
performance: reusing a data key means more data is compromised if that key is, and keys held in memory
are a potential attack target. Change the defaults only with a good understanding of the use case. A
longer encryption-key retention typically improves the decryption-cache hit rate.

## Escrow encryption (AWS KMS)

With AWS KMS a **default escrow key must be configured** by default (unless explicitly disabled with
`key-type: NONE`, or encryption is disabled). The escrow key guarantees that data can still be
decrypted even if the cloud KMS is unavailable or the key in the KMS is lost.

The data key is then encrypted **twice**: once by the KMS, and once by the application itself using the
**public** key of the escrow key pair (asymmetric encryption). The application only holds the public
key. The algorithm is RSA with a key length of 4096 bit (`RSA_4096`). The extra wrapped data key is
stored in the [multi-key container format](data-format.md). An escrow key can be set globally via
`default-escrow-key` and overridden per key via `keys.<keyName>.escrow-key`.

## Metrics

If the application provides a Micrometer `MeterRegistry` bean (e.g. by adding the
`jeap-spring-boot-monitoring-starter`), the library exposes:

| Metric                    | Tags                              | Meaning                                                       |
|---------------------------|-----------------------------------|--------------------------------------------------------------|
| `jeap_crypto_key_encrypt` | `escrow=true\|false`, `key=<ref>` | Counter: times a key is used for encryption (ignoring caching) |
| `jeap_crypto_key_decrypt` | `key=<ref>`                       | Counter: times a key is used for decryption (ignoring caching) |
| `jeap_crypto_noop`        | —                                 | `0`/`1` = jeap-crypto enabled / disabled for the environment   |
| `cache_size`              | cache `jeap-crypto-encryption-keys` / `jeap-crypto-decryption-keys` | Current number of data keys in the cache |
| `cache_gets_total`        | same caches, `result=hit\|miss`   | Cache hits (key reuse) and misses (new key fetched from KMS)  |

## Related

- [Architecture](architecture.md)
- [Configuration reference](configuration.md)
- [Crypto APIs](crypto-api.md)
- [Binary container format](data-format.md)
- [jeap-crypto](../README.md)
