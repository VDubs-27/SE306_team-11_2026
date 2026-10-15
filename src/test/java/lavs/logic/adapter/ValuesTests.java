package lavs.logic.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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
}
