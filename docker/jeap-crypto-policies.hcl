# See https://www.vaultproject.io/docs/concepts/policies for details

# Read and update permission on transit secrets engine for the system jeap
path "transit/jeap/*" {
  capabilities = [
    "read", "update"
  ]
}

# Read and update permission on another transit secrets engine for testing non-default configurations
path "transit/otherapp/*" {
  capabilities = [
    "read", "update"
  ]
}


# Read-only permission on secrets of the system jeap
path "secret/data/jeap/*" {
  capabilities = [
    "read"
  ]
}