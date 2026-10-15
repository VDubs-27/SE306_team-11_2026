package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lavs.logic.adapter.CoborrowerGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;

// Table 1: a co-borrower is changed by deleting and re-adding it, so there is no update
public class CoborrowerService {

    static final FieldError IS_BORROWER =
            new FieldError("coborrowerNumber", "The loan's own customer cannot also be a co-borrower.");
    static final FieldError NOT_A_CUSTOMER =
            new FieldError("coborrowerNumber", "No customer has this number.");
    static final FieldError ALREADY_ON_LOAN =
            new FieldError("coborrowerNumber", "This customer is already on the loan.");

    private final CoborrowerGateway coborrowers;
    private final Supplier<Constants> constants;

    public CoborrowerService(CoborrowerGateway coborrowers, Supplier<Constants> constants) {
        this.coborrowers = Objects.requireNonNull(coborrowers, "coborrowers");
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
        List<FieldError> errors = Rules.withTypeCheck(
                details.validate(), constants.get(), ConstantCategory.COBORROWER_TYPE, details.type(),
                "Coborrower type");
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> {
            FieldError problem = membershipProblem(loan, details.coborrowerNumber());
            if (problem != null) {
                return Result.invalid(List.of(problem));
            }
            return Result.success(coborrowers.add(loan.loanNumber(), details));
        });
    }

    public Result<Void> delete(LoanNumber loan, long id) {
        return Result.attempt(() -> {
            coborrowers.delete(loan, id);
            return Result.success(null);
        });
    }

    // Checked in order of cost: the loan's customer, then whether the customer exists, then the loan's people
    private FieldError membershipProblem(Loan loan, CustomerNumber person) {
        if (loan.customerNumber().equals(person)) {
            return IS_BORROWER;
        }
        if (!coborrowers.isCustomer(person)) {
            return NOT_A_CUSTOMER;
        }
        boolean onLoan = coborrowers.all(loan.loanNumber()).stream()
                .anyMatch(existing -> person.equals(existing.coborrowerNumber()));
        return onLoan ? ALREADY_ON_LOAN : null;
    }
}
