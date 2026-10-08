package fr.bnia.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import fr.bnia.commun.application.UseCase;
import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Règles d'architecture (ADR-002, ADR-014). Le module {@code -core} de chaque contexte, sans Spring, garantit
 * à la compilation que {@code domain} et {@code application} restent en Java pur ; ces règles gardent le
 * sens des dépendances entre couches et l'isolement des contextes.
 * Les packages suivent {@code fr.bnia.<contexte>.<couche>}, avec pour couches {@code domain}, {@code application} et
 * {@code adapter.<rest|persistence|integration|evenement>} (ADR-013 pour {@code evenement}).
 * {@code fr.bnia.commun} est le noyau partagé (ADR-009) ; {@code fr.bnia.boot} le câblage.
 * Seules autres exceptions à l'isolement des contextes : les parties publiées en lecture de
 * {@code referentiels} (ADR-008) et de {@code operateurs} (ADR-012).
 */
@AnalyzeClasses(packages = "fr.bnia", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String[] PARTIE_PUBLIEE_REFERENTIELS = {
            "fr.bnia.referentiels.domain.partage..", "fr.bnia.referentiels.application.partage.."};

    private static final String[] PARTIE_PUBLIEE_OPERATEURS = {
            "fr.bnia.operateurs.domain.partage..", "fr.bnia.operateurs.application.partage.."};

    private static final String[] ADAPTERS = {"fr.bnia..adapter.rest..", "fr.bnia..adapter.persistence..",
            "fr.bnia..adapter.integration..", "fr.bnia..adapter.evenement.."};

    /** Les adapters autres que celui du package donné. */
    private static String[] autresAdapters(String adapter) {
        return Arrays.stream(ADAPTERS).filter(a -> !a.equals(adapter)).toArray(String[]::new);
    }

    private static String[] concat(String[] paquets, String... autres) {
        return Stream.concat(Arrays.stream(paquets), Arrays.stream(autres)).toArray(String[]::new);
    }

    @ArchTest
    static final ArchRule le_domaine_est_du_java_pur = classes()
            .that().resideInAPackage("fr.bnia..domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "fr.bnia..domain..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule l_application_ne_depend_que_du_domaine = classes()
            .that().resideInAPackage("fr.bnia..application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "fr.bnia..domain..", "fr.bnia..application..");

    @ArchTest
    static final ArchRule l_adapter_rest_ignore_les_autres_adapters = adapterIsole("fr.bnia..adapter.rest..");

    @ArchTest
    static final ArchRule l_adapter_persistence_ignore_les_autres_adapters =
            adapterIsole("fr.bnia..adapter.persistence..");

    @ArchTest
    static final ArchRule l_adapter_integration_ignore_les_autres_adapters =
            adapterIsole("fr.bnia..adapter.integration..");

    /** ADR-013 : un écouteur ne fait qu'appeler un cas d'usage. */
    @ArchTest
    static final ArchRule l_adapter_evenement_ignore_les_autres_adapters = adapterIsole("fr.bnia..adapter.evenement..");

    private static ArchRule adapterIsole(String adapter) {
        return noClasses()
                .that().resideInAPackage(adapter)
                .should().dependOnClassesThat().resideInAnyPackage(autresAdapters(adapter))
                .allowEmptyShould(true);
    }

    @ArchTest
    static final ArchRule les_contextes_ne_dependent_pas_les_uns_des_autres = slices()
            .matching("fr.bnia.(*)..")
            .should().notDependOnEachOther()
            .ignoreDependency(alwaysTrue(), resideInAPackage("fr.bnia.commun.."))
            .ignoreDependency(alwaysTrue(), resideInAnyPackage(PARTIE_PUBLIEE_REFERENTIELS))
            .ignoreDependency(alwaysTrue(), resideInAnyPackage(PARTIE_PUBLIEE_OPERATEURS))
            .ignoreDependency(resideInAPackage("fr.bnia.boot.."), alwaysTrue());

    @ArchTest
    static final ArchRule les_autres_contextes_ne_lisent_que_la_partie_publiee_des_referentiels = noClasses()
            .that().resideOutsideOfPackages("fr.bnia.referentiels..", "fr.bnia.boot..")
            .should().dependOnClassesThat(resideInAPackage("fr.bnia.referentiels..")
                    .and(not(resideInAnyPackage(PARTIE_PUBLIEE_REFERENTIELS))));

    /** ADR-008 : petite, stable, en lecture seule ; ni agrégat ni cas d'usage interne n'y transparaît. */
    @ArchTest
    static final ArchRule la_partie_publiee_des_referentiels_ne_depend_que_d_elle_meme_et_du_noyau = classes()
            .that().resideInAnyPackage(PARTIE_PUBLIEE_REFERENTIELS)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(concat(PARTIE_PUBLIEE_REFERENTIELS, "java..", "fr.bnia.commun.domain.."));

    @ArchTest
    static final ArchRule les_autres_contextes_ne_lisent_que_la_partie_publiee_des_operateurs = noClasses()
            .that().resideOutsideOfPackages("fr.bnia.operateurs..", "fr.bnia.boot..")
            .should().dependOnClassesThat(resideInAPackage("fr.bnia.operateurs..")
                    .and(not(resideInAnyPackage(PARTIE_PUBLIEE_OPERATEURS))));

    /** ADR-012 : même exigence que pour les référentiels (ADR-008). */
    @ArchTest
    static final ArchRule la_partie_publiee_des_operateurs_ne_depend_que_d_elle_meme_et_du_noyau = classes()
            .that().resideInAnyPackage(PARTIE_PUBLIEE_OPERATEURS)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(concat(PARTIE_PUBLIEE_OPERATEURS, "java..", "fr.bnia.commun.domain.."))
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule le_noyau_commun_ne_depend_d_aucun_contexte = noClasses()
            .that().resideInAPackage("fr.bnia.commun..")
            .should().dependOnClassesThat(
                    resideInAPackage("fr.bnia..").and(not(resideInAPackage("fr.bnia.commun.."))));

    @ArchTest
    static final ArchRule les_contextes_sont_sans_cycle = slices()
            .matching("fr.bnia.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule les_cas_d_usage_vivent_dans_la_couche_application = classes()
            .that().areAnnotatedWith(UseCase.class)
            .should().resideInAPackage("fr.bnia..application..");

    @ArchTest
    static final ArchRule une_seule_application_spring_boot = classes()
            .that().areAnnotatedWith(SpringBootApplication.class)
            .should().haveFullyQualifiedName("fr.bnia.BniaApplication");
}
