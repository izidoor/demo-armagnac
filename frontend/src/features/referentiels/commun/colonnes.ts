/**
 * Les colonnes d'un tableau de référentiel, selon la largeur du contenu, comme la liste des opérateurs : le libellé
 * prend la place qui reste et se tronque (texte complet au survol) ; sous 36rem, le code et l'état perdent leur
 * largeur fixe. Le tableau tient ainsi dans un téléphone, une ligne par valeur, avec son menu d'actions.
 */
export const COLONNE_CODE = 'w-40 @max-[36rem]/content:w-auto'
export const COLONNE_ETAT = 'w-28 @max-[36rem]/content:w-auto'
export const COLONNE_LIBELLE = 'w-full max-w-0 min-w-20'
