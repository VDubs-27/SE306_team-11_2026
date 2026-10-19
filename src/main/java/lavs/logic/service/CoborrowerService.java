package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lavs.logic.adapter.CoborrowerGateway;
import lavs.logic.adapter.LoanGateway;
import lavs.logic.adapter.MainframeException;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;

// Table 1: a co-borrower is changed by deleting and re-adding it, which is what update does
public class CoborrowerService {

    static final FieldError IS_BORROWER =
            new FieldError("coborrowerNumber", "The loan's own customer cannot also be a co-borrower.");
    static final FieldError NOT_A_CUSTOMER =
            new FieldError("coborrowerNumber", "No customer has this number.");
    static final FieldError ALREADY_ON_LOAN =
            new FieldError("coborrowerNumber", "This customer is already on the loan.");
    static final String NOT_RESTORED_MESSAGE =
            "The co-borrower could not be changed, and the original could not be put back. Add them to the loan again.";

    private final CoborrowerGateway coborrowers;
    private final LoanGateway loans;
    private final Supplier<Constants> constants;

    public CoborrowerService(CoborrowerGateway coborrowers, LoanGateway loans, Supplier<Constants> constants) {
        this.coborrowers = Objects.requireNonNull(coborrowers, "coborrowers");
        this.loans = Objects.requireNonNull(loans, "loans");
        this.constants = Objects.requireNonNull(constants, "constants");
    }

    public Result<Page<Coborrower.Summary>> list(LoanNumber loan, long page, long size) {
        return Result.attempt(() -> Result.success(coborrowers.list(loan, page, size)));
    }

    public Result<Coborrower> get(LoanNumber loan, long id) {
        return Result.attempt(() -> Result.success(coborrowers.retrieve(loan, id)));
    }

    // Takes the whole loan, which the loan screen already holds, so the borrower check needs no extra call
    public Result<Coborrower> add(Loan loan, Coborrower.Details details) {
        List<FieldError> errors = validate(details);
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> {
            FieldError problem = lockProblem(loan.loanNumber());
            if (problem == null) {
                problem = membershipProblem(loan, details.coborrowerNumber(), Rules.NEW_RECORD);
            }
            if (problem != null) {
                return Result.invalid(List.of(problem));
            }
            return Result.success(coborrowers.add(loan.loanNumber(), details));
        });
    }

    // The original is deleted before the replacement is added, so changing only the type never puts the same
    // person on the loan twice. If the add then fails, the original is added back.
    public Result<Coborrower> update(Loan loan, long id, Coborrower.Details details) {
        List<FieldError> errors = validate(details);
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> {
            Coborrower original = coborrowers.retrieve(loan.loanNumber(), id);
            Coborrower.Details originalDetails = new Coborrower.Details(original.type(), original.coborrowerNumber());
            if (originalDetails.equals(details)) {
                return Result.success(original);
            }
            FieldError problem = lockProblem(loan.loanNumber());
            if (problem == null) {
                problem = membershipProblem(loan, details.coborrowerNumber(), id);
            }
            if (problem != null) {
                return Result.invalid(List.of(problem));
            }
            coborrowers.delete(loan.loanNumber(), id);
            return replace(loan.loanNumber(), originalDetails, details);
        });
    }

    public Result<Void> delete(LoanNumber loan, long id) {
        return Result.attempt(() -> {
            FieldError problem = lockProblem(loan);
            if (problem != null) {
                return Result.invalid(List.of(problem));
            }
            coborrowers.delete(loan, id);
            return Result.success(null);
        });
    }

    private List<FieldError> validate(Coborrower.Details details) {
        return Rules.withTypeCheck(
                details.validate(), constants.get(), ConstantCategory.COBORROWER_TYPE, details.type(),
                "Coborrower type");
    }

    private Result<Coborrower> replace(LoanNumber loan, Coborrower.Details original, Coborrower.Details details) {
        try {
            return Result.success(coborrowers.add(loan, details));
        } catch (MainframeException e) {
            try {
                coborrowers.add(loan, original);
            } catch (MainframeException restoreFailure) {
                return Result.failed(NOT_RESTORED_MESSAGE);
            }
            return Result.failed(e.getMessage());
        }
    }

    // The loan is read again, so people aren't changed on a loan submitted since the screen loaded it
    private FieldError lockProblem(LoanNumber loan) {
        return loans.retrieve(loan).isEditable() ? null : Rules.LOAN_LOCKED;
    }

    // Checked in order of cost: the loan's customer, then whether the customer exists, then the loan's people.
    // The record being replaced, if any, does not count as already being on the loan.
    private FieldError membershipProblem(Loan loan, CustomerNumber person, long replacing) {
        if (loan.customerNumber().equals(person)) {
            return IS_BORROWER;
        }
        if (!coborrowers.isCustomer(person)) {
            return NOT_A_CUSTOMER;
        }
        boolean onLoan = coborrowers.all(loan.loanNumber()).stream()
                .anyMatch(existing -> existing.id() != replacing && person.equals(existing.coborrowerNumber()));
        return onLoan ? ALREADY_ON_LOAN : null;
    }
}
