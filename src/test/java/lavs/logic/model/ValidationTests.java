package lavs.logic.model;

import java.math.BigDecimal;
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

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void dateOnOrBeforeOtherIsOnOrBefore(long daysAfter) {
        Assertions.assertTrue(Validation.isOnOrBefore(TODAY.plusDays(daysAfter), TODAY));
    }

    @Test
    void dateAfterOtherIsNotOnOrBefore() {
        Assertions.assertFalse(Validation.isOnOrBefore(TODAY.plusDays(1), TODAY));
    }

    @Test
    void missingDatesAreNotOnOrBefore() {
        Assertions.assertAll(
                () -> Assertions.assertFalse(Validation.isOnOrBefore(null, TODAY)),
                () -> Assertions.assertFalse(Validation.isOnOrBefore(TODAY, null))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "1", "1000000"})
    void amountsAboveZeroArePositive(String value) {
        Assertions.assertTrue(Validation.isPositive(new BigDecimal(value)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01"})
    void amountsOfZeroOrLessAreNotPositive(String value) {
        Assertions.assertFalse(Validation.isPositive(new BigDecimal(value)));
    }

    @Test
    void amountBelowZeroIsNegative() {
        Assertions.assertTrue(Validation.isNegative(new BigDecimal("-0.01")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "0.01"})
    void amountsOfZeroOrMoreAreNotNegative(String value) {
        Assertions.assertFalse(Validation.isNegative(new BigDecimal(value)));
    }

    @Test
    void missingAmountIsNeitherPositiveNorNegative() {
        Assertions.assertAll(
                () -> Assertions.assertFalse(Validation.isPositive(null)),
                () -> Assertions.assertFalse(Validation.isNegative(null))
        );
    }
}
