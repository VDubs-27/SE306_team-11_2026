package lavs.logic.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ValuesTests {

    @ParameterizedTest
    @ValueSource(strings = {"y", "Y"})
    void yesFlagsAreTrue(String flag) {
        assertTrue(Values.isYes(flag));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"n", "yes", "true"})
    void otherFlagsAreFalse(String flag) {
        assertFalse(Values.isYes(flag));
    }

    @Test
    void decimalKeepsTheValue() {
        assertEquals(new BigDecimal("65000.5"), Values.decimal(65000.5));
    }

    @Test
    void missingDecimalStaysMissing() {
        assertNull(Values.decimal((Double) null));
    }

    @Test
    void missingAmountIsSentAsZero() {
        assertEquals(0, Values.toDouble(null));
    }

    @Test
    void decimalTextKeepsItsDigits() {
        assertEquals(new BigDecimal("250000.00"), Values.decimal(" 250000.00 "));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankDecimalTextIsMissing(String blank) {
        assertNull(Values.decimal(blank));
    }

    @Test
    void dateIsReadFromIsoText() {
        assertEquals(LocalDate.of(2026, 1, 15), Values.date("2026-01-15"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankDateIsMissing(String blank) {
        assertNull(Values.date(blank));
    }

    @Test
    void dateIsSentAsIsoText() {
        assertEquals("2026-01-15", Values.text(LocalDate.of(2026, 1, 15)));
    }

    @Test
    void missingDateIsSentAsNothing() {
        assertNull(Values.text(null));
    }

    @Test
    void requiredAmountIsSentAsADouble() {
        assertEquals(250000.5, Values.amount(new BigDecimal("250000.5")));
    }

    @Test
    void missingRequiredAmountFails() {
        assertThrows(NullPointerException.class, () -> Values.amount(null));
    }
}
