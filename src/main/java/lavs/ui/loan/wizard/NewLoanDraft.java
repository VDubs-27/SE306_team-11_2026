package lavs.ui.loan.wizard;

import java.util.ArrayList;
import java.util.List;

import lavs.logic.model.loan.Loan;
import lavs.ui.common.DocumentFile;

/**
 * What has been entered in the new loan wizard so far. Each step reads and fills its part;
 * nothing reaches the mainframe until the loan is submitted.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class NewLoanDraft {

    private final LoanPerson applicant;
    private final List<LoanPerson> others = new ArrayList<>();
    private Loan.Details details;
    private final List<DocumentFile> documents = new ArrayList<>();

    NewLoanDraft(LoanPerson applicant) {
        this.applicant = applicant;
    }

    /** The customer the loan is for. */
    LoanPerson applicant() {
        return applicant;
    }

    /** The amount, interest, repayments and dates, once the Loan details step has passed; null before. */
    Loan.Details details() {
        return details;
    }

    void setDetails(Loan.Details details) {
        this.details = details;
    }

    /** Supporting documents, added to the loan once it is submitted. The Upload step edits this list. */
    List<DocumentFile> documents() {
        return documents;
    }

    /** Co-borrowers and guarantors, in the order they were added. The Co-borrowers step edits this list. */
    List<LoanPerson> others() {
        return others;
    }
}
