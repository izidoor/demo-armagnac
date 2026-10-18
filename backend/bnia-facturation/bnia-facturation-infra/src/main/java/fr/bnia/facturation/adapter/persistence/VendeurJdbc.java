package fr.bnia.facturation.adapter.persistence;

import static fr.bnia.facturation.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.facturation.adapter.persistence.Lignes.AdresseLigne;
import fr.bnia.facturation.application.VendeurRepository;
import fr.bnia.facturation.domain.Vendeur;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Repository;

/** Le vendeur, une seule ligne d'identifiant 1 : insérée au premier paramétrage, mise à jour ensuite. */
@Repository
class VendeurJdbc implements VendeurRepository {

    private static final short UNIQUE = 1;

    private final JdbcAggregateTemplate template;

    VendeurJdbc(JdbcAggregateTemplate template) {
        this.template = template;
    }

    @Override
    public Optional<Vendeur> charger() {
        return Optional.ofNullable(template.findById(UNIQUE, Ligne.class)).map(Ligne::versDomaine);
    }

    @Override
    public void enregistrer(Vendeur vendeur) {
        Ligne ligne = Ligne.de(vendeur);
        if (template.existsById(UNIQUE, Ligne.class)) {
            template.update(ligne);
        } else {
            template.insert(ligne);
        }
    }

    @Table(schema = SCHEMA, name = "vendeur")
    record Ligne(@Id Short id, String nom, @Embedded.Empty(prefix = "siege_") AdresseLigne siege,
            @Embedded.Nullable(prefix = "facturation_") AdresseLigne facturation, String siren, String siret,
            String numeroTva, String iban, String bic, String telephone, String email, boolean tvaSurDebits,
            String escompte, String penalites) {

        static Ligne de(Vendeur v) {
            return new Ligne(UNIQUE, v.nom(), AdresseLigne.de(v.siege()), AdresseLigne.de(v.adresseFacturation()),
                    v.siren(), v.siret(), v.numeroTva(), v.iban(), v.bic(), v.telephone(), v.email(), v.tvaSurDebits(),
                    v.escompte(), v.penalites());
        }

        Vendeur versDomaine() {
            return new Vendeur(nom, siege.versDomaine(), Lignes.adresse(facturation), siren, siret, numeroTva, iban,
                    bic, telephone, email, tvaSurDebits, escompte, penalites);
        }
    }
}
