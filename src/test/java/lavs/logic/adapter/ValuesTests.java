package lavs.logic.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
        assertNull(Values.decimal(null));
    }

    @Test
    void missingAmountIsSentAsZero() {
        assertEquals(0, Values.toDouble(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankDateIsNull(String value) {
        assertNull(Values.date(value));
    }

    @Test
    void plainDateIsParsed() {
        assertEquals(LocalDate.of(2026, 10, 5), Values.date("2026-10-05"));
    }

    @Test
    void timestampKeepsItsDate() {
        assertEquals(LocalDate.of(2026, 10, 16), Values.date("2026-10-16T12:13:36.4120000+13:00"));
    }

    @Test
    void unreadableDateThrows() {
        assertThrows(DateTimeParseException.class, () -> Values.date("05/10/2026"));
    }
}
