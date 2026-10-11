package lavs.model.customer;

import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CustomerTests {

    private static final CustomerNumber NUMBER = new CustomerNumber("C123");
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 17);

    private static Customer customer(String firstName, String familyName, CustomerStatus status) {
        return new Customer(NUMBER, "Ms", firstName, familyName, status, DATE_OF_BIRTH, "New Zealand");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankFirstNameIsRejected(String firstName) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> customer(firstName, "Lee", CustomerStatus.ACTIVE));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankFamilyNameIsRejected(String familyName) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> customer("Ann", familyName, CustomerStatus.ACTIVE));
    }

    @Test
    void missingNumberThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new Customer(
                null, "Ms", "Ann", "Lee", CustomerStatus.ACTIVE, DATE_OF_BIRTH, "New Zealand"));
    }

    @Test
    void fullNameJoinsFirstAndFamilyName() {
        Assertions.assertEquals("Ann Lee", customer("Ann", "Lee", CustomerStatus.ACTIVE).fullName());
    }

    @Test
    void withStatusChangesOnlyTheStatus() {
        Customer active = customer("Ann", "Lee", CustomerStatus.ACTIVE);

        Customer inactive = active.withStatus(CustomerStatus.INACTIVE);

        Assertions.assertAll(
                () -> Assertions.assertEquals(customer("Ann", "Lee", CustomerStatus.INACTIVE), inactive),
                () -> Assertions.assertEquals(CustomerStatus.ACTIVE, active.status(), "original should be unchanged")
        );
    }

    @Test
    void detailsCopiesEveryEditableField() {
        Assertions.assertEquals(
                new CustomerDetails("Ms", "Ann", "Lee", DATE_OF_BIRTH, "New Zealand"),
                customer("Ann", "Lee", CustomerStatus.ACTIVE).details());
    }
}
