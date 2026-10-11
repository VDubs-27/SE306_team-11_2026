package lavs.model.customer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmailTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    @Test
    void detailsCopiesEveryEditableField() {
        Email email = new Email(42, CUSTOMER, EmailType.OTHER, "Shared inbox", "ann@example.com", true);

        Assertions.assertEquals(
                new EmailDetails(CUSTOMER, EmailType.OTHER, "Shared inbox", "ann@example.com", true),
                email.details());
    }
}
