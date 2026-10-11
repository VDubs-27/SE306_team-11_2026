package lavs.logic.model.loan;

import static lavs.logic.model.Validation.isOnOrBefore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.Codes;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;
import lavs.logic.model.customer.CustomerNumber;

public record Loan(
    LoanNumber loanNumber,
    CustomerNumber customerNumber,
    Status status,
    BigDecimal principal,
    RateType rateType,
    BigDecimal rateAmount,
    RateFrequency rateFrequency,
    LocalDate startDate,
    BigDecimal paymentAmount,
    PaymentFrequency paymentFrequency,
    LocalDate renewalDate
) {
    public Loan {
        Objects.requireNonNull(loanNumber, "loanNumber");
        Objects.requireNonNull(customerNumber, "customerNumber");
    }

    public Loan withStatus(Status newStatus) {
        return new Loan(
            loanNumber, customerNumber, newStatus, principal, rateType, rateAmount, rateFrequency,
            startDate, paymentAmount, paymentFrequency, renewalDate);
    }

    public Details details() {
        return new Details(
            principal, rateType, rateAmount, rateFrequency, startDate, paymentAmount, paymentFrequency, renewalDate);
    }

    public enum Status {
        NEW,
        PENDING,
        ACTIVE,
        DEFAULTED,
        CANCELLED;

        public boolean canMoveTo(Status target) {
            return this == NEW && target == PENDING;
        }
    }

    public enum RateType {
        FIXED("Fixed"),
        FLOATING("Floating"),
        INTEREST_ONLY("InterestOnly"),
        UNKNOWN("");

        private final String code;

        RateType(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static RateType fromCode(String code) {
            return Codes.fromCode(values(), RateType::code, code, UNKNOWN);
        }
    }

    public enum RateFrequency {
        DAILY("Daily"),
        WEEKLY("Weekly"),
        FORTNIGHTLY("Fortnightly"),
        MONTHLY("Monthly"),
        YEARLY("Yearly"),
        UNKNOWN("");

        private final String code;

        RateFrequency(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static RateFrequency fromCode(String code) {
            return Codes.fromCode(values(), RateFrequency::code, code, UNKNOWN);
        }
    }

    public enum PaymentFrequency {
        WEEKLY("Weekly"),
        FORTNIGHTLY("Fortnightly"),
        MONTHLY("Monthly"),
        YEARLY("Yearly"),
        ON_RENEWAL("Onrenewal"),
        UNKNOWN("");

        private final String code;

        PaymentFrequency(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static PaymentFrequency fromCode(String code) {
            return Codes.fromCode(values(), PaymentFrequency::code, code, UNKNOWN);
        }
    }

    public record Summary(
        LoanNumber loanNumber,
        CustomerNumber customerNumber,
        Status status,
        BigDecimal principal,
        LocalDate startDate
    ) {
        public Summary {
            Objects.requireNonNull(loanNumber, "loanNumber");
        }
    }

    public record Totals(
        LoanNumber loanNumber,
        BigDecimal totalAmountPaid,
        BigDecimal totalInterestPaid,
        long numberOfPayments,
        LocalDate finalPaymentDate,
        BigDecimal finalPaymentAmount
    ) {
        public Totals {
            Objects.requireNonNull(loanNumber, "loanNumber");
        }
    }

    public record Details(
        BigDecimal principal,
        RateType rateType,
        BigDecimal rateAmount,
        RateFrequency rateFrequency,
        LocalDate startDate,
        BigDecimal paymentAmount,
        PaymentFrequency paymentFrequency,
        LocalDate renewalDate
    ) {
        public List<FieldError> validate() {
            return new Validator()
                .required("principal", principal, "Principal")
                .positive("principal", principal, "Principal")
                .required("rateType", rateType, "Rate type")
                .check(rateType == RateType.UNKNOWN, "rateType", "Rate type must be one of the listed options.")
                .required("rateAmount", rateAmount, "Rate")
                .notNegative("rateAmount", rateAmount, "Rate")
                .required("rateFrequency", rateFrequency, "Rate frequency")
                .check(
                    rateFrequency == RateFrequency.UNKNOWN,
                    "rateFrequency", "Rate frequency must be one of the listed options.")
                .required("startDate", startDate, "Start date")
                .required("paymentAmount", paymentAmount, "Payment amount")
                .required("paymentFrequency", paymentFrequency, "Payment frequency")
                .check(
                    paymentFrequency == PaymentFrequency.UNKNOWN,
                    "paymentFrequency", "Payment frequency must be one of the listed options.")
                .required("renewalDate", renewalDate, "Renewal date")
                .check(
                    isOnOrBefore(renewalDate, startDate),
                    "renewalDate", "Renewal date must be after the start date.")
                .errors();
        }
    }
}
