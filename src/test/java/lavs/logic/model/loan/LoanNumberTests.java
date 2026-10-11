package lavs.logic.model.loan;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LoanNumberTests {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankValueIsRejected(String value) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LoanNumber(value));
    }

    @Test
    void toStringReturnsRawValue() {
        Assertions.assertEquals("01-0001-0000015-001", new LoanNumber("01-0001-0000015-001").toString());
    }
}
