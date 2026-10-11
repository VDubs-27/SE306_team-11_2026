package lavs.logic.model.customer;

import java.util.Arrays;
import java.util.List;
import lavs.logic.model.FieldError;
import mainframe.client.messages.customer.AddAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AddressTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");
    private static final String LINE1 = "1 Queen Street";
    private static final String COUNTRY = "New Zealand";

    @Test
    void detailsCopiesEveryEditableField() {
        Address address = new Address(
                42, CUSTOMER, Address.Type.OTHER, "Holiday home", "1 Queen Street", "Flat 2",
                "Ponsonby", "Auckland", "1011", "New Zealand", true, false);

        Assertions.assertEquals(
                new Address.Details(
                        Address.Type.OTHER, "Holiday home", "1 Queen Street", "Flat 2",
                        "Ponsonby", "Auckland", "1011", "New Zealand", true, false),
                address.details());
    }

    @Test
    void completeSummaryIsAccepted() {
        Assertions.assertEquals(CUSTOMER, new Address.Summary(42, CUSTOMER, Address.Type.POSTAL, "Auckland", "New Zealand", true, true).customerNumber());
    }

    @Test
    void summaryRequiresCustomerNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new Address.Summary(42, null, Address.Type.POSTAL, "Auckland", "New Zealand", true, true));
    }

    @Test
    void summaryRequiresType() {
        Assertions.assertThrows(NullPointerException.class, () -> new Address.Summary(42, CUSTOMER, null, "Auckland", "New Zealand", true, true));
    }

    @Nested
    class DetailsValidation {

        private static Address.Details details(Address.Type type, String otherType, String line1, String country) {
            return new Address.Details(type, otherType, line1, null, null, null, null, country, false, false);
        }

        private static Address.Details withPostCode(String postCode) {
            return new Address.Details(
                    Address.Type.RESIDENTIAL, null, LINE1, null, null, null, postCode, COUNTRY, false, false);
        }

        private static Address.Details withText(String field, String value) {
            return switch (field) {
                case "otherType" -> new Address.Details(
                        Address.Type.OTHER, value, LINE1, null, null, null, null, COUNTRY, false, false);
                case "line1" -> new Address.Details(
                        Address.Type.RESIDENTIAL, null, value, null, null, null, null, COUNTRY, false, false);
                case "line2" -> new Address.Details(
                        Address.Type.RESIDENTIAL, null, LINE1, value, null, null, null, COUNTRY, false, false);
                case "suburb" -> new Address.Details(
                        Address.Type.RESIDENTIAL, null, LINE1, null, value, null, null, COUNTRY, false, false);
                case "city" -> new Address.Details(
                        Address.Type.RESIDENTIAL, null, LINE1, null, null, value, null, COUNTRY, false, false);
                case "country" -> new Address.Details(
                        Address.Type.RESIDENTIAL, null, LINE1, null, null, null, null, value, false, false);
                default -> throw new IllegalArgumentException(field);
            };
        }

        @Test
        void validAddressHasNoErrors() {
            Assertions.assertEquals(List.of(), details(Address.Type.RESIDENTIAL, null, LINE1, COUNTRY).validate());
        }

        @Test
        void missingTypeIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("type", "Address type is required.")),
                    details(null, null, LINE1, COUNTRY).validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void otherTypeIsRequiredWhenTypeIsOther(String otherType) {
            Assertions.assertEquals(
                    List.of(new FieldError("otherType", "Other address type is required.")),
                    details(Address.Type.OTHER, otherType, LINE1, COUNTRY).validate());
        }

        @Test
        void otherTypeIsAcceptedWhenTypeIsOther() {
            Assertions.assertEquals(List.of(), details(Address.Type.OTHER, "Holiday home", LINE1, COUNTRY).validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankLine1IsRejected(String line1) {
            Assertions.assertEquals(
                    List.of(new FieldError("line1", "Address line 1 is required.")),
                    details(Address.Type.RESIDENTIAL, null, line1, COUNTRY).validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankCountryIsRejected(String country) {
            Assertions.assertEquals(
                    List.of(new FieldError("country", "Country is required.")),
                    details(Address.Type.RESIDENTIAL, null, LINE1, country).validate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"otherType", "line1", "line2", "suburb", "city", "country"})
        void textFieldAtLimitIsAccepted(String field) {
            Assertions.assertEquals(List.of(), withText(field, "a".repeat(60)).validate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"otherType", "line1", "line2", "suburb", "city", "country"})
        void textFieldOverLimitIsRejected(String field) {
            List<FieldError> errors = withText(field, "a".repeat(61)).validate();

            Assertions.assertAll(
                    () -> Assertions.assertEquals(List.of(field), errors.stream().map(FieldError::field).toList()),
                    () -> Assertions.assertTrue(errors.get(0).message().endsWith(" must be 60 characters or fewer."))
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankPostCodeIsAccepted(String postCode) {
            Assertions.assertEquals(List.of(), withPostCode(postCode).validate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"1011", "0600"})
        void numericPostCodeIsAccepted(String postCode) {
            Assertions.assertEquals(List.of(), withPostCode(postCode).validate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"10a1", "10 11", "-101", "SW1A"})
        void nonNumericPostCodeIsRejected(String postCode) {
            Assertions.assertEquals(
                    List.of(new FieldError("postCode", "Post code must contain only digits.")),
                    withPostCode(postCode).validate());
        }

        @Test
        void postCodeAtLimitIsAccepted() {
            Assertions.assertEquals(List.of(), withPostCode("1".repeat(60)).validate());
        }

        @Test
        void postCodeOverLimitIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("postCode", "Post code must be 60 characters or fewer.")),
                    withPostCode("1".repeat(61)).validate());
        }
    }

    @Nested
    class TypeCodes {

        @Test
        void codesMatchTheMainframeConstants() {
            Assertions.assertEquals(
                    List.of(
                            AddAddress.TYPE_RESIDENTIAL,
                            AddAddress.TYPE_POSTAL,
                            AddAddress.TYPE_FORWARDING,
                            AddAddress.TYPE_DELEGATED,
                            AddAddress.TYPE_OTHER),
                    Arrays.stream(Address.Type.values())
                            .filter(type -> type != Address.Type.UNKNOWN)
                            .map(Address.Type::code)
                            .toList());
        }

        @ParameterizedTest
        @EnumSource(value = Address.Type.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
        void everyTypeIsFoundByItsCode(Address.Type type) {
            Assertions.assertEquals(type, Address.Type.fromCode(type.code()));
        }

        @Test
        void codeLookupIgnoresCase() {
            Assertions.assertEquals(Address.Type.RESIDENTIAL, Address.Type.fromCode("residential"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Holiday", "UNKNOWN"})
        void unrecognisedCodeIsUnknown(String code) {
            Assertions.assertEquals(Address.Type.UNKNOWN, Address.Type.fromCode(code));
        }

        @Test
        void unknownTypeCannotBeSaved() {
            Assertions.assertEquals(
                    List.of(new FieldError("type", "Address type must be one of the listed options.")),
                    new Address.Details(Address.Type.UNKNOWN, null, "1 Queen Street", null, null, null, null, "New Zealand", false, false).validate());
        }
    }
}
