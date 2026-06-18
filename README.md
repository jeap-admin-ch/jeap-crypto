# jEAP Crypto

jEAP Crypto is a library that provides client-side encryption for data **at-rest** in Spring Boot
applications, and is part of the jEAP project. It performs the actual encryption in the application
(client-side) and uses a Key Management Service (KMS) only to wrap/unwrap short-lived data keys, so
the wrapping key never leaves the KMS. Encryption is based on AES-256-GCM. The library is independent
of the persistence technology and provides:

* `CryptoService`, `KeyReferenceCryptoService` and `KeyIdCryptoService` APIs to encrypt/decrypt byte arrays
* A self-describing binary container format that stores the encrypted data key together with the ciphertext
* KMS backends for HashiCorp Vault (Transit secrets engine) and AWS KMS, wired up via Spring Boot starters
* Data-key caching (per wrapping key) to reduce KMS round-trips, with Micrometer metrics
* Escrow encryption of data keys (AWS KMS) for disaster recovery without the KMS
* Ready-made helpers for encrypted database columns (JPA `AttributeConverter`) and S3 objects

## Documentation

Start with [Getting started](docs/getting-started.md), then follow the links below.

| Topic                                                       | File                                                       |
|-------------------------------------------------------------|------------------------------------------------------------|
| Getting started (add the starter, encrypt & decrypt)        | [docs/getting-started.md](docs/getting-started.md)         |
| Architecture & encryption flow                              | [docs/architecture.md](docs/architecture.md)               |
| Configuration reference (`jeap.crypto.*`)                   | [docs/configuration.md](docs/configuration.md)             |
| Crypto APIs (`CryptoService` / `KeyId` / `KeyReference`)    | [docs/crypto-api.md](docs/crypto-api.md)                   |
| Key management (Vault & AWS KMS, escrow, caching, metrics)  | [docs/key-management.md](docs/key-management.md)           |
| Encrypted database columns                                  | [docs/database-columns.md](docs/database-columns.md)       |
| S3 object storage                                           | [docs/s3-object-storage.md](docs/s3-object-storage.md)     |
| Binary container format                                     | [docs/data-format.md](docs/data-format.md)                 |

## Modules

The group id for all modules is `ch.admin.bit.jeap`; the version is managed by the jEAP Spring Boot
parent. A consuming service depends on one of the starters (plus optional helper modules).

| Module                          | Purpose                                                                              |
|---------------------------------|--------------------------------------------------------------------------------------|
| `jeap-crypto-core`              | Crypto APIs, AES-GCM engine, container format, escrow and key-management abstractions |
| `jeap-crypto-vault`             | HashiCorp Vault Transit key-management implementation                                 |
| `jeap-crypto-aws-kms`           | AWS KMS key-management implementation (incl. escrow encryption)                       |
| `jeap-crypto-spring`            | Common Spring Boot auto-configuration, bean registration and metrics                 |
| `jeap-crypto-vault-starter`     | Spring Boot starter wiring jEAP Crypto with Vault                                     |
| `jeap-crypto-aws-kms-starter`   | Spring Boot starter wiring jEAP Crypto with AWS KMS                                   |
| `jeap-crypto-db`                | JPA `AttributeConverter` for transparently encrypting database columns               |
| `jeap-crypto-s3`                | `JeapCryptoS3Template` for encrypting/decrypting S3 objects                           |

## Changes
This library is versioned using [Semantic Versioning](http://semver.org/) and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/).

## Note

This repository is part the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
