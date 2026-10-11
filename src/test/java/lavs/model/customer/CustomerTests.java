package lavs.model.customer;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CustomerTests {

    private static final CustomerNumber NUMBER = new CustomerNumber("C123");
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 17);

    private static Customer customer(String firstName, String familyName, Customer.Status status) {
        return new Customer(NUMBER, "Ms", firstName, familyName, status, DATE_OF_BIRTH, "New Zealand");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankFirstNameIsRejected(String firstName) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> customer(firstName, "Lee", Customer.Status.ACTIVE));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankFamilyNameIsRejected(String familyName) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> customer("Ann", familyName, Customer.Status.ACTIVE));
    }

    @Test
    void missingNumberThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new Customer(
                null, "Ms", "Ann", "Lee", Customer.Status.ACTIVE, DATE_OF_BIRTH, "New Zealand"));
    }

    @Test
    void fullNameJoinsFirstAndFamilyName() {
        Assertions.assertEquals("Ann Lee", customer("Ann", "Lee", Customer.Status.ACTIVE).fullName());
    }

    @Test
    void withStatusChangesOnlyTheStatus() {
        Customer active = customer("Ann", "Lee", Customer.Status.ACTIVE);

        Customer inactive = active.withStatus(Customer.Status.INACTIVE);

        Assertions.assertAll(
                () -> Assertions.assertEquals(customer("Ann", "Lee", Customer.Status.INACTIVE), inactive),
                () -> Assertions.assertEquals(Customer.Status.ACTIVE, active.status(), "original should be unchanged")
        );
    }

    @Test
    void detailsCopiesEveryEditableField() {
        Assertions.assertEquals(
                new Customer.Details("Ms", "Ann", "Lee", DATE_OF_BIRTH, "New Zealand"),
                customer("Ann", "Lee", Customer.Status.ACTIVE).details());
    }

    @Test
    void summaryFullNameJoinsFirstAndFamilyName() {
        Customer.Summary summary = new Customer.Summary(
                NUMBER, "Ms", "Ann", "Lee", DATE_OF_BIRTH, Customer.Status.ACTIVE);

        Assertions.assertEquals("Ann Lee", summary.fullName());
    }

    @Nested
    class DetailsValidation {

        private static Customer.Details details(String firstName, String familyName) {
            return new Customer.Details(null, firstName, familyName, null, null);
        }

        @Test
        void onlyNamesAreRequired() {
            Assertions.assertEquals(List.of(), details("Ann", "Lee").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankFirstNameIsRejected(String firstName) {
            Assertions.assertEquals(
                    List.of("First name is required."),
                    details(firstName, "Lee").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankFamilyNameIsRejected(String familyName) {
            Assertions.assertEquals(
                    List.of("Family name is required."),
                    details("Ann", familyName).validate());
        }

        @Test
        void everyMissingFieldIsReported() {
            Assertions.assertEquals(
                    List.of("First name is required.", "Family name is required."),
                    details(null, null).validate());
        }
    }
}
