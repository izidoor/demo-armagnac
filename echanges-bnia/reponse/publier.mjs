// Publie la réponse dans l'application, lue après connexion (« Voir l'offre » de la barre latérale) : une page HTML
// par section, sa feuille de style, ses images, ses polices et le PDF, servis par le back sous /api/offre/ ; le
// diaporama, publié par echanges-bnia/diaporama, sous /api/offre/diaporama/.
// Usage, après pnpm build : pnpm publier → backend/bnia-boot/src/main/resources/offre/
import { copyFileSync, mkdirSync, readFileSync, readdirSync, rmSync, statSync, writeFileSync } from 'node:fs'
import { basename, dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const racine = dirname(fileURLToPath(import.meta.url))
const sortie = join(racine, 'sortie')
const cible = join(racine, '..', '..', 'backend', 'bnia-boot', 'src', 'main', 'resources', 'offre')
const PDF = 'reponse-ao-armagnac-py-pollet.pdf'

// Le diaporama, publié à part (echanges-bnia/diaporama), garde son dossier.
mkdirSync(cible, { recursive: true })
for (const entree of readdirSync(cible)) if (entree !== 'diaporama') rmSync(join(cible, entree), { recursive: true })
mkdirSync(join(cible, 'polices'), { recursive: true })

const html = readFileSync(join(sortie, 'reponse.html'), 'utf8')
const sections = [...html.matchAll(/<section class="section" id="([^"]+)">([\s\S]*?)<\/section>/g)]
  .map(([, id, contenu]) => ({ id, contenu }))

// Titres des miniatures : les entrées du sommaire, dans l'ordre des sections qui suivent la page de garde.
const texte = (fragment) => fragment.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim()
const entrees = [...html.matchAll(/<span class="entree">([\s\S]*?)<\/span>/g)].map(([, e]) => texte(e))
if (entrees.length !== sections.length - 1) {
  throw new Error(`${entrees.length} entrées au sommaire pour ${sections.length - 1} sections après la garde`)
}
const annexes = sections.slice(1).filter((s) => s.id.includes('annexe')).map((s) => s.id)
const index = sections.map((s, i) => ({
  id: s.id,
  titre: i === 0 ? 'Couverture et sommaire' : entrees[i - 1],
  ...(annexes.includes(s.id) && { annexe: String.fromCharCode(65 + annexes.indexOf(s.id)) }),
}))

// Images : chemins relatifs au dossier de la réponse (gabarit/…, ../logo-…), publiés à plat.
const images = new Set()
const aPlat = (contenu) => contenu.replace(/src="(?!https?:|data:)([^"]+)"/g, (_, chemin) => {
  images.add(chemin)
  return `src="${basename(chemin)}"`
})

// Le sommaire de la garde mène aux sections : la page signale le clic à l'application (postMessage).
const sommaireCliquable = (contenu) => {
  let i = 1
  return contenu.replace(/<li><span class="ligne">/g, () => `<li data-section="${sections[i++].id}"><span class="ligne">`)
}

const tete = `<script>
  if (location.hash === '#miniature') document.documentElement.classList.add('miniature')
  document.addEventListener('click', (e) => {
    const lien = e.target.closest('[data-section]')
    if (lien) parent.postMessage({ offre: lien.dataset.section }, location.origin)
  })
</script>`

for (const [i, { id, contenu }] of sections.entries()) {
  const corps = aPlat(i === 0 ? sommaireCliquable(contenu) : contenu)
  writeFileSync(join(cible, `${id}.html`), `<!doctype html>
<html lang="fr">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${index[i].titre}</title>
<base target="_blank">
<link rel="stylesheet" href="offre.css">
${tete}
</head>
<body>
<section class="section" id="${id}">${corps}</section>
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

const lecture = `
/* ---- Lecture dans l'application : une section par page, miniatures, sommaire cliquable ---- */

.garde-sommaire .points, .garde-sommaire .page { display: none; }
.garde-sommaire li[data-section] { cursor: pointer; }
.garde-sommaire li[data-section]:hover .entree { color: var(--ambre-fonce); text-decoration: underline; }

@media screen and (max-width: 860px) {
  body { padding: 0; }
  .section { width: auto; min-height: 0; padding: 20px 16px; box-shadow: none; }
  .saut { margin: 20px -16px; }
  .trois-places, .deux-colonnes, .colonnes, .definitions, .livraisons, .garde-bas, .signatures { grid-template-columns: 1fr; }
  .chiffres { grid-template-columns: 1fr 1fr; }
  .bandeau ul { columns: 1; }
  .profil-entete { flex-wrap: wrap; }
  .profil-entete .coordonnees { text-align: left; white-space: normal; }
  table { display: block; overflow-x: auto; }
}

.miniature, .miniature body { overflow: hidden; }
.miniature body { padding: 0; background: #fff; }
.miniature .section { margin: 0; box-shadow: none; }
`
writeFileSync(join(cible, 'offre.css'),
  `${polices}\n\n${readFileSync(join(racine, 'gabarit', 'style.css'), 'utf8')}${lecture}`)

// Le PDF le plus récent de la sortie (pnpm build écrit reponse.pdf, le dépôt l'a renommé).
const pdf = readdirSync(sortie).filter((f) => f.endsWith('.pdf'))
  .map((f) => ({ f, le: statSync(join(sortie, f)).mtimeMs })).sort((a, b) => b.le - a.le)[0]
if (pdf) copyFileSync(join(sortie, pdf.f), join(cible, PDF))

writeFileSync(join(cible, 'sections.json'),
  JSON.stringify({ pdf: pdf ? PDF : null, sections: index }, null, 2) + '\n')
console.log(`${sections.length} sections, ${images.size} images publiées : ${cible}`)
