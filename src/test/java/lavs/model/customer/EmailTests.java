package lavs.model.customer;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    @Test
    void detailsCopiesEveryEditableField() {
        Email email = new Email(42, CUSTOMER, Email.Type.OTHER, "Shared inbox", "ann@example.com", true);

        Assertions.assertEquals(
                new Email.Details(CUSTOMER, Email.Type.OTHER, "Shared inbox", "ann@example.com", true),
                email.details());
    }

    @Nested
    class DetailsValidation {

        private static Email.Details details(Email.Type type, String otherType, String address) {
            return new Email.Details(CUSTOMER, type, otherType, address, false);
        }

        @Test
        void validEmailHasNoErrors() {
            Assertions.assertEquals(List.of(), details(Email.Type.PERSONAL, null, "ann@example.com").validate());
        }

        @Test
        void missingTypeIsRejected() {
            Assertions.assertEquals(
                    List.of("Email type is required."),
                    details(null, null, "ann@example.com").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void otherTypeIsRequiredWhenTypeIsOther(String otherType) {
            Assertions.assertEquals(
                    List.of("Other email type is required."),
                    details(Email.Type.OTHER, otherType, "ann@example.com").validate());
        }

        @Test
        void otherTypeIsAcceptedWhenTypeIsOther() {
            Assertions.assertEquals(
                    List.of(),
                    details(Email.Type.OTHER, "Shared inbox", "ann@example.com").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankAddressIsRejected(String address) {
            Assertions.assertEquals(
                    List.of("Email address is required."),
                    details(Email.Type.PERSONAL, null, address).validate());
        }

        @Test
        void missingCustomerNumberThrows() {
            Assertions.assertThrows(NullPointerException.class, () -> new Email.Details(
                    null, Email.Type.PERSONAL, null, "ann@example.com", false));
        }
    }
}
