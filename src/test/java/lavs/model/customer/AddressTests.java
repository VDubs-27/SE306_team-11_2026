package lavs.model.customer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AddressTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    @Test
    void detailsCopiesEveryEditableField() {
        Address address = new Address(
                42, CUSTOMER, AddressType.OTHER, "Holiday home", "1 Queen Street", "Flat 2",
                "Ponsonby", "Auckland", "1011", "New Zealand", true, false);

        Assertions.assertEquals(
                new AddressDetails(
                        CUSTOMER, AddressType.OTHER, "Holiday home", "1 Queen Street", "Flat 2",
                        "Ponsonby", "Auckland", "1011", "New Zealand", true, false),
                address.details());
    }
}
