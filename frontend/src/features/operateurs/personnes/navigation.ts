import { useNavigate } from '@tanstack/react-router'
import { type PersonneAnnuaire } from '@/api/generated'
import { type Espace } from '../commun/api'

/** Genre d'une personne de l'annuaire, tel qu'il figure dans l'URL de sa fiche. */
export type GenrePersonne = 'contact' | 'membre' | 'invitation'

export function genreDe(p: PersonneAnnuaire): GenrePersonne {
  return p.genre === 'CONTACT'
    ? 'contact'
    : p.genre === 'MEMBRE'
      ? 'membre'
      : 'invitation'
}

/** Ouvrir la fiche d'une personne, ou revenir à l'annuaire, dans l'espace courant. */
export function useNavigationPersonnes(espace: Espace, operateur: number) {
  const navigate = useNavigate()
  return {
    ouvrir: (genre: GenrePersonne, id: number) =>
      espace === 'bo'
        ? navigate({
            to: '/bo/operateurs/$id/personnes/$genre/$personne',
            params: {
              id: String(operateur),
              genre,
              personne: String(id),
            },
          })
        : navigate({
            to: '/portail/annuaire/$genre/$personne',
            params: { genre, personne: String(id) },
          }),
    annuaire: () =>
      espace === 'bo'
        ? navigate({
            to: '/bo/operateurs/$id',
            params: { id: String(operateur) },
            search: { onglet: 'annuaire' },
          })
        : navigate({ to: '/portail/annuaire' }),
  }
}
