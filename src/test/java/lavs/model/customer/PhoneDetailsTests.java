package lavs.model.customer;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneDetailsTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    private static PhoneDetails phone(PhoneType type, String otherType, String number) {
        return new PhoneDetails(CUSTOMER, type, otherType, "+64", number, false, false);
    }

    @Test
    void validPhoneHasNoErrors() {
        Assertions.assertEquals(List.of(), phone(PhoneType.MOBILE, null, "21 123 4567").validate());
    }

    @Test
    void missingTypeIsRejected() {
        Assertions.assertEquals(
                List.of("Phone type is required."),
                phone(null, null, "21 123 4567").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void otherTypeIsRequiredWhenTypeIsOther(String otherType) {
        Assertions.assertEquals(
                List.of("Other phone type is required."),
                phone(PhoneType.OTHER, otherType, "21 123 4567").validate());
    }

    @Test
    void otherTypeIsAcceptedWhenTypeIsOther() {
        Assertions.assertEquals(List.of(), phone(PhoneType.OTHER, "Pager", "21 123 4567").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankNumberIsRejected(String number) {
        Assertions.assertEquals(
                List.of("Phone number is required."),
                phone(PhoneType.MOBILE, null, number).validate());
    }

    @Test
    void missingCustomerNumberThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new PhoneDetails(
                null, PhoneType.MOBILE, null, "+64", "21 123 4567", false, false));
    }
}
