package lavs.logic.model.loan;

import static lavs.logic.model.Validation.isBlank;

public record LoanNumber(String value) {
    public LoanNumber {
        if (isBlank(value)) {
            throw new IllegalArgumentException("Loan number must not be null or blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
