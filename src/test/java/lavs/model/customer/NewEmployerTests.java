package lavs.model.customer;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NewEmployerTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    private static NewEmployer employer(EmployerType type, String name) {
        return new NewEmployer(CUSTOMER, type, name, null, null);
    }

    @Test
    void onlyTypeAndNameAreRequired() {
        Assertions.assertEquals(List.of(), employer(EmployerType.PRIMARY, "Acme Ltd").validate());
    }

    @Test
    void missingTypeIsRejected() {
        Assertions.assertEquals(
                List.of("Employer type is required."),
                employer(null, "Acme Ltd").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankNameIsRejected(String name) {
        Assertions.assertEquals(
                List.of("Employer name is required."),
                employer(EmployerType.PRIMARY, name).validate());
    }

    @Test
    void missingCustomerNumberThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new NewEmployer(
                null, EmployerType.PRIMARY, "Acme Ltd", "Engineer", new BigDecimal("85000")));
    }
}
