package lavs.model.customer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CustomerNumberTests {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankValueIsRejected(String value) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CustomerNumber(value));
    }

    @Test
    void toStringReturnsRawValue() {
        Assertions.assertEquals("C123", new CustomerNumber("C123").toString());
    }
}
