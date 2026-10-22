package lavs.ui.loan.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import lavs.logic.service.Result;
import lavs.ui.common.Fetch;
import lavs.ui.loan.people.LoanPerson;
import lavs.ui.shell.LoanServices;

/**
 * Sends what the new loan wizard collected. Leaving Loan details sends the loan: it is created the
 * first time and updated after that, so going back and forth never makes a second loan. Submitting
 * adds its co-borrowers and guarantors one by one, then submits it for approval; if one of them
 * fails, the rest still goes ahead and what didn't is listed, and a loan that can't be submitted yet
 * stays New, to submit from the loan screen. Run it in the background.
 * AI-assisted: written by Jack, refined with Claude.
 */
final class LoanSaver {

    private LoanSaver() {
    }

    /**
     * A loan just submitted: as it is now, what couldn't be added to it (e.g. "Guarantor Jane Doe: …"),
     * and why it couldn't be submitted, or null when it was.
     */
    record Saved(Loan loan, List<String> notSaved, String notSubmitted) {
    }

    /**
     * Sends the draft's loan details: creates the loan if it doesn't exist yet, otherwise updates it.
     * Nothing is sent when the details haven't changed since they were last sent.
     */
    static Result<Loan> send(LoanServices services, NewLoanDraft draft) {
        Loan loan = draft.loan();
        if (loan == null) {
            return create(services, draft);
        }
        if (loan.details().equals(draft.details())) {
            return Result.success(loan);
        }
        return services.loans().update(loan, draft.details());
    }

    /** Adds the draft's people to the loan already sent, then submits it; always gives the loan as it ends up. */
    static Result<Saved> save(LoanServices services, NewLoanDraft draft) {
        Loan loan = draft.loan();
        List<String> notSaved = new ArrayList<>();
        addEach(draft.others(), person -> services.coborrowers().add(loan,
                new Coborrower.Details(person.role(), person.customer().number())), LoanSaver::name, notSaved);

        Result<Loan> submitted = services.loans().submit(loan.loanNumber());
        if (submitted.successful()) {
            return Result.success(new Saved(submitted.value(), notSaved, null));
        }
        Result<Loan> read = services.loans().retrieve(loan.loanNumber());
        return Result.success(new Saved(read.successful() ? read.value() : loan, notSaved, Fetch.reason(submitted)));
    }

    /** The loan as just created and read back, or why it couldn't be created. */
    private static Result<Loan> create(LoanServices services, NewLoanDraft draft) {
        CustomerNumber customer = draft.applicant().customer().number();
        Result<LoanNumber> created = services.loans().create(customer, draft.details());
        if (!created.successful()) {
            return new Result<>(null, created.fieldErrors(), created.errorMessage());
        }
        Result<Loan> read = services.loans().retrieve(created.value());
        return Result.success(read.successful() ? read.value() : asCreated(created.value(), customer, draft.details()));
    }

    /** Adds each one, noting any that fail with who or what it was and why. */
    private static <T> void addEach(List<T> items, Function<T, Result<?>> add, Function<T, String> name,
                                    List<String> notSaved) {
        for (T item : items) {
            Result<?> added = add.apply(item);
            if (!added.successful()) {
                notSaved.add(name.apply(item) + ": " + Fetch.reason(added));
            }
        }
    }

    /** e.g. "Guarantor Jane Doe". */
    private static String name(LoanPerson person) {
        return (person.role() == Coborrower.Type.GUARANTOR ? "Guarantor " : "Co-borrower ")
                + person.customer().fullName();
    }

    /** The loan as just created, New with the details entered, for when it can't be read back. */
    private static Loan asCreated(LoanNumber number, CustomerNumber customer, Loan.Details details) {
        return new Loan(number, customer, Loan.Status.NEW, details.principal(), details.rateType(),
                details.rateAmount(), details.rateFrequency(), details.startDate(), details.paymentAmount(),
                details.paymentFrequency(), details.renewalDate());
    }
}
