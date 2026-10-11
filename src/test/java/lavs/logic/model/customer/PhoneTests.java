package lavs.logic.model.customer;

import java.util.Arrays;
import java.util.List;
import lavs.logic.model.FieldError;
import mainframe.client.messages.customer.AddPhoneNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");

    @Test
    void detailsCopiesEveryEditableField() {
        Phone phone = new Phone(42, CUSTOMER, Phone.Type.OTHER, "Pager", "+64", "21 123 4567", true, false);

        Assertions.assertEquals(
                new Phone.Details(Phone.Type.OTHER, "Pager", "+64", "21 123 4567", true, false),
                phone.details());
    }

    @Test
    void completeSummaryIsAccepted() {
        Assertions.assertEquals(CUSTOMER, new Phone.Summary(42, CUSTOMER, Phone.Type.MOBILE, "+64", "21 123 4567", true, true).customerNumber());
    }

    @Test
    void summaryRequiresCustomerNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new Phone.Summary(42, null, Phone.Type.MOBILE, "+64", "21 123 4567", true, true));
    }

    @Test
    void summaryRequiresType() {
        Assertions.assertThrows(NullPointerException.class, () -> new Phone.Summary(42, CUSTOMER, null, "+64", "21 123 4567", true, true));
    }

    @Nested
    class DetailsValidation {

        private static Phone.Details details(Phone.Type type, String otherType, String countryCode, String number) {
            return new Phone.Details(type, otherType, countryCode, number, false, false);
        }

        @Test
        void validPhoneHasNoErrors() {
            Assertions.assertEquals(List.of(), details(Phone.Type.MOBILE, null, "+64", "21 123 4567").validate());
        }

        @Test
        void missingTypeIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("type", "Phone type is required.")),
                    details(null, null, "+64", "21 123 4567").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void otherTypeIsRequiredWhenTypeIsOther(String otherType) {
            Assertions.assertEquals(
                    List.of(new FieldError("otherType", "Other phone type is required.")),
                    details(Phone.Type.OTHER, otherType, "+64", "21 123 4567").validate());
        }

        @Test
        void otherTypeIsAcceptedWhenTypeIsOther() {
            Assertions.assertEquals(List.of(), details(Phone.Type.OTHER, "Pager", "+64", "21 123 4567").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankCountryCodeIsRejected(String countryCode) {
            Assertions.assertEquals(
                    List.of(new FieldError("countryCode", "Country code is required.")),
                    details(Phone.Type.MOBILE, null, countryCode, "21 123 4567").validate());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankNumberIsRejected(String number) {
            Assertions.assertEquals(
                    List.of(new FieldError("number", "Phone number is required.")),
                    details(Phone.Type.MOBILE, null, "+64", number).validate());
        }
    }

    @Nested
    class TypeCodes {

        @Test
        void codesMatchTheMainframeConstants() {
            Assertions.assertEquals(
                    List.of(
                            AddPhoneNumber.TYPE_HOME,
                            AddPhoneNumber.TYPE_MOBILE,
                            AddPhoneNumber.TYPE_WORK,
                            AddPhoneNumber.TYPE_OTHER),
                    Arrays.stream(Phone.Type.values())
                            .filter(type -> type != Phone.Type.UNKNOWN)
                            .map(Phone.Type::code)
                            .toList());
        }

        @ParameterizedTest
        @EnumSource(value = Phone.Type.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
        void everyTypeIsFoundByItsCode(Phone.Type type) {
            Assertions.assertEquals(type, Phone.Type.fromCode(type.code()));
        }

        @Test
        void codeLookupIgnoresCase() {
            Assertions.assertEquals(Phone.Type.MOBILE, Phone.Type.fromCode("mobile"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Holiday", "UNKNOWN"})
        void unrecognisedCodeIsUnknown(String code) {
            Assertions.assertEquals(Phone.Type.UNKNOWN, Phone.Type.fromCode(code));
        }

        @Test
        void unknownTypeCannotBeSaved() {
            Assertions.assertEquals(
                    List.of(new FieldError("type", "Phone type must be one of the listed options.")),
                    new Phone.Details(Phone.Type.UNKNOWN, null, "+64", "21 123 4567", false, false).validate());
        }
    }
}
