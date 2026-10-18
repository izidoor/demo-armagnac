package fr.bnia.facturation.adapter.integration;

import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.application.Clients;
import fr.bnia.facturation.domain.Acheteur;
import fr.bnia.facturation.domain.Adresse;
import fr.bnia.facturation.domain.CategorieAcheteur;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.AcheteurVue;
import fr.bnia.operateurs.domain.partage.AdresseVue;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.PaysVue;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Port de l'hôte (A-FAC-3, spec 08 §3.11) : un client est un opérateur, lu dans la partie publiée
 * d'{@code operateurs} (acheteur, RG-OPE-02) ; le nom et le code ISO de son pays dans celle de
 * {@code referentiels}. Un opérateur est toujours un professionnel (§3.5).
 */
@Component
class ClientsOperateurs implements Clients {

    private final OperateursLecture operateurs;
    private final ReferentielsLecture referentiels;

    ClientsOperateurs(OperateursLecture operateurs, ReferentielsLecture referentiels) {
        this.operateurs = operateurs;
        this.referentiels = referentiels;
    }

    @Override
    public Optional<Acheteur> acheteur(ClientRef client) {
        long id;
        try {
            id = Long.parseLong(client.identifiant());
        } catch (NumberFormatException inconnu) {
            return Optional.empty();
        }
        return operateurs.acheteur(id).map(a -> new Acheteur(ClientRef.operateur(a.id(), a.code()),
                CategorieAcheteur.PROFESSIONNEL, a.raisonSociale(), null, a.siret().orElse(null),
                a.numeroTva().orElse(null), adresse(a), null, a.emailFacturation().orElse(null)));
    }

    /** La ligne 3 de l'adresse d'un opérateur rejoint sa ligne 2. */
    private Adresse adresse(AcheteurVue a) {
        AdresseVue v = a.adresse();
        Optional<PaysVue> pays = v.pays().flatMap(referentiels::pays);
        String ligne2 = v.ligne2().map(l -> v.ligne3().map(l3 -> l + ", " + l3).orElse(l)).or(v::ligne3).orElse(null);
        return new Adresse(v.ligne1().orElse(null), ligne2, v.codePostal().orElse(null), v.ville().orElse(null),
                pays.map(PaysVue::libelle).orElse(null), pays.map(PaysVue::codeIso).orElse(null));
    }
}
