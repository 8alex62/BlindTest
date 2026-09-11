package com.esgi.blindTest.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * Regle de dependance vers l'interieur : domain <- adapter <- infra / presentation / security.
 *
 * Ecart assume et documente dans le README : le domaine porte @Component sur ses use cases
 * et Lombok sur ses modeles, comme le projet de reference. Ces deux points ne sont donc
 * volontairement pas couverts par les regles ci-dessous.
 */
@AnalyzeClasses(packages = "com.esgi.blindTest",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule le_domaine_ne_depend_daucune_couche_exterieure =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..adapter..", "..infra..", "..presentation..", "..security..")
                    .because("la dependance pointe vers l'interieur, jamais vers l'exterieur");

    @ArchTest
    static final ArchRule le_domaine_ignore_jpa_mapstruct_et_le_web =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("jakarta.persistence..", "jakarta.servlet..",
                            "org.mapstruct..", "org.springframework.data..",
                            "org.springframework.web..", "io.jsonwebtoken..")
                    .because("le domaine ne connait aucune technologie de persistance ni de transport");

    @ArchTest
    static final ArchRule aucune_entite_jpa_hors_de_linfrastructure =
            noClasses().that().resideInAnyPackage("..domain..", "..presentation..", "..security..")
                    .should().dependOnClassesThat().resideInAPackage("..infra.persistance.entity..")
                    .because("aucune entite JPA ne sort de la couche infrastructure");

    @ArchTest
    static final ArchRule aucun_dto_web_dans_le_domaine =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..presentation.request..", "..presentation.response..")
                    .because("aucun DTO web n'entre dans la couche application");

    @ArchTest
    static final ArchRule la_presentation_ignore_linfrastructure =
            noClasses().that().resideInAPackage("..presentation..")
                    .should().dependOnClassesThat().resideInAPackage("..infra..")
                    .because("la presentation passe par les use cases et leurs ports");

    @ArchTest
    static final ArchRule injection_par_constructeur =
            noFields().that().areDeclaredInClassesThat()
                    .haveSimpleNameNotEndingWith("MapperImpl")
                    .should().beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
                    .because("l'injection se fait par constructeur, jamais par champ"
                            + " (les implementations generees par MapStruct sont hors du perimetre)");

    @ArchTest
    static final ArchRule les_use_cases_nexposent_que_apply =
            methods().that().areDeclaredInClassesThat().resideInAPackage("..domain.usecase..")
                    .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("UseCase")
                    .and().arePublic()
                    .should().haveName("apply")
                    .because("un use case expose une seule methode publique");

    @ArchTest
    static final ArchRule les_implementations_de_ports_sont_dans_adapter_repository =
            classes().that().haveSimpleNameEndingWith("RepositoryImpl")
                    .should().resideInAPackage("..adapter.repository..");

    @ArchTest
    static final ArchRule les_adapters_de_use_case_sont_regroupes =
            classes().that().haveSimpleNameEndingWith("Adapter")
                    .should().resideInAPackage("..adapter.usecase_adapter..");

    @ArchTest
    static final ArchRule les_champs_des_composants_sont_finaux =
            fields().that().areDeclaredInClassesThat().resideInAPackage("..adapter.usecase_adapter..")
                    .and().areNotStatic()
                    .should().beFinal()
                    .because("les dependances injectees par constructeur sont immuables");
}
