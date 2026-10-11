package lavs.model.customer;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AddressTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    @Test
    void detailsCopiesEveryEditableField() {
        Address address = new Address(
                42, CUSTOMER, Address.Type.OTHER, "Holiday home", "1 Queen Street", "Flat 2",
                "Ponsonby", "Auckland", "1011", "New Zealand", true, false);

        Assertions.assertEquals(
                new Address.Details(
                        CUSTOMER, Address.Type.OTHER, "Holiday home", "1 Queen Street", "Flat 2",
                        "Ponsonby", "Auckland", "1011", "New Zealand", true, false),
                address.details());
    }

    @Nested
    class DetailsValidation {

        private static Address.Details details(Address.Type type, String otherType, String line1) {
            return new Address.Details(CUSTOMER, type, otherType, line1, null, null, null, null, null, false, false);
        }

        @Test
        void validAddressHasNoErrors() {
            Assertions.assertEquals(List.of(), details(Address.Type.RESIDENTIAL, null, "1 Queen Street").validate());
        }

        @Test
        void missingTypeIsRejected() {
            Assertions.assertEquals(
                    List.of("Address type is required."),
                    details(null, null, "1 Queen Street").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void otherTypeIsRequiredWhenTypeIsOther(String otherType) {
            Assertions.assertEquals(
                    List.of("Other address type is required."),
                    details(Address.Type.OTHER, otherType, "1 Queen Street").validate());
        }

        @Test
        void otherTypeIsAcceptedWhenTypeIsOther() {
            Assertions.assertEquals(
                    List.of(),
                    details(Address.Type.OTHER, "Holiday home", "1 Queen Street").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankLine1IsRejected(String line1) {
            Assertions.assertEquals(
                    List.of("Address line 1 is required."),
                    details(Address.Type.RESIDENTIAL, null, line1).validate());
        }

        @Test
        void missingCustomerNumberThrows() {
            Assertions.assertThrows(NullPointerException.class, () -> new Address.Details(
                    null, Address.Type.RESIDENTIAL, null, "1 Queen Street", null, null, null, null, null, false, false));
        }
    }
}
