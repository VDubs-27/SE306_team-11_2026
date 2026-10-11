package lavs.logic.model.loan;

import java.util.Arrays;
import java.util.List;
import lavs.logic.model.FieldError;
import lavs.logic.model.customer.CustomerNumber;
import mainframe.client.messages.loan.AddCoborrower;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CoborrowerTests {

    private static final LoanNumber LOAN = new LoanNumber("01-0001-0000015-001");
    private static final CustomerNumber COBORROWER = new CustomerNumber("C456");

    @Test
    void completeCoborrowerIsAccepted() {
        Coborrower coborrower = new Coborrower(42, LOAN, Coborrower.Type.GUARANTOR, COBORROWER, "Bo Smith");

        Assertions.assertEquals(LOAN, coborrower.loanNumber());
    }

    @Test
    void coborrowerRequiresLoanNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new Coborrower(
                42, null, Coborrower.Type.GUARANTOR, COBORROWER, "Bo Smith"));
    }

    @Test
    void coborrowerRequiresType() {
        Assertions.assertThrows(NullPointerException.class, () -> new Coborrower(
                42, LOAN, null, COBORROWER, "Bo Smith"));
    }

    @Test
    void completeSummaryIsAccepted() {
        Coborrower.Summary summary = new Coborrower.Summary(42, LOAN, Coborrower.Type.COBORROWER, COBORROWER, "Bo");

        Assertions.assertEquals(LOAN, summary.loanNumber());
    }

    @Test
    void summaryRequiresLoanNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new Coborrower.Summary(
                42, null, Coborrower.Type.COBORROWER, COBORROWER, "Bo"));
    }

    @Test
    void summaryRequiresType() {
        Assertions.assertThrows(NullPointerException.class, () -> new Coborrower.Summary(
                42, LOAN, null, COBORROWER, "Bo"));
    }

    @Nested
    class DetailsValidation {

        @Test
        void completeCoborrowerHasNoErrors() {
            Assertions.assertEquals(List.of(), new Coborrower.Details(Coborrower.Type.GUARANTOR, COBORROWER).validate());
        }

        @Test
        void missingTypeIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("type", "Coborrower type is required.")),
                    new Coborrower.Details(null, COBORROWER).validate());
        }

        @Test
        void missingCoborrowerIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("coborrowerNumber", "Coborrower is required.")),
                    new Coborrower.Details(Coborrower.Type.COBORROWER, null).validate());
        }
    }

    @Nested
    class TypeCodes {

        @Test
        void codesMatchTheMainframeConstants() {
            Assertions.assertEquals(
                    List.of(AddCoborrower.TYPE_COBORROWER, AddCoborrower.TYPE_GUARANTOR),
                    Arrays.stream(Coborrower.Type.values())
                            .filter(type -> type != Coborrower.Type.UNKNOWN)
                            .map(Coborrower.Type::code)
                            .toList());
        }

        @ParameterizedTest
        @EnumSource(value = Coborrower.Type.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
        void everyTypeIsFoundByItsCode(Coborrower.Type type) {
            Assertions.assertEquals(type, Coborrower.Type.fromCode(type.code()));
        }

        @Test
        void codeLookupIgnoresCase() {
            Assertions.assertEquals(Coborrower.Type.GUARANTOR, Coborrower.Type.fromCode("guarantor"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Cosigner", "UNKNOWN"})
        void unrecognisedCodeIsUnknown(String code) {
            Assertions.assertEquals(Coborrower.Type.UNKNOWN, Coborrower.Type.fromCode(code));
        }

        @Test
        void unknownTypeCannotBeSaved() {
            Assertions.assertEquals(
                    List.of(new FieldError("type", "Coborrower type must be one of the listed options.")),
                    new Coborrower.Details(Coborrower.Type.UNKNOWN, COBORROWER).validate());
        }
    }
}
