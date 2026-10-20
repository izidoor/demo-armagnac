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

const fichierHtml = join(sortie, 'reponse.html')
writeFileSync(fichierHtml, page)
console.log(`HTML : ${fichierHtml}`)

if (!process.argv.includes('--html')) {
  const fichierPdf = join(sortie, 'reponse.pdf')
  execFileSync('chromium', [
    '--headless',
    '--no-pdf-header-footer',
    '--allow-file-access-from-files',
    `--print-to-pdf=${fichierPdf}`,
    pathToFileURL(fichierHtml).href,
  ], { stdio: 'ignore' })
  console.log(`PDF  : ${fichierPdf}`)
}
