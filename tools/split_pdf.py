#!/usr/bin/env python3
"""Découpe un PDF en Markdown par chapitre / section, à partir des signets."""
import re, sys, shutil, pathlib, unicodedata
import pymupdf
import pymupdf4llm

MAX_SPAN = 12        # pages max par fichier avant de descendre d'un niveau de signets
MAX_DEPTH = 3        # niveau de signets maximum exploité
MAX_BYTES = 30_000   # au-delà : tentative de découpage par titres d'annexe (C.1, C.2…)
FIRST_ANNEX = 10     # rang (1-based) du premier chapitre de niveau 1 qui est une annexe

# Étiquettes des fichiers d'annexes, dans l'ordre des signets :
# A glossaire, B comptes d'âge, C.1 à C.14 référentiel de données, D cartographie, E exemples
ANNEX_PREFIX = ["A", "B"] + [f"C{i:02d}" for i in range(1, 15)] + ["D", "E"]

NOISE = re.compile(
    r"^(Cahier des charges BNIA|Spécification de récupération des données AGI"
    r"|v\d+\.\d+|\d+ / \d+)(\s+(v\d+\.\d+|\d+ / \d+))*$"
)
ANNEX_SEC = re.compile(r"^#{1,4}\s*\**([A-E]\.\d+)\s+(.+?)\**\s*$", re.M)

PICTURE = re.compile(r"<!-- Start of picture text -->.*?<!-- End of picture text -->", re.S)


def clean(text: str) -> str:
    text = PICTURE.sub("*[Schéma supprimé (OCR illisible) : voir le PDF à cette page]*", text)
    kept = [l for l in text.splitlines() if not NOISE.match(l.strip(" _*\t"))]
    text = "\n".join(kept)
    text = re.sub(r"^#*\s*(?:Exclamation-Triangle\s*)?\**ATTENTION\**\s*$",
                  "> **⚠ ATTENTION**", text, flags=re.M)
    text = re.sub(r"^#*\s*\**INFO\**(?:\s+\**INFO\**)?\s*$",
                  "> **ℹ INFO**", text, flags=re.M)
    text = re.sub(r"^(#+)\s*\*\*(.+?)\*\*\s*$", r"\1 \2", text, flags=re.M)
    return re.sub(r"\n{3,}", "\n\n", text).strip()


def slug(s: str) -> str:
    s = unicodedata.normalize("NFKD", s).encode("ascii", "ignore").decode()
    return re.sub(r"[^a-z0-9]+", "-", s.lower()).strip("-")[:60]


def build_nodes(toc, n_pages):
    nodes = []
    for i, (lvl, title, page) in enumerate(toc):
        end = n_pages
        for j in range(i + 1, len(toc)):
            if toc[j][0] <= lvl:
                end = toc[j][2]
                break
        nodes.append({"lvl": lvl, "title": title, "start": page, "end": max(end, page)})
    return nodes


def kids_of(nodes, i):
    kids = []
    for j in range(i + 1, len(nodes)):
        if nodes[j]["lvl"] <= nodes[i]["lvl"]:
            break
        if nodes[j]["lvl"] == nodes[i]["lvl"] + 1:
            kids.append(j)
    return kids


def pick(nodes, i):
    nd, kids = nodes[i], kids_of(nodes, i)
    span = nd["end"] - nd["start"] + 1
    if span > MAX_SPAN and kids and nd["lvl"] < MAX_DEPTH:
        out = []
        first = nodes[kids[0]]["start"]
        if first > nd["start"]:
            out.append((f"{nd['title']} - intro", nd["start"], first))
        for k in kids:
            out += pick(nodes, k)
        return out
    return [(nd["title"], nd["start"], nd["end"])]


def emit(d: pathlib.Path, tag: str, title: str, text: str, header: str):
    marks = list(ANNEX_SEC.finditer(text))
    if len(text.encode()) <= MAX_BYTES or len(marks) < 2:
        (d / f"{tag}-{slug(title)}.md").write_text(header + text, encoding="utf-8")
        return
    cuts = [m.start() for m in marks] + [len(text)]
    if cuts[0] > 0:
        (d / f"{tag}-intro.md").write_text(header + text[:cuts[0]], encoding="utf-8")
    for m, a, b in zip(marks, cuts, cuts[1:]):
        (d / f"{m.group(1)}-{slug(m.group(2))}.md").write_text(header + text[a:b], encoding="utf-8")


def main(pdf: str, outdir: str):
    out = pathlib.Path(outdir)
    shutil.rmtree(out, ignore_errors=True)
    out.mkdir(parents=True)
    doc = pymupdf.open(pdf)
    toc = [t for t in doc.get_toc() if t[0] <= MAX_DEPTH]
    if not toc:
        sys.exit("Pas de signets dans ce PDF : voir plan B")
    nodes = build_nodes(toc, doc.page_count)
    src = pathlib.Path(pdf).name

    chap = 0
    for i, nd in enumerate(nodes):
        if nd["lvl"] != 1:
            continue
        chap += 1
        label = str(chap) if chap < FIRST_ANNEX else "ABCDEFGH"[chap - FIRST_ANNEX]
        d = out / f"{label}-{slug(nd['title'])}"
        d.mkdir()

        picks = pick(nodes, i)
        # Étiquetage A/B/C01… uniquement si le découpage correspond exactement
        # à la structure attendue ; sinon numérotation simple (garde-fou).
        annex = nd["title"].lower().startswith("annexe") and len(picks) == len(ANNEX_PREFIX)

        for num, (title, start, end) in enumerate(picks, 1):
            tag = ANNEX_PREFIX[num - 1] if annex else f"{num:02d}"
            pages = list(range(start - 1, min(end, doc.page_count)))
            chunks = pymupdf4llm.to_markdown(doc, pages=pages, page_chunks=True)
            text = "\n\n".join(f"<!-- p.{p + 1} -->\n{clean(c['text'])}"
                               for p, c in zip(pages, chunks))
            header = f"<!-- Source : {src} · {nd['title']} · pp. {start}-{end} -->\n\n"
            emit(d, tag, title, text, header)
            print(f"{label}/{tag} {title} (pp. {start}-{end})")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
