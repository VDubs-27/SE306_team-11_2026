package lavs.logic.model.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ScheduledRepaymentTests {

    private static final LoanNumber LOAN = new LoanNumber("01-0001-0000015-001");
    private static final BigDecimal AMOUNT = new BigDecimal("100.00");

    @Test
    void completeRepaymentIsAccepted() {
        ScheduledRepayment repayment = new ScheduledRepayment(
                LOAN, 1, LocalDate.of(2025, 1, 9), AMOUNT, AMOUNT, BigDecimal.ZERO, AMOUNT);

        Assertions.assertEquals(LOAN, repayment.loanNumber());
    }

    @Test
    void repaymentRequiresLoanNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new ScheduledRepayment(
                null, 1, LocalDate.of(2025, 1, 9), AMOUNT, AMOUNT, BigDecimal.ZERO, AMOUNT));
    }
}
