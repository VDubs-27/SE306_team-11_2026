package lavs.logic.service;

import java.util.ArrayList;
import java.util.List;
import lavs.logic.model.FieldError;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Loan;

// Table 1 loan rules: all nine input fields are required, and the rate type and both frequencies
// must be values the mainframe offered at login. The loan service runs this before a create or update.
final class LoanRules {

    static final FieldError CUSTOMER_REQUIRED = new FieldError("customerNumber", "Customer is required.");

    private LoanRules() {
    }

    static List<FieldError> validate(CustomerNumber customer, Loan.Details details, Constants constants) {
        List<FieldError> errors = new ArrayList<>();
        if (customer == null) {
            errors.add(CUSTOMER_REQUIRED);
        }
        errors.addAll(details.validate());
        errors = Rules.withOptionCheck(
                errors, constants, ConstantCategory.RATE_TYPE, details.rateType(), "rateType", "Rate type");
        errors = Rules.withOptionCheck(errors, constants, ConstantCategory.RATE_FREQUENCY, details.rateFrequency(),
                "rateFrequency", "Rate frequency");
        return Rules.withOptionCheck(errors, constants, ConstantCategory.PAYMENT_FREQUENCY,
                details.paymentFrequency(), "paymentFrequency", "Payment frequency");
    }
}
