package lavs.model.customer;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmployerTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    @Test
    void detailsCopiesEveryEditableField() {
        Employer employer = new Employer(
                42, CUSTOMER, EmployerType.SECONDARY, "Acme Ltd", "Engineer", new BigDecimal("85000.50"));

        Assertions.assertEquals(
                new EmployerDetails(
                        CUSTOMER, EmployerType.SECONDARY, "Acme Ltd", "Engineer", new BigDecimal("85000.50")),
                employer.details());
    }
}
