#!/usr/bin/env bash
# Ports propres à un worktree, pour le lancer à côté du dépôt principal (8080, 5173, 5432, 1025, 8025)
# et des autres worktrees : écrit backend/.env (lu par Docker Compose et par Spring Boot en profil dev)
# et frontend/.env.local (lu par Vite). Fichiers ignorés par git. Sans eux, les valeurs par défaut.
# À lancer depuis la racine du worktree ; sans effet si backend/.env existe déjà.
set -euo pipefail
racine="$(cd "$(dirname "$0")" && pwd)"
back="$racine/backend/.env"
front="$racine/frontend/.env.local"
nom="$(basename "$racine")"

if [[ -f "$back" ]]; then
  echo "Déjà configuré :" && cat "$back"
  exit 0
fi

# Décalages déjà pris par les autres worktrees.
pris=" $( (grep -hs "^BNIA_DECALAGE=" "$(dirname "$racine")"/*/backend/.env || true) | cut -d= -f2 | tr "\n" " ") "
libre() { ! ss -ltn 2>/dev/null | awk '{print $4}' | grep -qE "[:.]$1\$"; }

for n in $(seq 1 9); do
  [[ "$pris" == *" $n "* ]] && continue
  if libre $((8080 + n)) && libre $((5173 + n)) && libre $((5432 + n)) && libre $((1025 + n)) \
      && libre $((8025 + n)); then
    cat > "$back" <<EOF
# Instance du worktree $nom (worktree-env.sh). Ports : back $((8080 + n)), front $((5173 + n)).
BNIA_DECALAGE=$n
BNIA_COMPOSE_PROJECT=bnia-$nom
BNIA_DB_PORT=$((5432 + n))
BNIA_SMTP_PORT=$((1025 + n))
BNIA_MAILPIT_PORT=$((8025 + n))
SERVER_PORT=$((8080 + n))
BNIA_URL_PUBLIQUE=http://localhost:$((5173 + n))
EOF
    cat > "$front" <<EOF
# Instance du worktree $nom (worktree-env.sh).
BNIA_API_URL=http://localhost:$((8080 + n))
BNIA_FRONT_PORT=$((5173 + n))
EOF
    echo "Worktree $nom : back http://localhost:$((8080 + n)), front http://localhost:$((5173 + n)),"
    echo "Mailpit http://localhost:$((8025 + n)), base sur le port $((5432 + n)) (projet Compose bnia-$nom)."
    exit 0
  fi
done
echo "Aucun décalage libre de 1 à 9 : libérer un worktree ou écrire backend/.env à la main." >&2
exit 1
