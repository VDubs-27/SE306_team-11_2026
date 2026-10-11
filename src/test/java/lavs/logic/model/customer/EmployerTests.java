package lavs.logic.model.customer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lavs.logic.model.FieldError;
import mainframe.client.messages.customer.AddEmployer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmployerTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    @Test
    void detailsCopiesEveryEditableField() {
        Employer employer = new Employer(
                42, CUSTOMER, Employer.Type.SECONDARY, "Acme Ltd", "Engineer", new BigDecimal("85000.50"));

        Assertions.assertEquals(
                new Employer.Details(
                        Employer.Type.SECONDARY, "Acme Ltd", "Engineer", new BigDecimal("85000.50")),
                employer.details());
    }

    @Test
    void completeSummaryIsAccepted() {
        Assertions.assertEquals(CUSTOMER, new Employer.Summary(42, CUSTOMER, Employer.Type.PRIMARY, "Acme Ltd").customerNumber());
    }

    @Test
    void summaryRequiresCustomerNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new Employer.Summary(42, null, Employer.Type.PRIMARY, "Acme Ltd"));
    }

    @Test
    void summaryRequiresType() {
        Assertions.assertThrows(NullPointerException.class, () -> new Employer.Summary(42, CUSTOMER, null, "Acme Ltd"));
    }

    @Nested
    class DetailsValidation {

        private static Employer.Details details(Employer.Type type, String name) {
            return new Employer.Details(type, name, null, null);
        }

        @Test
        void onlyTypeAndNameAreRequired() {
            Assertions.assertEquals(List.of(), details(Employer.Type.PRIMARY, "Acme Ltd").validate());
        }

        @Test
        void missingTypeIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("type", "Employer type is required.")),
                    details(null, "Acme Ltd").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankNameIsRejected(String name) {
            Assertions.assertEquals(
                    List.of(new FieldError("name", "Employer name is required.")),
                    details(Employer.Type.PRIMARY, name).validate());
        }
    }

    @Nested
    class TypeCodes {

        @Test
        void codesMatchTheMainframeConstants() {
            Assertions.assertEquals(
                    List.of(
                            AddEmployer.TYPE_PRIMARY,
                            AddEmployer.TYPE_SECONDARY),
                    Arrays.stream(Employer.Type.values())
                            .filter(type -> type != Employer.Type.UNKNOWN)
                            .map(Employer.Type::code)
                            .toList());
        }

        @ParameterizedTest
        @EnumSource(value = Employer.Type.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
        void everyTypeIsFoundByItsCode(Employer.Type type) {
            Assertions.assertEquals(type, Employer.Type.fromCode(type.code()));
        }

        @Test
        void codeLookupIgnoresCase() {
            Assertions.assertEquals(Employer.Type.PRIMARY, Employer.Type.fromCode("primary"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Holiday", "UNKNOWN"})
        void unrecognisedCodeIsUnknown(String code) {
            Assertions.assertEquals(Employer.Type.UNKNOWN, Employer.Type.fromCode(code));
        }

        @Test
        void unknownTypeCannotBeSaved() {
            Assertions.assertEquals(
                    List.of(new FieldError("type", "Employer type must be one of the listed options.")),
                    new Employer.Details(Employer.Type.UNKNOWN, "Acme Ltd", null, null).validate());
        }
    }
}
