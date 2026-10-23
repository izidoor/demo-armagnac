// Publie le diaporama dans l'application, à côté de l'offre (« Voir l'offre », choix « Diaporama ») : une page HTML
// par diapositive, sa feuille de style, ses images, ses polices et le PDF, servis par le back sous
// /api/offre/diaporama/.
// Usage, après pnpm build : pnpm publier → backend/bnia-boot/src/main/resources/offre/diaporama/
import { copyFileSync, existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import { basename, dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const racine = dirname(fileURLToPath(import.meta.url))
const sortie = join(racine, 'sortie')
const cible = join(racine, '..', '..', 'backend', 'bnia-boot', 'src', 'main', 'resources', 'offre', 'diaporama')
const PDF = 'diaporama-py-pollet.pdf'

rmSync(cible, { recursive: true, force: true })
mkdirSync(join(cible, 'polices'), { recursive: true })

const html = readFileSync(join(sortie, 'diaporama.html'), 'utf8')
const diapositives = [...html.matchAll(/<section ([^>]*)>([\s\S]*?)<\/section>/g)].map(([, attributs, contenu]) => ({
  attributs,
  contenu,
  id: /\bid="([^"]+)"/.exec(attributs)[1],
  titre: /\bdata-titre="([^"]+)"/.exec(attributs)[1],
}))

// Images : chemins relatifs au dossier du diaporama (../logo-…, ../reponse/gabarit/…), publiés à plat.
const images = new Set()
const aPlat = (contenu) => contenu.replace(/src="(?!https?:|data:)([^"]+)"/g, (_, chemin) => {
  images.add(chemin)
  return `src="${basename(chemin)}"`
})

for (const { attributs, contenu, id, titre } of diapositives) {
  writeFileSync(join(cible, `${id}.html`), `<!doctype html>
<html lang="fr">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=1280">
<title>${titre}</title>
<link rel="stylesheet" href="diaporama.css">
</head>
<body>
<section ${attributs}>${aPlat(contenu)}</section>
</body>
</html>
`)
}
for (const chemin of images) copyFileSync(join(racine, chemin), join(cible, basename(chemin)))

// Polices : les @font-face latin et latin-ext des paquets @fontsource, fichiers sous polices/.
const polices = ['inter', 'source-serif-4'].map((paquet) => {
  const dossier = join(racine, 'node_modules', '@fontsource-variable', paquet)
  return [...readFileSync(join(dossier, 'index.css'), 'utf8').matchAll(/\/\* [\w-]+ \*\/\s*@font-face \{[^}]*\}/g)]
    .map(([regle]) => regle)
    .filter((regle) => /-latin(-ext)?-wght-normal/.test(regle))
    .map((regle) => regle.replace(/url\(\.\/files\/([^)]+)\)/, (_, fichier) => {
      copyFileSync(join(dossier, 'files', fichier), join(cible, 'polices', fichier))
      return `url(polices/${fichier})`
    }))
    .join('\n\n')
}).join('\n\n')

// Dans l'application, la page est la diapositive seule : l'iframe de 1280 × 720 la met à l'échelle.
const lecture = `
/* ---- Lecture dans l'application : une diapositive par page ---- */

html, body { overflow: hidden; }
@media screen { body { padding: 0; background: #fff; } .diapo { margin: 0; box-shadow: none; } }
`
writeFileSync(join(cible, 'diaporama.css'),
  `${polices}\n\n${readFileSync(join(racine, 'gabarit', 'style.css'), 'utf8')}${lecture}`)

const pdf = join(sortie, 'diaporama.pdf')
if (existsSync(pdf)) copyFileSync(pdf, join(cible, PDF))

writeFileSync(join(cible, 'diaporama.json'), JSON.stringify({
  pdf: existsSync(pdf) ? PDF : null,
  diapositives: diapositives.map(({ id, titre }) => ({ id, titre })),
}, null, 2) + '\n')
console.log(`${diapositives.length} diapositives, ${images.size} images publiées : ${cible}`)
