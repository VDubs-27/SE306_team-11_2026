package lavs.logic.model.loan;

import java.util.List;
import lavs.logic.model.FieldError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LoanDocumentTests {

    private static final LoanNumber LOAN = new LoanNumber("01-0001-0000015-001");

    @Test
    void completeDocumentIsAccepted() {
        LoanDocument document = new LoanDocument(42, LOAN, "Proof of Identity", "passport.pdf", "application/pdf", 123);

        Assertions.assertEquals(LOAN, document.loanNumber());
    }

    @Test
    void detailsCopiesEveryEditableField() {
        LoanDocument document = new LoanDocument(42, LOAN, "Proof of Identity", "passport.pdf", "application/pdf", 123);

        Assertions.assertEquals(
                new LoanDocument.Details("Proof of Identity", "passport.pdf", "application/pdf", 123),
                document.details());
    }

    @Test
    void documentRequiresLoanNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new LoanDocument(
                42, null, "Proof of Identity", "passport.pdf", "application/pdf", 123));
    }

    @Test
    void completeSummaryIsAccepted() {
        LoanDocument.Summary summary = new LoanDocument.Summary(42, LOAN, "Proof of Identity", "passport.pdf");

        Assertions.assertEquals(LOAN, summary.loanNumber());
    }

    @Test
    void summaryRequiresLoanNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new LoanDocument.Summary(
                42, null, "Proof of Identity", "passport.pdf"));
    }

    @Nested
    class DetailsValidation {

        @ParameterizedTest
        @ValueSource(longs = {0, 1, 5_000_000})
        void completeDetailsAreValid(long size) {
            LoanDocument.Details details =
                    new LoanDocument.Details("Proof of Identity", "passport.pdf", "application/pdf", size);

            Assertions.assertTrue(details.validate().isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankFieldsAreRequired(String blank) {
            LoanDocument.Details details = new LoanDocument.Details(blank, blank, blank, 123);

            Assertions.assertEquals(List.of(
                    new FieldError("type", "Document type is required."),
                    new FieldError("fileName", "File name is required."),
                    new FieldError("fileType", "File type is required.")), details.validate());
        }

        @Test
        void negativeSizeIsRejected() {
            LoanDocument.Details details =
                    new LoanDocument.Details("Proof of Identity", "passport.pdf", "application/pdf", -1);

            Assertions.assertEquals(List.of(new FieldError("size", "File size must be 0 or more.")),
                    details.validate());
        }
    }
}
