package fr.bnia.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import fr.bnia.commun.application.UseCase;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Règles d'architecture (ADR-002). Les packages suivent {@code fr.bnia.<contexte>.<couche>}, avec
 * pour couches {@code domain}, {@code application} et {@code adapter.<rest|persistence|integration>}.
 * {@code fr.bnia.commun} est le noyau partagé ; {@code fr.bnia.boot} le câblage.
 */
@AnalyzeClasses(packages = "fr.bnia", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

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
    static final ArchRule l_adapter_rest_ignore_les_autres_adapters = noClasses()
            .that().resideInAPackage("fr.bnia..adapter.rest..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("fr.bnia..adapter.persistence..", "fr.bnia..adapter.integration..");

    @ArchTest
    static final ArchRule l_adapter_persistence_ignore_les_autres_adapters = noClasses()
            .that().resideInAPackage("fr.bnia..adapter.persistence..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("fr.bnia..adapter.rest..", "fr.bnia..adapter.integration..");

    @ArchTest
    static final ArchRule l_adapter_integration_ignore_les_autres_adapters = noClasses()
            .that().resideInAPackage("fr.bnia..adapter.integration..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("fr.bnia..adapter.rest..", "fr.bnia..adapter.persistence..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule les_contextes_ne_dependent_pas_les_uns_des_autres = slices()
            .matching("fr.bnia.(*)..")
            .should().notDependOnEachOther()
            .ignoreDependency(alwaysTrue(), resideInAPackage("fr.bnia.commun.."))
            .ignoreDependency(resideInAPackage("fr.bnia.boot.."), alwaysTrue());

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
