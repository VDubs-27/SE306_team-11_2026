package lavs.ui.loan.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.document.Document;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import lavs.logic.service.Result;
import lavs.ui.common.DocumentFile;
import lavs.ui.common.Fetch;
import lavs.ui.loan.people.LoanPerson;
import lavs.ui.shell.LoanServices;

/**
 * Sends what the new loan wizard collected: creates the loan, adds its co-borrowers, guarantors and
 * documents one by one, then submits it for approval. Once the loan exists it is never created again,
 * so if something after that fails, the rest still goes ahead and what didn't is listed; a loan that
 * can't be submitted yet stays New, to submit from the loan screen. Run it in the background.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class LoanSaver {

    private LoanSaver() {
    }

    /**
     * A loan just created: as it is now, what couldn't be added to it (e.g. "Guarantor Jane Doe: …"),
     * and why it couldn't be submitted, or null when it was.
     */
    record Saved(Loan loan, List<String> notSaved, String notSubmitted) {
    }

    /** The loan as created, or why it couldn't be; nothing is sent when creating fails. */
    static Result<Saved> save(LoanServices services, NewLoanDraft draft) {
        CustomerNumber customer = draft.applicant().customer().number();
        Result<LoanNumber> created = services.loans().create(customer, draft.details());
        if (!created.successful()) {
            return new Result<>(null, created.fieldErrors(), created.errorMessage());
        }
        LoanNumber number = created.value();
        Loan loan = asCreated(number, customer, draft.details());
        List<String> notSaved = new ArrayList<>();
        addEach(draft.others(), person -> services.coborrowers().add(loan,
                new Coborrower.Details(person.role(), person.customer().number())), LoanSaver::name, notSaved);
        addEach(draft.documents(), document -> services.documents().add(number, details(document)),
                document -> "Document " + document.fileName(), notSaved);

        Result<Loan> submitted = services.loans().submit(number);
        if (submitted.successful()) {
            return Result.success(new Saved(submitted.value(), notSaved, null));
        }
        Result<Loan> read = services.loans().retrieve(number);
        return Result.success(new Saved(read.successful() ? read.value() : loan, notSaved, Fetch.reason(submitted)));
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

    /** The loan as just created, New with the details entered, for adding people to before it's read back. */
    private static Loan asCreated(LoanNumber number, CustomerNumber customer, Loan.Details details) {
        return new Loan(number, customer, Loan.Status.NEW, details.principal(), details.rateType(),
                details.rateAmount(), details.rateFrequency(), details.startDate(), details.paymentAmount(),
                details.paymentFrequency(), details.renewalDate());
    }

    private static Document.Details details(DocumentFile document) {
        return new Document.Details(document.type(), document.fileName(), document.fileType(), document.size());
    }
}
