#!/usr/bin/env python3
"""Génère ou régénère le sommaire en tête d'un fichier Markdown (specs).

Le sommaire liste les sections `##` et `###` sous forme de liens, au format des ancres GitHub.
Il est placé dans une section `## Sommaire`, juste avant la première section `##` du document.
Idempotent : un sommaire existant est remplacé.

Usage : python3 tools/sommaire.py docs/specs/*/spec.md
"""
import re
import sys

TITRE = "## Sommaire"
TITRE_SECTION = re.compile(r"^(##|###) (.+)$", re.MULTILINE)
BLOC_CODE = re.compile(r"^```.*?^```", re.MULTILINE | re.DOTALL)


def ancre(titre: str) -> str:
    """Ancre GitHub : minuscules, accents conservés, ponctuation supprimée, espaces en tirets."""
    texte = titre.strip().lower()
    texte = re.sub(r"[^\w\- ]", "", texte)
    return texte.replace(" ", "-")


def sommaire(corps: str) -> str:
    sans_code = BLOC_CODE.sub("", corps)
    vus: dict[str, int] = {}
    lignes = []
    for niveau, titre in TITRE_SECTION.findall(sans_code):
        cible = ancre(titre)
        # GitHub suffixe les ancres en double : -1, -2…
        if cible in vus:
            vus[cible] += 1
            cible = f"{cible}-{vus[cible]}"
        else:
            vus[cible] = 0
        retrait = "" if niveau == "##" else "  "
        lignes.append(f"{retrait}- [{titre}](#{cible})")
    return f"{TITRE}\n\n" + "\n".join(lignes) + "\n\n"


def traiter(chemin: str) -> None:
    with open(chemin, encoding="utf-8") as f:
        texte = f.read()

    # Retire un sommaire existant (jusqu'à la section `##` suivante).
    texte = re.sub(rf"^{TITRE}\n.*?(?=^## )", "", texte, flags=re.MULTILINE | re.DOTALL)

    premiere = re.search(r"^## ", texte, re.MULTILINE)
    if premiere is None:
        print(f"{chemin} : aucune section ##, ignoré", file=sys.stderr)
        return
    debut = premiere.start()
    texte = texte[:debut] + sommaire(texte[debut:]) + texte[debut:]

    with open(chemin, "w", encoding="utf-8") as f:
        f.write(texte)
    print(f"{chemin} : sommaire à jour")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    for chemin in sys.argv[1:]:
        traiter(chemin)
