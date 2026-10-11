package lavs.logic.model;

import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidationTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 12);

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankValuesAreBlank(String value) {
        Assertions.assertTrue(Validation.isBlank(value));
    }

    @Test
    void textIsNotBlank() {
        Assertions.assertFalse(Validation.isBlank(" a "));
    }

    @Test
    void lengthBelowMaxIsAllowed() {
        Assertions.assertFalse(Validation.exceedsLength("a".repeat(9), 10));
    }

    @Test
    void lengthAtMaxIsAllowed() {
        Assertions.assertFalse(Validation.exceedsLength("a".repeat(10), 10));
    }

    @Test
    void lengthAboveMaxExceeds() {
        Assertions.assertTrue(Validation.exceedsLength("a".repeat(11), 10));
    }

    @Test
    void nullDoesNotExceedLength() {
        Assertions.assertFalse(Validation.exceedsLength(null, 10));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1011", "0800"})
    void digitsAreNumeric(String value) {
        Assertions.assertTrue(Validation.isNumeric(value));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "10 11", "1011a", "-1", "1.5", "١٢"})
    void nonDigitsAreNotNumeric(String value) {
        Assertions.assertFalse(Validation.isNumeric(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ann@example.com", "a@b.co", "ann.lee+loans@mail.example.co.nz"})
    void wellFormedAddressesAreEmailAddresses(String value) {
        Assertions.assertTrue(Validation.isEmailAddress(value));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ann", "ann@", "@example.com", "ann@example", "ann lee@example.com", "ann@@example.com"})
    void malformedAddressesAreNotEmailAddresses(String value) {
        Assertions.assertFalse(Validation.isEmailAddress(value));
    }

    @Test
    void yesterdayIsPast() {
        Assertions.assertTrue(Validation.isPastDate(TODAY.minusDays(1), TODAY));
    }

    @Test
    void todayIsNotPast() {
        Assertions.assertFalse(Validation.isPastDate(TODAY, TODAY));
    }

    @Test
    void tomorrowIsNotPast() {
        Assertions.assertFalse(Validation.isPastDate(TODAY.plusDays(1), TODAY));
    }

    @Test
    void nullIsNotPast() {
        Assertions.assertFalse(Validation.isPastDate(null, TODAY));
    }
}
