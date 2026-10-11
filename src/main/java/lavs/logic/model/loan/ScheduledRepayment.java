package lavs.logic.model.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record ScheduledRepayment(
    LoanNumber loanNumber,
    long paymentNumber,
    LocalDate date,
    BigDecimal amountPaid,
    BigDecimal principalAmount,
    BigDecimal interestAmount,
    BigDecimal amountRemaining
) {
    public ScheduledRepayment {
        Objects.requireNonNull(loanNumber, "loanNumber");
    }
}
