package io.artur.bank.customer;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class PackageStructureValidationTest {

    private ImportOption importOption = location -> !location.contains("/test-classes");
    private JavaClasses classes = new ClassFileImporter().withImportOption(importOption).importPackages("io.artur.bank");
    private String baseModule = "..base..";
    private String customerModule = "..customer..";

    private String domainPackage = "..domain..";
    private String applicationPackage = "..application..";
    private String apiPackage = "..api..";
    private String infrastructurePackage = "..infrastructure..";
    private String akkaPackage = "..akka..";

    @Test
    void shouldCheckDependenciesForDomainPackage() {
        ArchRule domainRules = noClasses()
                .that()
                .resideInAPackage(domainPackage)
                .should()
                .accessClassesThat()
                .resideInAPackage(applicationPackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(apiPackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(akkaPackage);

        domainRules.check(classes);
    }

    @Test
    public void shouldCheckDependenciesForApplicationPackage() {
        // given
        ArchRule applicationRules = noClasses()
                .that()
                .resideInAPackage(applicationPackage)
                .should()
                .accessClassesThat()
                .resideInAPackage(apiPackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(infrastructurePackage);

        // when // then
        applicationRules.check(classes);
    }


    @Test
    void shouldCheckDependenciesForApiPackage() {
        ArchRule apiRules = noClasses()
                .that()
                .resideInAPackage(apiPackage)
                .should()
                .accessClassesThat()
                .resideInAPackage(infrastructurePackage);

        apiRules.check(classes);
    }

    @Test
    public void shouldDependenciesForBasePackage() {
        // given
        ArchRule baseModuleRules = noClasses()
                .that()
                .resideInAPackage(baseModule)
                .should()
                .accessClassesThat()
                .resideInAPackage(customerModule);

        // when // then
        baseModuleRules.check(classes);
    }
}
