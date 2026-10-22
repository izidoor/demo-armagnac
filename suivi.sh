#!/usr/bin/env bash
# Suivi de l'utilisation du prototype (docs/deploiement-scalingo.md, « Suivi de l'utilisation »).
#   ./suivi.sh [--local] [rapport] [depuis AAAA-MM-JJ]
# Rapports : tout (par défaut : personnes, visites, ecrans, offre, fichiers), personnes, visites (avec le parcours de
# chacune), ecrans, offre, fichiers (téléchargements, exports, imports), evenements, connexions.
# tableau : page de graphiques, écrite dans out/suivi-tableau.html (ignoré par git) et ouverte dans le navigateur.
# Instance hébergée : adresse et jeton dans .env.suivi, à la racine (ignoré par git) :
#   BNIA_SUIVI_URL=https://bnia-demo.osc-fr1.scalingo.io
#   BNIA_SUIVI_JETON=<valeur de BNIA_SUIVI_JETON sur Scalingo>
# Instance locale (profil dev, jeton « dev ») : avec --local, ou sans .env.suivi ; port de backend/.env s'il existe.
set -euo pipefail
cd "$(dirname "$0")"

local=false
if [[ "${1:-}" == "--local" ]]; then
    local=true
    shift
fi
if [[ "$local" == false && -f .env.suivi ]]; then
    # shellcheck disable=SC1091
    source .env.suivi
    : "${BNIA_SUIVI_URL:?BNIA_SUIVI_URL manque dans .env.suivi}"
    : "${BNIA_SUIVI_JETON:?BNIA_SUIVI_JETON manque dans .env.suivi}"
    echo "Instance hébergée (.env.suivi) : ${BNIA_SUIVI_URL}" >&2
else
    port=$(sed -n 's/^SERVER_PORT=//p' backend/.env 2>/dev/null || true)
    BNIA_SUIVI_URL="http://localhost:${port:-8080}"
    BNIA_SUIVI_JETON=dev
    echo "Instance locale : ${BNIA_SUIVI_URL}" >&2
fi

rapport="${1:-tout}"
depuis="${2:-}"

# Écrit le rapport brut (CSV « ; ») dans le fichier $2.
recuperer() {
    local url="${BNIA_SUIVI_URL%/}/api/suivi/export/$1${depuis:+?depuis=${depuis}}"
    local statut
    statut=$(curl -sS -o "$2" -w '%{http_code}' -H "Authorization: Bearer ${BNIA_SUIVI_JETON}" "$url") || {
        echo "Instance injoignable : ${BNIA_SUIVI_URL}" >&2
        exit 1
    }
    case "$statut" in
        200) ;;
        401) echo "Jeton refusé : comparer BNIA_SUIVI_JETON du serveur et de .env.suivi." >&2; exit 1 ;;
        404) echo "Export introuvable : rapport inconnu ($1), ou serveur sans jeton (BNIA_SUIVI_JETON absent," \
                  "ou back local démarré avant le suivi : le redémarrer)." >&2
             exit 1 ;;
        *) echo "Réponse ${statut} de ${url}" >&2; exit 1 ;;
    esac
}

exporter() {
    recuperer "$1" "$brut"
    column -t -s ';' < "$brut"
}

# Page autonome (tools/suivi-tableau.html, remplie des événements bruts en base64), ouverte dans le navigateur.
tableau() {
    local page=out/suivi-tableau.html donnees
    donnees=$(mktemp)
    recuperer evenements "$brut"
    base64 -w 76 < "$brut" > "$donnees"
    mkdir -p out
    awk -v donnees="$donnees" '$0 == "__DONNEES__" { while ((getline l < donnees) > 0) print l; next } { print }' \
        tools/suivi-tableau.html \
        | sed -e "s|__SOURCE__|${BNIA_SUIVI_URL}|" -e "s|__GENERE__|$(date '+%d/%m/%Y à %H:%M')|" > "$page"
    rm -f "$donnees"
    echo "Tableau : ${page}" >&2
    if command -v xdg-open > /dev/null; then
        xdg-open "$page" > /dev/null 2>&1 &
    fi
}

brut=$(mktemp)
trap 'rm -f "$brut"' EXIT

case "$rapport" in
    tout)
        for r in personnes visites ecrans offre fichiers; do
            printf '\n== %s\n' "$r"
            exporter "$r"
        done ;;
    tableau) tableau ;;
    *) exporter "$rapport" ;;
esac
