package lavs;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class ArchitectureTests {

    private static final JavaClasses LAVS_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("lavs");

    @Test
    void onlyTheAdapterUsesTheMainframeClient() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("lavs.logic.adapter..")
                .should().dependOnClassesThat().resideInAPackage("mainframe..")
                .because("all mainframe calls go through MainframeAdapter");

        rule.check(LAVS_CLASSES);
    }

    @Test
    void uiDoesNotUseTheAdapter() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("lavs.ui..")
                .should().dependOnClassesThat().resideInAPackage("lavs.logic.adapter..")
                .because("the UI talks to services, and services call MainframeAdapter");

        rule.check(LAVS_CLASSES);
    }
}
