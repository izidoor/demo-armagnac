#!/usr/bin/env bash
# Démarrage sur Scalingo (Procfile, docs/deploiement-scalingo.md).
# L'addon PostgreSQL donne SCALINGO_POSTGRESQL_URL (postgres://utilisateur:mot-de-passe@hote:port/base?…),
# le profil prod attend une URL JDBC et des identifiants séparés. Arguments en plus : passés à l'application
# (remise à zéro de la démonstration, scalingo-reset.sh).
set -euo pipefail

url=${SCALINGO_POSTGRESQL_URL#postgres://}
url=${url#postgresql://}
identifiants=${url%%@*}
export BNIA_DB_USER=${identifiants%%:*}
export BNIA_DB_PASSWORD=${identifiants#*:}
export BNIA_DB_URL="jdbc:postgresql://${url#*@}"

exec java ${JAVA_OPTS:-} -jar backend/bnia-boot/target/bnia-boot-*.jar \
    --spring.profiles.active="${BNIA_PROFILS:-prod,demo}" "$@"
