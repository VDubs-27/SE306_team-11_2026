package lavs.logic.model.customer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CustomerProfileTests {

    private static final CustomerNumber NUMBER = new CustomerNumber("C123");
    private static final Customer CUSTOMER = new Customer(
            NUMBER, "Ms", "Ann", "Lee", Customer.Status.ACTIVE, LocalDate.of(1990, 5, 17), "New Zealand");
    private static final Address.Summary ADDRESS = new Address.Summary(
            1L, NUMBER, Address.Type.RESIDENTIAL, "Auckland", "New Zealand", true, true);

    @Test
    void keepsEveryPart() {
        CustomerProfile profile = new CustomerProfile(CUSTOMER, List.of(ADDRESS), List.of(), List.of(), List.of());

        Assertions.assertEquals(CUSTOMER, profile.customer());
        Assertions.assertEquals(List.of(ADDRESS), profile.addresses());
    }

    @Test
    void missingCustomerThrows() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new CustomerProfile(null, List.of(), List.of(), List.of(), List.of()));
    }

    @Test
    void listsAreCopiedOnConstruction() {
        List<Address.Summary> addresses = new ArrayList<>(List.of(ADDRESS));
        CustomerProfile profile = new CustomerProfile(CUSTOMER, addresses, List.of(), List.of(), List.of());

        addresses.add(ADDRESS);

        Assertions.assertEquals(1, profile.addresses().size());
    }
}