# AGENTS.md

Guidance for AI coding agents working **in this repository**. For how to *use* the library in a
consuming service, read [README.md](README.md) and the [docs/](docs/) folder instead.

## Project

jEAP Crypto is a multi-module Maven library for client-side encryption of data **at-rest** in Spring
Boot applications. It encrypts byte arrays with AES-256-GCM using short-lived data keys, which are
themselves wrapped/unwrapped by a Key Management Service (KMS) so the wrapping key never leaves the
KMS. It defines a self-describing binary container format, supports HashiCorp Vault (Transit secrets
engine) and AWS KMS as backends, caches data keys to reduce KMS round-trips, and offers escrow
encryption for disaster recovery. Spring Boot starters auto-configure a `CryptoService` bean per
configured wrapping key.

## Repository layout

```
pom.xml                          # Parent POM (packaging=pom); declares the modules below
jeap-crypto-core/                # Crypto APIs (ch.admin.bit.jeap.crypto.api), AES-GCM engine,
                                 #   container data formats, escrow, key-management abstractions
jeap-crypto-vault/               # Vault Transit KeyManagementService implementation
jeap-crypto-aws-kms/             # AWS KMS KeyManagementService implementation + escrow encryption
jeap-crypto-spring/              # Common @AutoConfiguration, per-key bean registration, metrics
jeap-crypto-vault-starter/       # Spring Boot starter for Vault
jeap-crypto-aws-kms-starter/     # Spring Boot starter for AWS KMS
jeap-crypto-db/                  # JeapCryptoStringConverter (JPA AttributeConverter)
jeap-crypto-s3/                  # JeapCryptoS3Template
jeap-crypto-multi-kms-it/        # Integration tests across multiple KMS backends
docker/, Jenkinsfile, publiccode.yml, CHANGELOG.md, LICENSE
```

Dependency direction: `core` ← (`vault` | `aws-kms`) ← `spring` ← (`vault-starter` | `aws-kms-starter`).
The `db` and `s3` helper modules build on the `core` APIs. The `api` package
(`ch.admin.bit.jeap.crypto.api`) is the public surface; everything under
`ch.admin.bit.jeap.crypto.internal.*` is implementation detail.

## Build & test

```bash
./mvnw -pl <module> -am install      # build a module and its dependencies
./mvnw verify                        # full build incl. tests
./mvnw -pl jeap-crypto-multi-kms-it test
```

- Parent: `ch.admin.bit.jeap:jeap-internal-spring-boot-parent` (Spring Boot 4 aligned).
- Integration tests use Testcontainers (Vault, LocalStack for AWS KMS).
- Spring Boot 3 maintenance happens on the `release/springboot3` branch; `master` targets Spring Boot 4.

## jEAP conventions

- Java packages live under `ch.admin.bit.jeap.crypto.*`.
- Configuration properties use the prefixes `jeap.crypto.vault.*`, `jeap.crypto.awskms.*` and
  `jeap.crypto.db.*`; `jeap.crypto.disabledForTestEnv` disables the engine for tests.
- A `CryptoService` bean is registered per configured wrapping key; the bean name is
  `<keyName>CryptoService` (camel-cased key name + `CryptoService`), see `BeanNames`.
- Auto-configuration is registered via `@AutoConfiguration` and
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- Never log or persist plaintext data keys; only the KMS-wrapped data key is written into the container.
- The binary container format is versioned by a leading format-id byte (`01`, `02`, `03`); changing a
  format on the wire breaks decryption of already-persisted data.

## Docs

When changing public behaviour, update the matching focused file under [docs/](docs/) (one topic per
file) and the documentation index in the README.

## Versioning

- Semantic Versioning; all changes documented in [CHANGELOG.md](./CHANGELOG.md) (Keep a Changelog format).
- `setPomVersions.sh` updates the version across all module POMs.
- When working on a feature branch, increase the version to `x.y.z-SNAPSHOT` in the POMs.
- Always keep the -SNAPSHOT postfix in the POMs, CI will remove it when releasing a version. Do not use
  the SNAPSHOT postfix in other places (CHANGELOG, publiccode.yml etc).
- Keep changelog entries concise and follow existing patterns.
- Keep commit messages short, use the JIRA ID from the branch name as a prefix, do not use conventional
  commits (for example: "JEAP-1234 Added feature X").
- When bumping the version, also update the changelog, and update version/date in `publiccode.yml`.
