package lavs.logic.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidatorTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 12);

    @Test
    void noFailuresGivesNoErrors() {
        Assertions.assertEquals(List.of(), new Validator().required("name", "Ann", "Name").errors());
    }

    @Test
    void blankTextIsMissing() {
        Assertions.assertEquals(
                List.of(new FieldError("name", "Name is required.")),
                new Validator().required("name", "  ", "Name").errors());
    }

    @Test
    void nullObjectIsMissing() {
        Assertions.assertEquals(
                List.of(new FieldError("date", "Date is required.")),
                new Validator().required("date", (LocalDate) null, "Date").errors());
    }

    @Test
    void tooLongTextIsReported() {
        Assertions.assertEquals(
                List.of(new FieldError("title", "Title must be 10 characters or fewer.")),
                new Validator().maxLength("title", "a".repeat(11), 10, "Title").errors());
    }

    @Test
    void nonNumericTextIsReported() {
        Assertions.assertEquals(
                List.of(new FieldError("postCode", "Post code must contain only digits.")),
                new Validator().numeric("postCode", "10a1", "Post code").errors());
    }

    @Test
    void blankTextSkipsNumericCheck() {
        Assertions.assertEquals(List.of(), new Validator().numeric("postCode", "", "Post code").errors());
    }

    @Test
    void malformedEmailAddressIsReported() {
        Assertions.assertEquals(
                List.of(new FieldError("address", "Email address must be in the format name@example.com.")),
                new Validator().emailAddress("address", "ann@", "Email address").errors());
    }

    @Test
    void blankTextSkipsEmailAddressCheck() {
        Assertions.assertEquals(List.of(), new Validator().emailAddress("address", " ", "Email address").errors());
    }

    @Test
    void futureDateIsReported() {
        Assertions.assertEquals(
                List.of(new FieldError("dateOfBirth", "Date of birth must be in the past.")),
                new Validator().pastDate("dateOfBirth", TODAY, TODAY, "Date of birth").errors());
    }

    @Test
    void missingDateSkipsPastDateCheck() {
        Assertions.assertEquals(List.of(), new Validator().pastDate("dateOfBirth", null, TODAY, "Date of birth").errors());
    }

    @Test
    void zeroIsNotPositive() {
        Assertions.assertEquals(
                List.of(new FieldError("principal", "Principal must be greater than 0.")),
                new Validator().positive("principal", BigDecimal.ZERO, "Principal").errors());
    }

    @Test
    void missingAmountSkipsPositiveCheck() {
        Assertions.assertEquals(List.of(), new Validator().positive("principal", null, "Principal").errors());
    }

    @Test
    void negativeAmountIsReported() {
        Assertions.assertEquals(
                List.of(new FieldError("rateAmount", "Rate must be 0 or more.")),
                new Validator().notNegative("rateAmount", new BigDecimal("-0.01"), "Rate").errors());
    }

    @Test
    void zeroIsNotNegative() {
        Assertions.assertEquals(List.of(), new Validator().notNegative("rateAmount", BigDecimal.ZERO, "Rate").errors());
    }

    @Test
    void errorsAreKeptInRuleOrder() {
        List<FieldError> errors = new Validator()
                .required("first", null, "First")
                .check(true, "second", "Second failed.")
                .required("third", "", "Third")
                .errors();

        Assertions.assertEquals(
                List.of("first", "second", "third"),
                errors.stream().map(FieldError::field).toList());
    }

    @Test
    void errorsCannotBeModified() {
        List<FieldError> errors = new Validator().check(true, "field", "Failed.").errors();

        Assertions.assertThrows(UnsupportedOperationException.class, () -> errors.add(new FieldError("x", "y")));
    }
}
