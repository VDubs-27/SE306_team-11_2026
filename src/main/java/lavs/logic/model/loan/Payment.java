package lavs.logic.model.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

public record Payment(
    long id,
    LoanNumber loanNumber,
    LocalDate date,
    BigDecimal amountPaid,
    BigDecimal principalAmount,
    BigDecimal interestAmount,
    BigDecimal amountRemaining
) {
    public Payment {
        Objects.requireNonNull(loanNumber, "loanNumber");
    }

    public record Summary(
        long id,
        LoanNumber loanNumber,
        LocalDate date,
        BigDecimal amountPaid
    ) {
        public Summary {
            Objects.requireNonNull(loanNumber, "loanNumber");
        }
    }

    public record Details(
        LocalDate date,
        BigDecimal amountPaid
    ) {
        public List<FieldError> validate() {
            return new Validator()
                .required("date", date, "Payment date")
                .required("amountPaid", amountPaid, "Amount paid")
                .errors();
        }
    }
}
