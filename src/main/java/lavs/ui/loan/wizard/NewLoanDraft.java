package lavs.ui.loan.wizard;

import java.util.ArrayList;
import java.util.List;

/**
 * What has been entered in the new loan wizard so far. Each step reads and fills its part;
 * nothing reaches the mainframe until the loan is submitted.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class NewLoanDraft {

    private final LoanPerson applicant;
    private final List<LoanPerson> others = new ArrayList<>();

    NewLoanDraft(LoanPerson applicant) {
        this.applicant = applicant;
    }

    /** The customer the loan is for. */
    LoanPerson applicant() {
        return applicant;
    }

    /** Co-borrowers and guarantors, in the order they were added. The Co-borrowers step edits this list. */
    List<LoanPerson> others() {
        return others;
    }
}
