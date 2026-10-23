// Assemble diapositives.html dans le gabarit, ajoute les pieds de page, puis imprime le PDF 16:9 avec Chromium.
// Usage : pnpm build            → sortie/diaporama.html et sortie/diaporama.pdf
//         pnpm build --html     → HTML seul (aperçu rapide dans le navigateur)
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs'
import { join, dirname } from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { execFileSync } from 'node:child_process'

const racine = dirname(fileURLToPath(import.meta.url))
const sortie = join(racine, 'sortie')
mkdirSync(sortie, { recursive: true })

// Espaces insécables du français, dans le texte seulement : avant « : », « € » et « % », entre les milliers (pleine
// largeur : l'espace fine se perd à la projection).
const typographie = (html) => html.replace(/>([^<]+)</g, (_, texte) => '>' + texte
  .replace(/ ([:€%])/g, '\u00a0$1')
  .replace(/(\d) (?=\d{3}\b)/g, '$1\u00a0') + '<')

const source = readFileSync(join(racine, 'diapositives.html'), 'utf8').replace(/<!--[\s\S]*?-->/g, '')
const diapositives = [...source.matchAll(/<section ([^>]*)>([\s\S]*?)<\/section>/g)]
const total = diapositives.length
const pied = (numero) => `
  <footer class="pied"><span>Pierre-Yves Pollet · Observatoire économique et gestion de la Donnée Armagnac</span><span class="numero">${numero} / ${total}</span></footer>
`
const contenu = diapositives
  .map(([, attributs, corps], i) => `<section ${attributs}>${corps}${attributs.includes('data-sans-pied') ? '' : pied(i + 1)}</section>`)
  .join('\n\n')

const page = readFileSync(join(racine, 'gabarit', 'gabarit.html'), 'utf8')
  .replace('{{base}}', pathToFileURL(racine + '/').href)
  .replace('{{contenu}}', typographie(contenu))

const fichierHtml = join(sortie, 'diaporama.html')
writeFileSync(fichierHtml, page)
if (!process.argv.includes('--html')) {
  const fichierPdf = join(sortie, 'diaporama.pdf')
  execFileSync('chromium', [
    '--headless',
    '--no-pdf-header-footer',
    '--allow-file-access-from-files',
    `--print-to-pdf=${fichierPdf}`,
    pathToFileURL(fichierHtml).href,
  ], { stdio: 'ignore' })
  console.log(`PDF  : ${fichierPdf}`)
}
console.log(`HTML : ${fichierHtml} (${total} diapositives)`)
