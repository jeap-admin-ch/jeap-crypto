# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [5.11.0] - 2026-01-20
### Changed
- update jeap-spring-boot-vault-starter from 19.10.0 to 19.11.0
- Default server.forward-headers-strategy to NATIVE

## [5.10.0] - 2026-01-16
### Changed
  Enable via the `jeap.health.metric.contributor-metrics.enabled` property.
- update jeap-spring-boot-vault-starter from 19.9.0 to 19.10.0
- Added support for exposing additional metrics about application health contributors.

## [5.9.0] - 2026-01-14

### Changed
- Update parent from 5.16.8 to 5.17.0
- update jeap-spring-boot-vault-starter from 19.8.0 to 19.9.0
- update testcontainers from 1.21.3 to 1.21.4

## [5.8.0] - 2026-01-07

### Changed
- Update parent from 5.16.7 to 5.16.8
- update jeap-spring-boot-vault-starter from 19.7.0 to 19.8.0

## [5.7.0] - 2025-12-22

### Changed
- Update parent from 5.16.6 to 5.16.7
- update jeap-spring-boot-vault-starter from 19.6.0 to 19.7.0

## [5.6.0] - 2025-12-19

### Changed
- Update parent from 5.16.5 to 5.16.6
- update jeap-spring-boot-vault-starter from 19.5.0 to 19.6.0

## [5.5.0] - 2025-12-17

### Changed
- Update parent from 5.16.4 to 5.16.5
- update jeap-spring-boot-vault-starter from 19.4.1 to 19.5.0

## [5.4.1] - 2025-12-16
### Changed
- update jeap-spring-boot-vault-starter from 19.4.0 to 19.4.1
- Fix logback warnings due to deprecated features being used in the configuration

## [5.4.0] - 2025-12-15

### Changed
- Update parent from 5.16.3 to 5.16.4
- update jeap-spring-boot-vault-starter from 19.3.0 to 19.4.0

## [5.3.0] - 2025-12-08

### Changed
- Update parent from 5.16.2 to 5.16.3
- update jeap-spring-boot-vault-starter from 19.2.0 to 19.3.0

## [5.2.0] - 2025-12-08

### Changed
- Update parent from 5.16.1 to 5.16.2
- update jeap-spring-boot-vault-starter from 19.1.0 to 19.2.0

## [5.1.0] - 2025-12-04

### Changed
- Update parent from 5.16.0 to 5.16.1
- update jeap-spring-boot-vault-starter from 19.0.0 to 19.1.0

## [5.0.0] - 2025-12-03
### Changed
- update jeap-spring-boot-vault-starter from 18.5.0 to 19.0.0
-  Breaking Change
    - **Removed**
      - jeap-spring-boot-cloud-autoconfig-starter
      - jeap-spring-boot-config-starter
      - other cloudfoundry specifics


## [4.5.0] - 2025-11-28

### Changed
- Update parent from 5.15.1 to 5.16.0
- update jeap-spring-boot-vault-starter from 18.4.0 to 18.5.0

## [4.4.0] - 2025-11-14
### Changed
- update jeap-spring-boot-vault-starter from 18.3.0 to 18.4.0
- Update aws-advanced-jdbc-wrapper from 2.5.4 to 2.6.6


## [4.3.0] - 2025-11-12

### Changed
- Update parent from 5.15.0 to 5.15.1
- update jeap-spring-boot-vault-starter from 18.2.0 to 18.3.0

## [4.2.0] - 2025-10-02

### Changed
- Update parent from 5.14.0 to 5.15.0
- updated springdoc-openapi from 2.8.9 to 2.8.13
- update jeap-spring-boot-vault-starter from 18.1.0 to 18.2.0
- updated java-cfenv from 3.4.0 to 3.5.0

## [4.1.0] - 2025-09-19

### Changed
- Update parent from 5.13.0 to 5.14.0
- update jeap-spring-boot-vault-starter from 18.0.0 to 18.1.0

## [4.0.0] - 2025-09-02
### Changed
- update jeap-spring-boot-vault-starter from 17.43.0 to 18.0.0
- Support for the Spring Cloud bootstrap context mechanism has been removed. Use the spring.config.import mechanism
  instead for your (external) microservice configuration. 


## [3.28.0] - 2025-09-01
### Changed
- update jeap-spring-boot-vault-starter from 17.42.0 to 17.43.0
- The OAuth 2.0 client-related code has been extracted into its own starter, which is imported by the
  jeap-spring-boot-security-starter to maintain backward compatibility.


