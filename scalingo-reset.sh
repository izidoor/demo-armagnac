#!/usr/bin/env bash
# Remise à zéro volontaire de la base de démonstration hébergée (docs/deploiement-scalingo.md). Inutile
# après une modification de V1__schema.sql : l'application recrée alors la base à son démarrage.
#   ./scalingo-reset.sh [application]   (bnia-demo par défaut)
set -euo pipefail
app="${1:-bnia-demo}"

read -r -p "Effacer toutes les données de ${app} et recharger la démonstration ? Taper le nom de l'application : " saisie
[[ "$saisie" == "$app" ]] || { echo "Abandon."; exit 1; }

# Conteneur ponctuel : vide la base, la recrée, recharge la démonstration, puis s'arrête.
scalingo --app "$app" run \
    "bash bin/demarrer-scalingo.sh --bnia.demo.reinitialisation=true --server.port=0"
# L'application en ligne repart sur la base neuve (sessions effacées : chacun se reconnecte).
scalingo --app "$app" restart
