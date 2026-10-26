#!/usr/bin/env bash
# Copie des données téléchargée par un ADMIN du BNIA (« Données » des paramètres, SauvegardeController) : la base,
# au format de pg_dump, et les documents déposés, en zip, dans backend/bnia-boot/src/main/resources/sauvegarde/.
# Tirée d'une base de démonstration neuve, dans un PostgreSQL jetable : la base de dev n'est pas touchée.
# Après une modification de V1__schema.sql ou des données de démonstration, et après ./mvnw package (le jar) :
#   ./sauvegarde-demo.sh
set -euo pipefail
racine="$(cd "$(dirname "$0")" && pwd)"
cible="$racine/backend/bnia-boot/src/main/resources/sauvegarde"
jar="$(ls "$racine"/backend/bnia-boot/target/bnia-boot-*.jar)"
conteneur=bnia-sauvegarde
port=55439

docker run -d --rm --name "$conteneur" -p "$port:5432" \
    -e POSTGRES_DB=bnia -e POSTGRES_USER=bnia -e POSTGRES_PASSWORD=bnia postgres:18 > /dev/null
travail="$(mktemp -d)"
trap 'docker stop "$conteneur" > /dev/null; rm -rf "$travail"' EXIT
until docker exec "$conteneur" pg_isready -U bnia -d bnia -h localhost -q; do sleep 1; done
psql() { docker exec "$conteneur" psql -U bnia -d bnia -At "$@" < /dev/null; }

# La démonstration se charge, puis l'application s'arrête (ReinitialisationDemo).
java -jar "$jar" --spring.profiles.active=dev,demo --bnia.demo.reinitialisation=true --server.port=0 \
    --spring.docker.compose.enabled=false --logging.level.fr.bnia=info \
    --spring.datasource.url="jdbc:postgresql://localhost:$port/bnia" \
    --spring.datasource.username=bnia --spring.datasource.password=bnia > "$travail/demarrage.log" \
    || { tail -50 "$travail/demarrage.log"; exit 1; }

mkdir -p "$cible" "$travail/documents"
# Sans le suivi de l'utilisation du prototype ; des sessions ouvertes au chargement, la structure seule.
docker exec "$conteneur" pg_dump -U bnia -d bnia --format=custom --no-owner --no-privileges --exclude-schema=suivi \
    --exclude-table-data='authentification.spring_session*' > "$cible/bnia.dump"

# Un fichier par document, préfixé de son identifiant : deux documents peuvent porter le même nom.
psql -F $'\t' -c "select id, nom from documents.document order by id" | while IFS=$'\t' read -r id nom; do
    psql -c "select encode(contenu, 'base64') from documents.document where id = $id" | base64 -d \
        > "$travail/documents/$id-${nom//\//_}"
done
rm -f "$cible/bnia-documents.zip"
(cd "$travail" && zip -qr "$cible/bnia-documents.zip" documents)
ls -lh "$cible"
