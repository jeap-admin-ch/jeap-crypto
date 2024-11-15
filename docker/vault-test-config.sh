#!/bin/sh

# Make sure vault is started
while ! nc -z vault-server 8200 ; do sleep 1 ; done

set -exo pipefail

unset http_proxy
unset HTTP_PROXY
unset https_proxy
unset HTTPS_PROXY

export VAULT_ADDR=http://vault-server:8200

vault login secret

vault audit enable file file_path=stdout

# Enable transit secrets engine at transit/jeap and create keys for integration tests
vault secrets enable -path=transit/jeap transit
vault write -f transit/jeap/keys/testapp-encryption-key
vault write -f transit/jeap/keys/testapp-database-key

# Enable transit secrets engine at transit/otherapp and create a key for integration tests
vault secrets enable -path=transit/otherapp transit
vault write -f transit/otherapp/keys/otherapp-s3-key

# Enable approle auth method
vault auth enable -path=approle/jeap approle

# Create policy 'jeap-crypto-policy' for the approle with path restriction
SCRIPT_DIR=`dirname $0`
vault policy write jeap-crypto-policy ${SCRIPT_DIR}/jeap-crypto-policies.hcl

# Create approle for jeap-crypto, assign the jeap-crypto-policy policy
APPROLE_PATH=auth/approle/jeap/role/jeap-crypto
vault write ${APPROLE_PATH} \
   bind_secret_id=true \
   token_policies=jeap-crypto-policy

# Log approle
vault read ${APPROLE_PATH}

# Set fixed role-id for local tests
ROLE_ID=9999-8888-7777
vault write ${APPROLE_PATH}/role-id \
  role_id="${ROLE_ID}"

# Set fixed secret-id for local tests
SECRET_ID=1234-5678-9012-3456
vault write ${APPROLE_PATH}/custom-secret-id \
  secret_id="${SECRET_ID}"
