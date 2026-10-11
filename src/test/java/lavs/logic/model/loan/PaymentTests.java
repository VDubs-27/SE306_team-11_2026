package lavs.logic.model.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lavs.logic.model.FieldError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PaymentTests {

    private static final LoanNumber LOAN = new LoanNumber("01-0001-0000015-001");
    private static final LocalDate DATE = LocalDate.of(2025, 2, 3);
    private static final BigDecimal AMOUNT = new BigDecimal("100.00");

    @Test
    void completePaymentIsAccepted() {
        Payment payment = new Payment(42, LOAN, DATE, AMOUNT, new BigDecimal("97.66"), new BigDecimal("2.34"),
                new BigDecimal("1136.90"));

        Assertions.assertEquals(LOAN, payment.loanNumber());
    }

    @Test
    void paymentRequiresLoanNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new Payment(
                42, null, DATE, AMOUNT, null, null, null));
    }

    @Test
    void completeSummaryIsAccepted() {
        Assertions.assertEquals(LOAN, new Payment.Summary(42, LOAN, DATE, AMOUNT).loanNumber());
    }

    @Test
    void summaryRequiresLoanNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new Payment.Summary(42, null, DATE, AMOUNT));
    }

    @Nested
    class DetailsValidation {

        @Test
        void completePaymentHasNoErrors() {
            Assertions.assertEquals(List.of(), new Payment.Details(DATE, AMOUNT).validate());
        }

        @Test
        void missingDateIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("date", "Payment date is required.")),
                    new Payment.Details(null, AMOUNT).validate());
        }

        @Test
        void missingAmountIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("amountPaid", "Amount paid is required.")),
                    new Payment.Details(DATE, null).validate());
        }
    }
}
