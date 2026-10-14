package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.Exemples.BNIA;
import static fr.bnia.declarations.domain.Exemples.MAINTENANT;
import static fr.bnia.declarations.domain.Exemples.refuse;
import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.declarations.domain.partage.Cote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentaireTest {

    private static final Auteur JULIE = new Auteur(9, "Julie Darrigade");

    @Test
    @DisplayName("RG-DEC-24 : sur la DSM ou une ligne ; interne seulement côté BNIA, jamais montré à l'opérateur")
    void rg_dec_24_interne_du_bnia() {
        Commentaire surLigne = Commentaire.ecrire(50, 101L, JULIE, Cote.OPERATEUR, "  Transfert vers Eauze ",
                false, MAINTENANT);
        assertThat(surLigne.ligne()).isEqualTo(101L);
        assertThat(surLigne.texte()).isEqualTo("Transfert vers Eauze");
        refuse("RG-DEC-24", () -> Commentaire.ecrire(50, null, JULIE, Cote.OPERATEUR, "Note", true,
                MAINTENANT));
        refuse("RG-DEC-24", () -> Commentaire.ecrire(50, null, JULIE, Cote.OPERATEUR, " ", false,
                MAINTENANT));

        Commentaire interne = Commentaire.ecrire(50, null, BNIA, Cote.BNIA, "À vérifier", true, MAINTENANT);
        assertThat(interne.visibleDe(true)).isTrue();
        assertThat(interne.visibleDe(false)).isFalse();
        assertThat(surLigne.visibleDe(false)).isTrue();
    }

    @Test
    @DisplayName("RG-DEC-25 : l'auteur modifie (« modifié ») ou supprime son commentaire, personne d'autre")
    void rg_dec_25_auteur() {
        Commentaire commentaire = Commentaire.ecrire(50, null, BNIA, Cote.BNIA, "À vérifier", true,
                MAINTENANT);
        refuse("RG-DEC-25", () -> commentaire.modifier(JULIE.utilisateur(), "Autre", false, MAINTENANT));
        refuse("RG-DEC-25", () -> commentaire.exigerSupprimable(JULIE.utilisateur()));

        commentaire.modifier(BNIA.utilisateur(), "Vérifié", false, MAINTENANT.plusSeconds(60));
        assertThat(commentaire.texte()).isEqualTo("Vérifié");
        assertThat(commentaire.interne()).isFalse();
        assertThat(commentaire.modifieLe()).isEqualTo(MAINTENANT.plusSeconds(60));
        commentaire.exigerSupprimable(BNIA.utilisateur());
    }

    @Test
    @DisplayName("RG-DEC-25 : le motif d'une demande de compléments se modifie, ne se supprime pas, n'est jamais interne")
    void rg_dec_25_motif() {
        refuse("RG-DEC-20", () -> Commentaire.motif(50, BNIA, " ", MAINTENANT));
        Commentaire motif = Commentaire.motif(50, BNIA, "DAE manquants pour l'export", MAINTENANT);
        assertThat(motif.motif()).isTrue();
        assertThat(motif.visibleDe(false)).isTrue();
        refuse("RG-DEC-25", () -> motif.exigerSupprimable(BNIA.utilisateur()));
        refuse("RG-DEC-24", () -> motif.modifier(BNIA.utilisateur(), "DAE manquants", true, MAINTENANT));
        motif.modifier(BNIA.utilisateur(), "DAE manquants pour l'export (juin)", false, MAINTENANT);
    }

    @Test
    void le_commentaire_d_une_ligne_supprimee_passe_sur_la_dsm() {
        Commentaire commentaire = Commentaire.ecrire(50, 101L, JULIE, Cote.OPERATEUR, "Note", false,
                MAINTENANT);
        commentaire.detacher("« dénomination Réserve, clé NB, compte d'âge 8 »");
        assertThat(commentaire.ligne()).isNull();
        assertThat(commentaire.ligneSupprimee()).contains("Réserve");
    }
}
