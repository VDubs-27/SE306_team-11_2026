package lavs.logic.service;

import lavs.logic.adapter.AddressGateway;
import lavs.logic.adapter.CoborrowerGateway;
import lavs.logic.adapter.CustomerGateway;
import lavs.logic.adapter.EmailGateway;
import lavs.logic.adapter.EmployerGateway;
import lavs.logic.adapter.LoanGateway;
import lavs.logic.adapter.PhoneGateway;
import lavs.logic.adapter.RepaymentGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import lavs.logic.model.loan.ScheduledRepayment;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class LoanService {

    private final LoanGateway loanGateway;
    private final RepaymentGateway repaymentGateway;
    private final LoanStartRequirement loanStartRequirement;
    private final SubmitRequirement submitRequirement;
    private final Supplier<Constants> constants;

    public LoanService(LoanGateway loanGateway, RepaymentGateway repaymentGateway, CustomerGateway customerGateway,
                       AddressGateway addressGateway, PhoneGateway phoneGateway, EmailGateway emailGateway,
                       EmployerGateway employerGateway, CoborrowerGateway coborrowerGateway,
                       Supplier<Constants> constants) {
        this.loanGateway = Objects.requireNonNull(loanGateway, "loanGateway");
        this.repaymentGateway = Objects.requireNonNull(repaymentGateway, "repaymentGateway");
        AddressGateway addresses = Objects.requireNonNull(addressGateway, "addressGateway");
        this.loanStartRequirement = new LoanStartRequirement(
                Objects.requireNonNull(customerGateway, "customerGateway"), addresses,
                new ContactRequirement(Objects.requireNonNull(phoneGateway, "phoneGateway"),
                        Objects.requireNonNull(emailGateway, "emailGateway")));
        this.submitRequirement = new SubmitRequirement(addresses,
                Objects.requireNonNull(employerGateway, "employerGateway"),
                Objects.requireNonNull(coborrowerGateway, "coborrowerGateway"));
        this.constants = Objects.requireNonNull(constants, "constants");
    }

    public Result<Void> canStartLoan(CustomerNumber customerNumber) {
        return Result.attempt(() -> {
           List<FieldError> problems = loanStartRequirement.problems(customerNumber);
           if (!problems.isEmpty()) {
               return Result.invalid(problems);
           }
           return Result.success(null);
        });
    }

    // The page size is the caller's, because each loan screen shows a different number of rows
    public Result<Page<Loan.Summary>> listForCustomer(CustomerNumber customerNumber, long page, long size) {
        return Result.attempt(() -> Result.success(loanGateway.list(customerNumber, page, size)));
    }

    public Result<Loan> retrieve(LoanNumber loanNumber) {
        return Result.attempt(() -> Result.success(loanGateway.retrieve(loanNumber)));
    }

    public Result<Loan.Totals> totals(LoanNumber loanNumber) {
        return Result.attempt(() -> Result.success(repaymentGateway.totals(loanNumber)));
    }

    public Result<Page<ScheduledRepayment>> schedule(LoanNumber loanNumber, long page, long size) {
        return Result.attempt(() -> Result.success(repaymentGateway.schedule(loanNumber, page, size)));
    }

    public Result<LoanNumber> create(CustomerNumber customerNumber, Loan.Details loanDetails) {
        List<FieldError> errors = validate(customerNumber, loanDetails);
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> {
            List<FieldError> problems = loanStartRequirement.problems(customerNumber);
            if (!problems.isEmpty()) {
                return Result.invalid(problems);
            }
            return Result.success(loanGateway.create(customerNumber, loanDetails));
        });
    }

    public Result<Loan> update(Loan loan, Loan.Details loanDetails) {
        List<FieldError> errors = validate(loan.customerNumber(), loanDetails);
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> Result.success(loanGateway.update(loan, loanDetails)));
    }

    // Table 1: the only status change an MMM can make is New to Pending. The loan is read again first, so the
    // check uses its current status rather than whatever the screen loaded earlier.
    public Result<Loan> submit(LoanNumber loanNumber) {
        return Result.attempt(() -> {
            Loan loan = loanGateway.retrieve(loanNumber);
            List<FieldError> problems = submitRequirement.problems(loan);
            if (!problems.isEmpty()) {
                return Result.invalid(problems);
            }
            loanGateway.updateStatus(loanNumber, Loan.Status.PENDING);
            return Result.success(loan.withStatus(Loan.Status.PENDING));
        });
    }

    private List<FieldError> validate(CustomerNumber customerNumber, Loan.Details loanDetails) {
        return LoanRules.validate(customerNumber, loanDetails, constants.get());
    }
}
