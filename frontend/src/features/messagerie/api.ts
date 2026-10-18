import {
  consulterConversation,
  consulterConversationObjet,
  ecrireMessageObjet,
  listerConversationsBnia,
  listerMesConversations,
  listerMesNouveauxMessages,
  listerNouveauxMessagesBnia,
  marquerMessageNonLu,
  modifierMessage,
  ouvrirConversation,
  repondreConversation,
  supprimerMessage,
  type ObjetConversation,
} from '@/api/generated'

/** Un type d'objet conversable (spec 15 §3.2). */
export type TypeObjet = ObjetConversation['type']

/** Clés de cache de la messagerie, invalidées ensemble après une écriture ou une lecture (spec 15). */
export const cleMessagerie = ['messagerie'] as const

/** Les listes (nouveaux messages, conversations, compteur) : relues sans relire la conversation affichée. */
export const cleListesMessagerie = [...cleMessagerie, 'listes'] as const

/** Filtre de « Mes conversations » (RG-MSG-15, 16) ; l'opérateur, au back-office seulement. */
export type FiltreConversations = {
  operateur?: number
  type?: TypeObjet
  libres?: boolean
  nonLues?: boolean
  recherche?: string
}

/** RG-MSG-14 : une pièce jointe, ouverte dans un nouvel onglet (PDF, image) ou téléchargée (Excel). */
export const urlPieceJointe = (conversation: number, cle: number) =>
  `/api/messagerie/conversations/${conversation}/pieces/${cle}`

/** RG-MSG-14 : les formats admis en pièce jointe, par type MIME et leurs extensions. */
const TYPES_PIECES: Record<string, string[]> = {
  'application/pdf': ['.pdf'],
  'image/png': ['.png'],
  'image/jpeg': ['.jpg', '.jpeg'],
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': [
    '.xlsx',
  ],
}

/**
 * Le filtre du sélecteur de fichiers : par type MIME, pour que le navigateur propose un filtre commun plutôt qu'un
 * filtre par extension (Firefox sous Linux présélectionnait le dernier, « *.xlsx »).
 */
export const FORMATS_PIECES = Object.keys(TYPES_PIECES).join(',')

/**
 * Le fichier est-il d'un format admis (RG-MSG-14) ? Le sélecteur laisse toujours choisir « Tous les fichiers » :
 * l'extension est vérifiée, le type MIME déclaré par le navigateur n'étant pas toujours renseigné.
 */
export function formatPieceAdmis(fichier: File) {
  const nom = fichier.name.toLowerCase()
  return Object.values(TYPES_PIECES).some((extensions) =>
    extensions.some((e) => nom.endsWith(e))
  )
}

/** Les appels de la messagerie (spec 15 §5) : mêmes endpoints au back-office et au portail (RG-MSG-03). */
export const apiMessagerie = {
  /** RG-MSG-04, 10 : la conversation d'un objet, vide s'il n'en a pas encore ; affichée, elle est lue. */
  conversationObjet: (type: TypeObjet, identifiant: string) =>
    consulterConversationObjet({
      path: { type, identifiant },
      throwOnError: true,
    }).then((r) => r.data),
  ecrireSurObjet: (type: TypeObjet, identifiant: string, texte: string) =>
    ecrireMessageObjet({
      path: { type, identifiant },
      body: { texte },
      throwOnError: true,
    }).then((r) => r.data),
  /** RG-MSG-10 : une conversation ; affichée, elle est lue pour mon côté. */
  conversation: (id: number) =>
    consulterConversation({ path: { id }, throwOnError: true }).then(
      (r) => r.data
    ),
  /** RG-MSG-14 : des pièces jointes en conversation libre seulement. */
  repondre: (id: number, texte: string, pieces: File[] = []) =>
    repondreConversation({
      path: { id },
      body: { texte, pieces },
      throwOnError: true,
    }).then((r) => r.data),
  /** RG-MSG-12 : au portail, une conversation libre avec le BNIA, que je suis seul à voir dans mon opérateur. */
  ouvrir: (
    operateur: number,
    titre: string,
    texte: string,
    pieces: File[] = []
  ) =>
    ouvrirConversation({
      path: { operateur },
      body: { titre, texte, pieces },
      throwOnError: true,
    }).then((r) => r.data),
  /** RG-MSG-11 : les nouveaux messages, au back-office (opérateur absent) ou au portail. */
  nouveaux: (operateur?: number) =>
    (operateur == null
      ? listerNouveauxMessagesBnia({ throwOnError: true })
      : listerMesNouveauxMessages({ path: { operateur }, throwOnError: true })
    ).then((r) => r.data),
  /** RG-MSG-15, 16 : mes conversations, au back-office (opérateur absent) ou au portail. */
  conversations: (operateur: number | undefined, filtre: FiltreConversations) =>
    (operateur == null
      ? listerConversationsBnia({ query: filtre, throwOnError: true })
      : listerMesConversations({
          path: { operateur },
          query: {
            type: filtre.type,
            libres: filtre.libres,
            nonLues: filtre.nonLues,
            recherche: filtre.recherche,
          },
          throwOnError: true,
        })
    ).then((r) => r.data),
  modifier: (id: number, texte: string) =>
    modifierMessage({
      path: { id },
      body: { texte },
      throwOnError: true,
    }).then((r) => r.data),
  supprimer: (id: number) =>
    supprimerMessage({ path: { id }, throwOnError: true }),
  marquerNonLu: (id: number) =>
    marquerMessageNonLu({ path: { id }, throwOnError: true }),
}
