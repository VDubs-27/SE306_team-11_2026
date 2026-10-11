package lavs.model.customer;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
                        CUSTOMER, Employer.Type.SECONDARY, "Acme Ltd", "Engineer", new BigDecimal("85000.50")),
                employer.details());
    }

    @Nested
    class DetailsValidation {

        private static Employer.Details details(Employer.Type type, String name) {
            return new Employer.Details(CUSTOMER, type, name, null, null);
        }

        @Test
        void onlyTypeAndNameAreRequired() {
            Assertions.assertEquals(List.of(), details(Employer.Type.PRIMARY, "Acme Ltd").validate());
        }

        @Test
        void missingTypeIsRejected() {
            Assertions.assertEquals(
                    List.of("Employer type is required."),
                    details(null, "Acme Ltd").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankNameIsRejected(String name) {
            Assertions.assertEquals(
                    List.of("Employer name is required."),
                    details(Employer.Type.PRIMARY, name).validate());
        }

        @Test
        void missingCustomerNumberThrows() {
            Assertions.assertThrows(NullPointerException.class, () -> new Employer.Details(
                    null, Employer.Type.PRIMARY, "Acme Ltd", "Engineer", new BigDecimal("85000")));
        }
    }
}
