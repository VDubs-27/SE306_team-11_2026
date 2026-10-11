package lavs.model.customer;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    @Test
    void detailsCopiesEveryEditableField() {
        Phone phone = new Phone(42, CUSTOMER, Phone.Type.OTHER, "Pager", "+64", "21 123 4567", true, false);

        Assertions.assertEquals(
                new Phone.Details(CUSTOMER, Phone.Type.OTHER, "Pager", "+64", "21 123 4567", true, false),
                phone.details());
    }

    @Nested
    class DetailsValidation {

        private static Phone.Details details(Phone.Type type, String otherType, String number) {
            return new Phone.Details(CUSTOMER, type, otherType, "+64", number, false, false);
        }

        @Test
        void validPhoneHasNoErrors() {
            Assertions.assertEquals(List.of(), details(Phone.Type.MOBILE, null, "21 123 4567").validate());
        }

        @Test
        void missingTypeIsRejected() {
            Assertions.assertEquals(
                    List.of("Phone type is required."),
                    details(null, null, "21 123 4567").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void otherTypeIsRequiredWhenTypeIsOther(String otherType) {
            Assertions.assertEquals(
                    List.of("Other phone type is required."),
                    details(Phone.Type.OTHER, otherType, "21 123 4567").validate());
        }

        @Test
        void otherTypeIsAcceptedWhenTypeIsOther() {
            Assertions.assertEquals(List.of(), details(Phone.Type.OTHER, "Pager", "21 123 4567").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankNumberIsRejected(String number) {
            Assertions.assertEquals(
                    List.of("Phone number is required."),
                    details(Phone.Type.MOBILE, null, number).validate());
        }

        @Test
        void missingCustomerNumberThrows() {
            Assertions.assertThrows(NullPointerException.class, () -> new Phone.Details(
                    null, Phone.Type.MOBILE, null, "+64", "21 123 4567", false, false));
        }
    }
}
