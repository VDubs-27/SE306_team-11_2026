package lavs.logic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lavs.logic.model.FieldError;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Loan;
import org.junit.jupiter.api.Test;

class LoanRulesTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final Constants CONSTANTS = new Constants(Map.of(
            ConstantCategory.RATE_TYPE, List.of("Fixed", "Floating"),
            ConstantCategory.RATE_FREQUENCY, List.of("Weekly", "Monthly"),
            ConstantCategory.PAYMENT_FREQUENCY, List.of("Weekly", "Monthly")));

    private static Loan.Details details(
            Loan.RateType rateType, Loan.RateFrequency rateFrequency, Loan.PaymentFrequency paymentFrequency) {
        return new Loan.Details(
                new BigDecimal("1000"), rateType, new BigDecimal("5"), rateFrequency, LocalDate.of(2025, 1, 2),
                new BigDecimal("100"), paymentFrequency, LocalDate.of(2030, 1, 2));
    }

    private static Loan.Details validDetails() {
        return details(Loan.RateType.FIXED, Loan.RateFrequency.WEEKLY, Loan.PaymentFrequency.MONTHLY);
    }

    @Test
    void validLoanHasNoErrors() {
        assertTrue(LoanRules.validate(CUSTOMER, validDetails(), CONSTANTS).isEmpty());
    }

    @Test
    void missingCustomerIsAFieldError() {
        assertEquals(List.of(LoanRules.CUSTOMER_REQUIRED), LoanRules.validate(null, validDetails(), CONSTANTS));
    }

    @Test
    void detailErrorsAreIncluded() {
        Loan.Details noPrincipal = new Loan.Details(
                null, Loan.RateType.FIXED, new BigDecimal("5"), Loan.RateFrequency.WEEKLY, LocalDate.of(2025, 1, 2),
                new BigDecimal("100"), Loan.PaymentFrequency.MONTHLY, LocalDate.of(2030, 1, 2));

        assertEquals(List.of(new FieldError("principal", "Principal is required.")),
                LoanRules.validate(CUSTOMER, noPrincipal, CONSTANTS));
    }

    @Test
    void optionsTheMainframeDoesNotOfferAreRejected() {
        List<FieldError> errors = LoanRules.validate(CUSTOMER,
                details(Loan.RateType.INTEREST_ONLY, Loan.RateFrequency.DAILY, Loan.PaymentFrequency.ON_RENEWAL),
                CONSTANTS);

        assertEquals(List.of(
                new FieldError("rateType", "Rate type must be one of the listed options."),
                new FieldError("rateFrequency", "Rate frequency must be one of the listed options."),
                new FieldError("paymentFrequency", "Payment frequency must be one of the listed options.")),
                errors);
    }

    @Test
    void unknownOptionIsReportedOnce() {
        List<FieldError> errors = LoanRules.validate(CUSTOMER,
                details(Loan.RateType.UNKNOWN, Loan.RateFrequency.WEEKLY, Loan.PaymentFrequency.MONTHLY), CONSTANTS);

        assertEquals(1, errors.stream().filter(error -> error.field().equals("rateType")).count());
    }
}
