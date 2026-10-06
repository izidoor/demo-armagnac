#!/usr/bin/env bash
# Recrée la base de développement (ADR-005) : à lancer après chaque modification de V1__schema.sql.
set -euo pipefail
compose="$(dirname "$0")/backend/compose.yaml"
docker compose -f "$compose" down -v
docker compose -f "$compose" up -d
