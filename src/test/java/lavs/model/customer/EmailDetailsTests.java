package lavs.model.customer;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailDetailsTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    private static EmailDetails email(EmailType type, String otherType, String address) {
        return new EmailDetails(CUSTOMER, type, otherType, address, false);
    }

    @Test
    void validEmailHasNoErrors() {
        Assertions.assertEquals(List.of(), email(EmailType.PERSONAL, null, "ann@example.com").validate());
    }

    @Test
    void missingTypeIsRejected() {
        Assertions.assertEquals(
                List.of("Email type is required."),
                email(null, null, "ann@example.com").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void otherTypeIsRequiredWhenTypeIsOther(String otherType) {
        Assertions.assertEquals(
                List.of("Other email type is required."),
                email(EmailType.OTHER, otherType, "ann@example.com").validate());
    }

    @Test
    void otherTypeIsAcceptedWhenTypeIsOther() {
        Assertions.assertEquals(List.of(), email(EmailType.OTHER, "Shared inbox", "ann@example.com").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankAddressIsRejected(String address) {
        Assertions.assertEquals(
                List.of("Email address is required."),
                email(EmailType.PERSONAL, null, address).validate());
    }

    @Test
    void missingCustomerNumberThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new EmailDetails(
                null, EmailType.PERSONAL, null, "ann@example.com", false));
    }
}
