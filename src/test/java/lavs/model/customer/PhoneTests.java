package lavs.model.customer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PhoneTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    @Test
    void detailsCopiesEveryEditableField() {
        Phone phone = new Phone(42, CUSTOMER, PhoneType.OTHER, "Pager", "+64", "21 123 4567", true, false);

        Assertions.assertEquals(
                new PhoneDetails(CUSTOMER, PhoneType.OTHER, "Pager", "+64", "21 123 4567", true, false),
                phone.details());
    }
}
