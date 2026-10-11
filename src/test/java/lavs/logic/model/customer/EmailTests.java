package lavs.logic.model.customer;

import java.util.Arrays;
import java.util.List;
import lavs.logic.model.FieldError;
import mainframe.client.messages.customer.AddEmail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    @Test
    void detailsCopiesEveryEditableField() {
        Email email = new Email(42, CUSTOMER, Email.Type.OTHER, "Shared inbox", "ann@example.com", true);

        Assertions.assertEquals(
                new Email.Details(Email.Type.OTHER, "Shared inbox", "ann@example.com", true),
                email.details());
    }

    @Test
    void completeSummaryIsAccepted() {
        Assertions.assertEquals(CUSTOMER, new Email.Summary(42, CUSTOMER, Email.Type.WORK, "ann@example.com", true).customerNumber());
    }

    @Test
    void summaryRequiresCustomerNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new Email.Summary(42, null, Email.Type.WORK, "ann@example.com", true));
    }

    @Test
    void summaryRequiresType() {
        Assertions.assertThrows(NullPointerException.class, () -> new Email.Summary(42, CUSTOMER, null, "ann@example.com", true));
    }

    @Nested
    class DetailsValidation {

        private static Email.Details details(Email.Type type, String otherType, String address) {
            return new Email.Details(type, otherType, address, false);
        }

        @Test
        void validEmailHasNoErrors() {
            Assertions.assertEquals(List.of(), details(Email.Type.PERSONAL, null, "ann@example.com").validate());
        }

        @Test
        void missingTypeIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("type", "Email type is required.")),
                    details(null, null, "ann@example.com").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void otherTypeIsRequiredWhenTypeIsOther(String otherType) {
            Assertions.assertEquals(
                    List.of(new FieldError("otherType", "Other email type is required.")),
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
                    List.of(new FieldError("address", "Email address is required.")),
                    details(Email.Type.PERSONAL, null, address).validate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"ann@example.com", "ann.lee+loans@mail.example.co.nz"})
        void wellFormedAddressIsAccepted(String address) {
            Assertions.assertEquals(List.of(), details(Email.Type.PERSONAL, null, address).validate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"ann", "ann@", "@example.com", "ann@example", "ann lee@example.com", "ann@@example.com"})
        void malformedAddressIsRejected(String address) {
            Assertions.assertEquals(
                    List.of(new FieldError("address", "Email address must be in the format name@example.com.")),
                    details(Email.Type.PERSONAL, null, address).validate());
        }

        @Test
        void addressAtLimitIsAccepted() {
            String address = "a".repeat(48) + "@example.com";

            Assertions.assertEquals(List.of(), details(Email.Type.PERSONAL, null, address).validate());
        }

        @Test
        void addressOverLimitIsRejected() {
            String address = "a".repeat(49) + "@example.com";

            Assertions.assertEquals(
                    List.of(new FieldError("address", "Email address must be 60 characters or fewer.")),
                    details(Email.Type.PERSONAL, null, address).validate());
        }
    }

    @Nested
    class TypeCodes {

        @Test
        void codesMatchTheMainframeConstants() {
            Assertions.assertEquals(
                    List.of(
                            AddEmail.TYPE_PERSONAL,
                            AddEmail.TYPE_WORK,
                            AddEmail.TYPE_OTHER),
                    Arrays.stream(Email.Type.values())
                            .filter(type -> type != Email.Type.UNKNOWN)
                            .map(Email.Type::code)
                            .toList());
        }

        @ParameterizedTest
        @EnumSource(value = Email.Type.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
        void everyTypeIsFoundByItsCode(Email.Type type) {
            Assertions.assertEquals(type, Email.Type.fromCode(type.code()));
        }

        @Test
        void codeLookupIgnoresCase() {
            Assertions.assertEquals(Email.Type.PERSONAL, Email.Type.fromCode("personal"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Holiday", "UNKNOWN"})
        void unrecognisedCodeIsUnknown(String code) {
            Assertions.assertEquals(Email.Type.UNKNOWN, Email.Type.fromCode(code));
        }

        @Test
        void unknownTypeCannotBeSaved() {
            Assertions.assertEquals(
                    List.of(new FieldError("type", "Email type must be one of the listed options.")),
                    new Email.Details(Email.Type.UNKNOWN, null, "ann@example.com", false).validate());
        }
    }
}
