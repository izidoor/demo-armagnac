#!/usr/bin/env bash
# Remise à zéro volontaire de la base de développement (ADR-005). Inutile après une modification de
# V1__schema.sql : l'application recrée alors la base au démarrage, en profil dev.
set -euo pipefail
compose="$(dirname "$0")/backend/compose.yaml"
docker compose -f "$compose" down -v
docker compose -f "$compose" up -d
