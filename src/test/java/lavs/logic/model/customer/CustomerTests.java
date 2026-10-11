package lavs.logic.model.customer;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lavs.logic.model.FieldError;
import mainframe.client.messages.customer.UpdateStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CustomerTests {

    private static final CustomerNumber NUMBER = new CustomerNumber("C123");
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 17);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 12);

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
    void withStatusRejectsUnknown() {
        Customer active = customer("Ann", "Lee", Customer.Status.ACTIVE);

        Assertions.assertThrows(IllegalArgumentException.class, () -> active.withStatus(Customer.Status.UNKNOWN));
    }

    @Test
    void withStatusRejectsNull() {
        Customer active = customer("Ann", "Lee", Customer.Status.ACTIVE);

        Assertions.assertThrows(IllegalArgumentException.class, () -> active.withStatus(null));
    }

    @Nested
    class StatusCodes {

        @Test
        void codesMatchTheMainframeConstants() {
            Assertions.assertEquals(
                    List.of(UpdateStatus.STATUS_ACTIVE, UpdateStatus.STATUS_INACTIVE),
                    Arrays.stream(Customer.Status.values())
                            .filter(status -> status != Customer.Status.UNKNOWN)
                            .map(Customer.Status::code)
                            .toList());
        }

        @ParameterizedTest
        @EnumSource(value = Customer.Status.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
        void everyStatusIsFoundByItsCode(Customer.Status status) {
            Assertions.assertEquals(status, Customer.Status.fromCode(status.code()));
        }

        @Test
        void codeLookupIgnoresCase() {
            Assertions.assertEquals(Customer.Status.INACTIVE, Customer.Status.fromCode("INACTIVE"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Suspended", "UNKNOWN"})
        void unrecognisedCodeIsUnknown(String code) {
            Assertions.assertEquals(Customer.Status.UNKNOWN, Customer.Status.fromCode(code));
        }
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

        private static Customer.Details names(String firstName, String familyName) {
            return new Customer.Details(null, firstName, familyName, null, null);
        }

        @Test
        void onlyNamesAreRequired() {
            Assertions.assertEquals(List.of(), names("Ann", "Lee").validate(TODAY));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankFirstNameIsRejected(String firstName) {
            Assertions.assertEquals(
                    List.of(new FieldError("firstName", "First name is required.")),
                    names(firstName, "Lee").validate(TODAY));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankFamilyNameIsRejected(String familyName) {
            Assertions.assertEquals(
                    List.of(new FieldError("familyName", "Family name is required.")),
                    names("Ann", familyName).validate(TODAY));
        }

        @Test
        void everyMissingFieldIsReported() {
            Assertions.assertEquals(
                    List.of(
                            new FieldError("firstName", "First name is required."),
                            new FieldError("familyName", "Family name is required.")),
                    names(null, null).validate(TODAY));
        }

        @Test
        void textFieldsAtLimitAreAccepted() {
            Customer.Details details = new Customer.Details(
                    "a".repeat(10), "a".repeat(40), "a".repeat(40), null, "a".repeat(40));

            Assertions.assertEquals(List.of(), details.validate(TODAY));
        }

        @Test
        void titleOverLimitIsRejected() {
            Customer.Details details = new Customer.Details("a".repeat(11), "Ann", "Lee", null, null);

            Assertions.assertEquals(
                    List.of(new FieldError("title", "Title must be 10 characters or fewer.")),
                    details.validate(TODAY));
        }

        @Test
        void firstNameOverLimitIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("firstName", "First name must be 40 characters or fewer.")),
                    names("a".repeat(41), "Lee").validate(TODAY));
        }

        @Test
        void familyNameOverLimitIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("familyName", "Family name must be 40 characters or fewer.")),
                    names("Ann", "a".repeat(41)).validate(TODAY));
        }

        @Test
        void nationalityOverLimitIsRejected() {
            Customer.Details details = new Customer.Details(null, "Ann", "Lee", null, "a".repeat(41));

            Assertions.assertEquals(
                    List.of(new FieldError("nationality", "Nationality must be 40 characters or fewer.")),
                    details.validate(TODAY));
        }

        @Test
        void dateOfBirthInThePastIsAccepted() {
            Customer.Details details = new Customer.Details(null, "Ann", "Lee", TODAY.minusDays(1), null);

            Assertions.assertEquals(List.of(), details.validate(TODAY));
        }

        @ParameterizedTest
        @ValueSource(longs = {0, 1})
        void dateOfBirthTodayOrLaterIsRejected(long daysAfterToday) {
            Customer.Details details = new Customer.Details(null, "Ann", "Lee", TODAY.plusDays(daysAfterToday), null);

            Assertions.assertEquals(
                    List.of(new FieldError("dateOfBirth", "Date of birth must be in the past.")),
                    details.validate(TODAY));
        }
    }
}
