package com.pswied.loan.strasbourg.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.pswied.loan.strasbourg");

    @Test
    void domainShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.pswied.loan.strasbourg.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.pswied.loan.strasbourg.infrastructure..");

        rule.check(CLASSES);
    }

    @Test
    void applicationShouldDependOnDomainAndNotOnInfrastructure() {
        ArchRule rule = classes()
                .that().resideInAPackage("com.pswied.loan.strasbourg.application..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "com.pswied.loan.strasbourg.application..",
                        "com.pswied.loan.strasbourg.domain..",
                        "java..",
                        "jakarta..",
                        "org.slf4j..",
                        "io.smallrye..",
                        "io.quarkus.scheduler..",
                        "org.eclipse.microprofile.."
                );

        rule.check(CLASSES);
    }

    @Test
    void interfacesShouldDependOnApplicationAndNotOnInfrastructure() {
        ArchRule rule = classes()
                .that().resideInAPackage("com.pswied.loan.strasbourg.interfaces..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "com.pswied.loan.strasbourg.interfaces..",
                        "com.pswied.loan.strasbourg.application..",
                        "com.pswied.loan.strasbourg.domain..",
                        "java..",
                        "jakarta..",
                        "org.eclipse.microprofile..",
                        "io.quarkus.."
                );

        rule.check(CLASSES);
    }
}
