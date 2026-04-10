#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CERTS_DIR="$SCRIPT_DIR/certs"

rm -rf "$CERTS_DIR"
mkdir -p "$CERTS_DIR"


docker run --rm -v "$CERTS_DIR:/certs" alpine sh -c '
apk add --no-cache openssl > /dev/null 2>&1
set -e
PASSWORD="changeit"

openssl req -x509 -newkey rsa:2048 -days 365 -nodes \
  -keyout /certs/ca-key.pem \
  -out /certs/ca-cert.pem \
  -subj "/CN=MicroservicesCA"

cat > /certs/san.cnf <<SANEOF
[req]
distinguished_name = req_dn
req_extensions = v3_req
prompt = no

[req_dn]
CN = localhost

[v3_req]
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
DNS.2 = api-gateway
DNS.3 = auth-service
DNS.4 = user-service
DNS.5 = property-service
DNS.6 = client-frontend
DNS.7 = owner-frontend
DNS.8 = minio
IP.1 = 127.0.0.1
SANEOF

openssl req -newkey rsa:2048 -nodes \
  -keyout /certs/server-key.pem \
  -out /certs/server-req.pem \
  -config /certs/san.cnf

openssl x509 -req -days 365 \
  -in /certs/server-req.pem \
  -CA /certs/ca-cert.pem \
  -CAkey /certs/ca-key.pem \
  -CAcreateserial \
  -out /certs/server-cert.pem \
  -extfile /certs/san.cnf \
  -extensions v3_req

openssl pkcs12 -export \
  -in /certs/server-cert.pem \
  -inkey /certs/server-key.pem \
  -out /certs/keystore.p12 \
  -name server \
  -password "pass:$PASSWORD"

openssl pkcs12 -export \
  -nokeys \
  -in /certs/ca-cert.pem \
  -out /certs/truststore.p12 \
  -password "pass:$PASSWORD"
'

echo "Certificates generated in $CERTS_DIR"
