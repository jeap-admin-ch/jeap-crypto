# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.10.1] - 2025-02-12

### Changed

- Improve platform support (ex. aarch64) by providing Corretto Crypto Provider implementations for all available platforms
- Make Corretto Crypto Provider optional at runtime

## [3.10.0] - 2025-02-10

### Changed

- Update parent from 5.5.0 to 5.5.1

## [3.9.0] - 2025-02-07

### Changed

- Update parent from 5.4.1 to 5.5.0
- Maven central publication

## [3.8.0] - 2024-12-31

### Changed

- Update parent from 5.4.0 to 5.4.1

## [3.7.0] - 2024-12-17

### Changed

- upgrade spring boot to version 3.4.0 

## [3.6.1] - 2024-12-17

### Added

- credential scan (trufflehog)

## [3.6.0] - 2024-12-09

### Changed

- Configure trivy scan for all branches

## [3.5.0] - 2024-12-06

### Changed

- Update parent from 5.2.5 to 5.3.0
- Prepare repository for Open Source distribution

## [3.4.0] - 2024-11-12

### Changed

- Prepare repository for Open Source distribution

## [3.3.0] - 2024-11-07

### Changed

- Update parent from 5.2.0 to 5.2.1

## [3.2.1] - 2024-11-05

### Changed

- Add license definition & plugins

## [3.2.0] - 2024-10-31

### Changed

- Update parent from 5.1.0 to 5.1.1

## [3.1.0] - 2024-09-20

### Changed

- Update parent from 5.0.0 to 5.1.0

## [3.0.0] - 2024-09-06

### Changed

- Update parent from 4.11.1 to 5.0.0 (java 21)
- Update jeap-spring-boot-vault-starter to version 17.0.0 (java 21)

## [2.14.0] - 2024-08-21

### Changed

- Update parent from 4.10.0 to 4.11.1

## [2.13.0] - 2024-07-18

### Changed

- Update jeap-spring-boot-vault-starter to version 16.0.0

## [2.12.0] - 2024-07-15

### Changed

- Update parent from 4.8.3 to 4.10.0

## [2.11.0] - 2024-05-03

### Changed

- Update parent from 4.8.2 to 4.8.3

## [2.10.0] - 2024-03-27

### Changed

- Update parent from 4.8.0 to 4.8.2

## [2.9.2] - 2024-03-11

- Only require escrow key for AWS KMS at startup when keys are configured
- Add exclusion for apache HTTP client to s3 module

## [2.9.1] - 2024-03-05

- Add metrics for key usage

## [2.9.0] - 2024-03-04

- Upgraded to jeap internal parent 4.8.0 (Spring Boot 3.2.3)

## [2.8.0] - 2024-02-21

- Add escrow key support for AWS KMS

## [2.7.0] - 2024-02-14

- Add AWS KMS starter

## [2.6.0] - 2024-02-13

- Add AWS KMS support

## [2.5.0] - 2024-02-09

- Add multi-key format
- Internal refactoring in preparation of multiple KMS

## [2.4.0] - 2024-01-25

- Upgraded jeap-internal-spring-boot-parent from 4.4.1 to 4.5.0 (spring boot 3.2.2)

## [2.3.0] - 16.01.2024

- Upgrade internal parent to 4.4.0

## [2.2.0] - 08.01.2024

- Upgrade internal parent to 4.3.2
- Upgrade jeap-spring-boot-vault-starter from 13.0.0 to 14.0.0 (no bootstrap)

## [2.1.0] - 13.12.2023

- Upgrade internal parent to 4.3.0 (spring boot 3.2)

## [2.0.1] - 11.12.2023

- Fixed: JeapCryptoStringConverter supports encryption/decryption of empty string

## [2.0.0] - 16.08.2023

- Upgrade internal parent to 4.0.0 (spring boot 3.1)

## [1.4.1] - 18.07.2023

-  Added trigger for automatic upgrade of jeap-spring-boot-parent

## [1.4.0] - 11.07.2023

- Upgrade to AWS SDK v2

## [1.3.1] - 23.06.2023

- Update to internal parent 3.5.0
- Align ConditionalOnProperty annotation with spring-cloud-starter-vault

## [1.3.0] - 11.05.2023

- Adding jeap-crypto-db and the converter JeapCryptoStringConverter to encrypt / decrypt String attributes in entities.

## [1.2.0] - 02.05.2023

- Adding the KeyIdCryptoService interface to jeap-crypto-core and an implementation to jeap-crypto-vault-starter.

## [1.1.0] - 24.04.2023

- Application property `jeap.crypto.disabledForTestEnv=true` allows to disable encryption for local, dev and ref environments

## [1.0.1] - 20.04.2023

- Upgrade internal parent to 3.4.1 (spring boot 2.7.11)

## [1.0.0] - 16.03.2023

- Initial release
