import { type ReactNode } from 'react'
import { ChevronDown } from 'lucide-react'
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible'
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet'

/**
 * Aide à la saisie d'une DSM, dans un panneau latéral, en sections repliables : le fonctionnement de
 * l'écran puis un glossaire. Les raccourcis de la grille sont sous la grille.
 * Tirée de la spec 05 (RG-DEC-03 à 19) et du glossaire ; à tenir à jour avec eux.
 */
export function AideDsm({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className='w-full overflow-y-auto sm:max-w-xl'>
        <SheetHeader>
          <SheetTitle>Aide à la saisie de la DSM</SheetTitle>
          <SheetDescription>
            Comment remplir votre déclaration de stocks et de mouvements, et ce
            que veulent dire ses termes.
          </SheetDescription>
        </SheetHeader>
        <div className='mx-4 mb-6 divide-y border-y text-sm leading-relaxed'>
          <Section titre='Le principe' ouverte>
            <p>
              La DSM déclare, pour une période, le stock d'Armagnac de chaque{' '}
              <strong>ligne</strong> et tout ce qui l'a fait varier. Pour chaque
              ligne : stock de début, plus les entrées, moins les sorties, donne
              le stock de fin. La période est le mois pour un négociant, du 1er
              avril au 31 mars pour un producteur.
            </p>
            <p>
              Tous les volumes sont en <strong>litres d'alcool pur</strong>,
              avec au plus deux décimales ; la virgule et le point sont
              acceptés. Une cellule vide vaut 0.
            </p>
          </Section>

          <Section titre='Les lignes'>
            <p>
              Une ligne est une eau-de-vie identifiée par trois colonnes, dans
              l'ordre de la grille :
            </p>
            <ul className='list-disc space-y-1 ps-5'>
              <li>
                <strong>millésime ou dénomination</strong> : un millésime (2010)
                pour une ligne millésimée, une dénomination (XO, VSOP, Blanche…)
                pour un assemblage ;
              </li>
              <li>
                <strong>clé de conditionnement</strong> : <Code>MB</Code>{' '}
                millésime sous bois, <Code>MV</Code> millésime sous verre,{' '}
                <Code>NB</Code> assemblage sous bois, <Code>NV</Code> assemblage
                sous verre ;
              </li>
              <li>
                <strong>compte d'âge</strong>, de <Code>00</Code> à{' '}
                <Code>20+</Code> (voir le glossaire).
              </li>
            </ul>
            <p>
              Deux lignes ne peuvent pas avoir les trois mêmes valeurs ; les
              dénominations se comparent sans tenir compte des majuscules, des
              accents ni des espaces.
            </p>
            <p>
              Les lignes <strong>reprises</strong> du stock précédent ne se
              suppriment pas et leur identité ne change pas. Vous pouvez{' '}
              <strong>ajouter</strong> une ligne depuis n'importe quel onglet («
              Ajouter une ligne », sous la grille) ; elle part d'un stock de
              début à 0 et se supprime depuis le récapitulatif, mention «
              ajoutée ».
            </p>
          </Section>

          <Section titre='Les onglets et les colonnes'>
            <p>
              Les 26 colonnes numérotées du format unique sont réparties en cinq
              onglets ; les lignes sont les mêmes dans chacun.
            </p>
            <ul className='list-disc space-y-1 ps-5'>
              <li>
                <strong>Récapitulatif</strong> : stock de début (1), achats (2,
                3), totaux des entrées et des sorties, stock de fin (26).
              </li>
              <li>
                <strong>Mouvements en entrée</strong> (4 à 9) : distillation,
                passage sous bois ou sous verre, assemblages et coupes,
                transferts entre chais, réintégrations.
              </li>
              <li>
                <strong>Mouvements en sortie</strong> (10 à 16) : pertes sur
                inventaire, passage sous bois ou sous verre, assemblages et
                coupes, transferts, Floc de Gascogne, autres fabrications. La
                date de la DIA se saisit en tête de cet onglet.
              </li>
              <li>
                <strong>Ventes France</strong> (17 à 21) et{' '}
                <strong>Ventes à l'exportation</strong> (22 à 25).
              </li>
            </ul>
            <p>Les fonds des cellules disent ce qui se saisit :</p>
            <ul className='list-disc space-y-1 ps-5'>
              <li>blanc : à saisir ;</li>
              <li>
                bleuté, mention « calculée » : stock de début, stock de fin,
                totaux ; non modifiable ;
              </li>
              <li>
                hachuré, mention « sans objet » : colonne qui ne concerne pas un
                producteur (2, 3, 5, 9, 11, 19, 24, 25) ; elle reste vide.
              </li>
            </ul>
          </Section>

          <Section titre='Les calculs'>
            <p>
              Pour chaque ligne, le stock de fin (colonne 26) est le stock de
              début (1), plus les achats (2, 3) et les entrées (4 à 9), moins
              les sorties et les ventes (10 à 25). La rangée « Total »
              additionne chaque colonne sur toutes les lignes.
            </p>
          </Section>

          <Section titre='Les anomalies'>
            <p>
              Le cadre en haut de la page vérifie la DSM pendant la saisie. Un
              clic sur une anomalie ouvre l'onglet et la ligne concernés.
            </p>
            <ul className='list-disc space-y-1 ps-5'>
              <li>
                <strong>Stock de fin négatif</strong> sur une ligne : plus de
                sorties que de stock.
              </li>
              <li>
                <strong>Assemblages / coupes déséquilibrés</strong> : le total
                de la colonne 6 (entrées) doit égaler celui de la colonne 12
                (sorties). Ce qui sort de certaines lignes pour un assemblage
                entre dans une autre ; le contrôle porte sur les totaux, pas
                ligne par ligne.
              </li>
              <li>
                <strong>Passages sous bois / sous verre déséquilibrés</strong>,
                pour un négociant : le total de la colonne 5 doit égaler celui
                de la colonne 11. Ce qui sort d'une ligne sous bois entre dans
                une ligne sous verre.
              </li>
              <li>
                <strong>Compte d'âge trop élevé</strong> pour une ligne{' '}
                <Code>MV</Code> : il ne peut dépasser celui de son millésime.
              </li>
            </ul>
            <p>
              Une anomalie n'empêche pas d'enregistrer : vous pouvez la corriger
              plus tard. Elle doit l'être avant l'envoi au BNIA.
            </p>
          </Section>

          <Section titre='Enregistrer'>
            <p>
              Rien n'est enregistré tant que vous n'avez pas cliqué sur «
              Enregistrer » ; la mention « Modifications non enregistrées » le
              rappelle. La DSM reste un brouillon, que vous complétez autant de
              fois que nécessaire. Chaque enregistrement est inscrit à
              l'historique.
            </p>
          </Section>

          <Section titre='Envoyer, instruction, validation'>
            <p>
              Quand la DSM est complète et sans anomalie, « Envoyer »
              l'enregistre puis la transmet au BNIA. Elle ne se modifie plus,
              sauf si le BNIA vous demande des compléments.
            </p>
            <ul className='list-disc space-y-1 ps-5'>
              <li>
                <strong>Brouillon</strong> : vous la remplissez ; vous pouvez
                encore la supprimer.
              </li>
              <li>
                <strong>Envoyée</strong> : elle attend le BNIA.
              </li>
              <li>
                <strong>En instruction</strong> : le BNIA la vérifie.
              </li>
              <li>
                <strong>Compléments demandés</strong> : le BNIA vous la renvoie
                avec un motif, en commentaire. Vous la corrigez, puis « Renvoyer
                ».
              </li>
              <li>
                <strong>Compléments envoyés</strong> : vos corrections attendent
                le BNIA, qui valide ou redemande des compléments.
              </li>
              <li>
                <strong>Validée</strong> : elle est figée ; son stock de fin
                devient le stock de début de la période suivante.
              </li>
            </ul>
            <p>
              La DSM suivante reprend les lignes dont le stock de fin est
              positif. Au 1er avril, les eaux-de-vie sous bois vieillissent :
              compte d'âge recalculé pour une ligne <Code>MB</Code>, augmenté
              d'un an pour une ligne <Code>NB</Code>, jusqu'à <Code>20+</Code> ;
              deux lignes devenues identiques sont réunies.
            </p>
          </Section>

          <Section titre='Pièces et DRM'>
            <p>
              L'onglet Pièces reçoit les justificatifs en PDF (10 Mo au plus) :
              dès qu'un type de mouvement a un total non nul, au moins une pièce
              de ce type est attendue — facture ou DAE pour les achats et les
              ventes négoce ou export, avoir ou DAE pour les réintégrations,
              déclaration de fabrication douanes pour les autres fabrications.
              Le Floc de Gascogne (colonne 15) n'en appelle pas. Une pièce porte
              sur la DSM ou sur une de ses lignes.
            </p>
            <p>
              Un producteur dépose aussi une DRM par mois de la période. Pour
              remplacer une pièce ou une DRM, supprimez-la puis déposez la
              nouvelle.
            </p>
            <p>
              Vous déposez et supprimez tant que la DSM est chez vous
              (brouillon, compléments demandés) ; le BNIA le peut aussi pendant
              l'instruction. Une pièce manquante n'empêche pas l'envoi, mais le
              BNIA ne peut valider la DSM qu'une fois toutes les pièces
              déposées.
            </p>
          </Section>
          <Section titre='Cotisation'>
            <p>
              L'onglet Cotisation calcule la cotisation CVO sur les sorties de
              l'assiette : colonnes 14, 16 à 19 et 21 à 25, sauf les colonnes
              grisées d'un producteur. Les volumes se comptent en hectolitres
              d'alcool pur (litres ÷ 100), par ligne de cotisation : sorties
              France, sorties export, autres produits.
            </p>
            <p>
              Le taux est celui de la grille du BNIA en vigueur à la fin de la
              période ; sa part exonérée de TVA donne la part non assujettie, le
              reste la part assujettie, chacune arrondie au centime. La TVA et
              le TTC sont indicatifs : la facture du BNIA fixe les montants.
              Sans taux à cette date, la cotisation est non calculable.
            </p>
          </Section>
          <Section titre='Édition PDF'>
            <p>
              Le bouton PDF ouvre la DSM enregistrée dans un nouvel onglet, au
              format unique : un onglet par page, volumes au format hl.l,cl
              (22.54,15 pour 2 254,15 L), puis la cotisation. Avant la
              validation, chaque page porte la mention « Document provisoire,
              DSM non validée ». Chaque édition est inscrite à l'historique.
            </p>
          </Section>
          <Section titre='Excel : édition et import'>
            <p>
              Le bouton Excel télécharge la DSM enregistrée au format du modèle
              du BNIA : cinq onglets, colonnes numérotées, volumes en litres
              affichés au format hl.l,cl, totaux calculés, et un onglet «
              Cotisation » pour information. Le classeur d'un brouillon sert de
              modèle pré-rempli : les lignes se déclarent dans le récapitulatif
              (millésime ou dénomination, clé, compte d'âge), leurs volumes dans
              chaque onglet, sur la même rangée.
            </p>
            <p>
              Importer remplace les colonnes 2 à 25 par celles du fichier. Les
              lignes se rapprochent par leur identité : une ligne nouvelle est
              ajoutée, une ligne reprise absente du fichier reste à 0, une ligne
              ajoutée absente est supprimée. Le stock de début, le stock de fin,
              le compte d'âge d'une ligne MB et la date de DIA restent ceux de
              la DSM ; un écart du fichier est signalé en avertissement.
            </p>
            <p>
              L'import est tout ou rien : une clé inconnue, un volume négatif ou
              illisible, une ligne en double, une valeur en colonne grisée ou un
              mois différent de la période refusent le fichier, avec la liste
              des erreurs (onglet, ligne, colonne). Les modifications non
              enregistrées de l'écran sont perdues.
            </p>
          </Section>
          <Section titre='Commentaires'>
            <p>
              Sous le classeur, le fil des échanges avec le BNIA, dans tous les
              états de la DSM, même validée. Un commentaire porte sur la DSM ou
              sur une ligne ; une ligne commentée porte une pastille dans la
              grille, qui mène à ses commentaires.
            </p>
            <p>
              Vous modifiez ou supprimez vos propres commentaires ; le motif
              d'une demande de compléments ne se supprime pas. Les commentaires
              internes du BNIA ne vous sont pas montrés.
            </p>
          </Section>

          <Section titre='Glossaire'>
            <dl className='space-y-3'>
              <Terme nom='Assemblage'>
                Mélange de plusieurs Armagnacs ; son compte d'âge est celui de
                l'eau-de-vie la plus jeune. Clés <Code>NB</Code> et{' '}
                <Code>NV</Code>.
              </Terme>
              <Terme nom="Compte d'âge">
                Âge commercial d'une eau-de-vie, qui augmente chaque 1er avril.
                Pour un millésime N : <Code>00</Code> jusqu'au 31 mars N+1,{' '}
                <Code>0</Code> jusqu'au 31 mars N+2, puis <Code>1</Code>,{' '}
                <Code>2</Code>… jusqu'à <Code>20+</Code>. <Code>00</Code> et{' '}
                <Code>0</Code> sont deux valeurs différentes. Calculé
                automatiquement pour une ligne <Code>MB</Code>, saisi pour les
                autres.
              </Terme>
              <Terme nom='Coupe'>
                Prélèvement sur une ou plusieurs lignes pour en alimenter une
                autre ; se déclare comme un assemblage (colonnes 6 et 12).
              </Terme>
              <Terme nom='DIA'>
                Déclaration d'inventaire déposée aux douanes. Sa date,
                facultative, est celle de l'inventaire qui justifie les pertes
                de la colonne 10.
              </Terme>
              <Terme nom='Litre d’alcool pur (L AP)'>
                Volume d'eau-de-vie multiplié par son titre alcoométrique : 100
                litres à 40 % font 40 L AP. Unité de tous les volumes de la DSM.
              </Terme>
              <Terme nom='Millésime'>
                Année de récolte d'une eau-de-vie non assemblée. Clés{' '}
                <Code>MB</Code> et <Code>MV</Code>.
              </Terme>
              <Terme nom='Point de départ'>
                Début de votre première période déclarative et stock initial,
                ligne par ligne, fixés par le BNIA. La première DSM en reprend
                les lignes.
              </Terme>
              <Terme nom='Sous bois, sous verre'>
                En fût (clés <Code>MB</Code>, <Code>NB</Code>) ou en bouteille
                (clés <Code>MV</Code>, <Code>NV</Code>). Une eau-de-vie vieillit
                sous bois, pas sous verre.
              </Terme>
              <Terme nom='Stock de début, stock de fin'>
                Le stock de début reprend le stock de fin de la DSM précédente,
                ou le stock initial du point de départ ; le stock de fin est
                calculé.
              </Terme>
            </dl>
          </Section>
        </div>
      </SheetContent>
    </Sheet>
  )
}

/** Une section repliable ; plusieurs peuvent être ouvertes à la fois. */
function Section({
  titre,
  ouverte = false,
  children,
}: {
  titre: string
  ouverte?: boolean
  children: ReactNode
}) {
  return (
    <Collapsible defaultOpen={ouverte} className='group'>
      <CollapsibleTrigger className='hover:text-primary flex w-full items-center justify-between py-3 text-left text-base font-semibold'>
        {titre}
        <ChevronDown className='text-muted-foreground size-4 shrink-0 transition-transform group-data-[state=open]:rotate-180' />
      </CollapsibleTrigger>
      <CollapsibleContent className='space-y-2 pb-4'>
        {children}
      </CollapsibleContent>
    </Collapsible>
  )
}

function Terme({ nom, children }: { nom: string; children: ReactNode }) {
  return (
    <div>
      <dt className='font-medium'>{nom}</dt>
      <dd className='text-muted-foreground'>{children}</dd>
    </div>
  )
}

function Code({ children }: { children: ReactNode }) {
  return (
    <code className='bg-muted rounded-sm px-1 py-0.5 font-mono text-xs'>
      {children}
    </code>
  )
}
