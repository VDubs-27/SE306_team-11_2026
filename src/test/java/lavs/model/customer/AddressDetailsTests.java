package lavs.model.customer;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AddressDetailsTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    private static AddressDetails address(AddressType type, String otherType, String line1) {
        return new AddressDetails(CUSTOMER, type, otherType, line1, null, null, null, null, null, false, false);
    }

    @Test
    void validAddressHasNoErrors() {
        Assertions.assertEquals(List.of(), address(AddressType.RESIDENTIAL, null, "1 Queen Street").validate());
    }

    @Test
    void missingTypeIsRejected() {
        Assertions.assertEquals(
                List.of("Address type is required."),
                address(null, null, "1 Queen Street").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void otherTypeIsRequiredWhenTypeIsOther(String otherType) {
        Assertions.assertEquals(
                List.of("Other address type is required."),
                address(AddressType.OTHER, otherType, "1 Queen Street").validate());
    }

    @Test
    void otherTypeIsAcceptedWhenTypeIsOther() {
        Assertions.assertEquals(List.of(), address(AddressType.OTHER, "Holiday home", "1 Queen Street").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankLine1IsRejected(String line1) {
        Assertions.assertEquals(
                List.of("Address line 1 is required."),
                address(AddressType.RESIDENTIAL, null, line1).validate());
    }

    @Test
    void missingCustomerNumberThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new AddressDetails(
                null, AddressType.RESIDENTIAL, null, "1 Queen Street", null, null, null, null, null, false, false));
    }
}