## [3.27.0] - 2025-08-26

### Changed
- Update parent from 5.12.1 to 5.13.0
- update jeap-spring-boot-vault-starter from 17.41.0 to 17.42.0

## [3.26.0] - 2025-08-14

### Changed
- Update parent from 5.12.0 to 5.12.1
- update jeap-spring-boot-vault-starter from 17.40.1 to 17.41.0

## [3.25.1] - 2025-08-08
### Changed
- update jeap-spring-boot-vault-starter from 17.40.0 to 17.40.1
- Make feature-policy header configurable in jeap-spring-boot-web-config-starter


## [3.25.0] - 2025-08-05

### Changed
- Update parent from 5.11.0 to 5.12.0
- updated springdoc-openapi from 2.8.6 to 2.8.9
- updated wiremock from 3.12.1 to 3.13.1
- update jeap-spring-boot-vault-starter from 17.39.3 to 17.40.0
- updated logstash from 8.0 to 8.1

## [3.24.3] - 2025-07-09
### Changed
- update jeap-spring-boot-vault-starter from 17.39.2 to 17.39.3
- switch from deprecated org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration to org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration


## [3.24.2] - 2025-07-09
### Changed
- update jeap-spring-boot-vault-starter from 17.39.1 to 17.39.2
- ServletRequestSecurityTracer now properly handles non-REST requests (e.g., SOAP) by falling back to the request URI when the REST HandlerMapping pattern is not available.


## [3.24.1] - 2025-07-07
### Changed
- update jeap-spring-boot-vault-starter from 17.39.0 to 17.39.1
- Make sure JeapPostgreSQLAWSDataSourceAutoConfig is evaluated before Spring's DataSourceAutoConfiguration to avoid
  DataSource bean conflicts.


## [3.24.0] - 2025-07-04

### Changed
- Update parent from 5.10.2 to 5.11.0
- update jeap-spring-boot-vault-starter from 17.38.0 to 17.39.0
- update testcontainers from 1.21.0 to 1.21.3
- update guava-testlib from 31.1-jre to 33.4.8-jre

## [3.23.0] - 2025-06-18

### Changed
- Update parent from 5.10.1 to 5.10.2
- update jeap-spring-boot-vault-starter.version from 17.37.0 to 17.38.0

## [3.22.1] - 2025-06-17

### Changed
- Update because to upload (central-publish) didn't work properly

## [3.22.0] - 2025-06-17

### Changed
- Update parent from 5.10.0 to 5.10.1
- update jeap-spring-boot-vault-starter.version from 17.36.0 to 17.37.0

## [3.21.0] - 2025-06-13

### Changed
- Update parent from 5.9.0 to 5.10.0
- update jeap-spring-boot-vault-starter.version from 17.35.0 to 17.36.0

## [3.20.0] - 2025-06-12
### Changed
- update jeap-spring-boot-vault-starter.version from 17.34.0 to 17.35.0
- security-starter-test: removed spring-security-rsa dependency as its functionality is now included in spring-security


## [3.19.0] - 2025-06-05
### Changed
- update jeap-spring-boot-vault-starter.version from 17.32.0 to 17.34.0
- Update parent from 5.8.1 to 5.9.0

## [3.18.0] - 2025-06-04

### Changed

- Update parent from 5.8.1 to 5.9.0

### Bugfix

- Project Name now required for uploads to Maven Central

## [3.17.0] - 2025-05-26
### Changed

- update jeap-spring-boot-vault-starter.version from 17.25.0 to 17.32.0
- Update parent from 5.8.0 to 5.8.1

## [3.16.0] - 2025-05-09

### Changed

- Support getting encrypted S3 objects by version ID in the crypto S3 template

## [3.15.0] - 2025-04-30

### Changed

- Update parent from 5.7.0 to 5.8.0

## [3.14.0] - 2025-03-27

### Changed

- Update parent from 5.6.0 to 5.7.0
- update jeap-spring-boot-vault-starter from 17.21.0 to 17.25.0
- update testcontainers from 1.19.8 to 1.20.6

## [3.13.0] - 2025-03-06

### Changed

- Removed conflicting versions of the Amazon Corretto Crypto Provider (ACCP) that could lead to the ACCP being disabled

## [3.12.0] - 2025-03-06

### Changed

- Update parent from 5.5.5 to 5.6.0
- update jeap-spring-boot-vault-starter from 17.19.0 to 17.21.0

## [3.11.0] - 2025-03-05

### Changed

- Update parent from 5.5.2 to 5.5.5

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
