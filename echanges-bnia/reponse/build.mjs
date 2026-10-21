// Assemble sections/*.md dans le gabarit HTML, puis imprime le PDF avec Chromium.
// Usage : pnpm build            → sortie/reponse.html et sortie/reponse.pdf
//         pnpm build --html     → HTML seul (aperçu rapide dans le navigateur)
import { readFileSync, readdirSync, writeFileSync, mkdirSync } from 'node:fs'
import { join, dirname } from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { execFileSync } from 'node:child_process'
import MarkdownIt from 'markdown-it'

const racine = dirname(fileURLToPath(import.meta.url))
const sortie = join(racine, 'sortie')
mkdirSync(sortie, { recursive: true })

const md = new MarkdownIt({ html: true, typographer: true, quotes: '«  »‹  ›' })

const sections = readdirSync(join(racine, 'sections'))
  .filter((f) => f.endsWith('.md'))
  .sort()
  .map((f) => {
    const id = f.replace(/\.md$/, '')
    const html = md.render(readFileSync(join(racine, 'sections', f), 'utf8'))
    return `<section class="section" id="${id}">\n${html}</section>`
  })
  .join('\n')

const page = readFileSync(join(racine, 'gabarit', 'gabarit.html'), 'utf8')
  .replace('{{base}}', pathToFileURL(racine + '/').href)
  .replace('{{contenu}}', sections)

// Renvois de page du sommaire : {{page:NN-nom}}, remplacé par la page où paraît le premier h1 de la section.
const renvois = (html, pages) => html.replace(/\{\{page:([^}]+)\}\}/g, (_, id) => pages[id] ?? '')

const fichierHtml = join(sortie, 'reponse.html')
const fichierPdf = join(sortie, 'reponse.pdf')
const imprimer = () => execFileSync('chromium', [
  '--headless',
  '--no-pdf-header-footer',
  '--allow-file-access-from-files',
  `--print-to-pdf=${fichierPdf}`,
  pathToFileURL(fichierHtml).href,
], { stdio: 'ignore' })

writeFileSync(fichierHtml, renvois(page, {}))
if (!process.argv.includes('--html')) {
  // Première impression, numéros vides : ils tiennent sur la même ligne et ne déplacent rien.
  imprimer()
  const compacte = (texte) => texte.replace(/&nbsp;|&#160;/g, '').replace(/&amp;/g, '&')
    .replace(/ﬁ/g, 'fi').replace(/ﬂ/g, 'fl').replace(/\s+/g, '')
  // Le pied de page (« Pierre-Yves Pollet · … ») est retiré : il contient le titre de la section profil.
  const pied = compacte(/@bottom-left\s*\{\s*content:\s*'([^']+)'/.exec(readFileSync(join(racine, 'gabarit', 'style.css'), 'utf8'))[1])
  const textes = execFileSync('pdftotext', [fichierPdf, '-'], { encoding: 'utf8' }).split('\f')
    .map((texte) => compacte(texte).replaceAll(pied, ''))
  const pages = {}
  for (const [, id, contenu] of sections.matchAll(/<section class="section" id="([^"]+)">([\s\S]*?)<\/section>/g)) {
    const titre = /<h1>([\s\S]*?)<\/h1>/.exec(contenu)?.[1].replace(/<[^>]+>/g, '')
    if (!titre) continue
    const cle = compacte(titre).slice(0, 30)
    const index = textes.findIndex((texte, i) => i > 0 && texte.includes(cle))
    if (index > 0) pages[id] = String(index + 1)
  }
  writeFileSync(fichierHtml, renvois(page, pages))
  imprimer()
  console.log(`PDF  : ${fichierPdf}`)
}
console.log(`HTML : ${fichierHtml}`)
