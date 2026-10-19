package lavs.ui.loan.view;

import java.util.Locale;

import lavs.logic.model.loan.Loan;

/**
 * When the loan screen lets a loan be changed: its details and who is on it can change only while
 * it's New. Once submitted they're locked; documents and notes stay open.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class LoanLock {

    private LoanLock() {
    }

    static boolean isEditable(Loan loan) {
        return loan.isEditable();
    }

    /** Why part of a locked loan can't change, e.g. what = "its details". */
    static String lockedNote(Loan loan, String what) {
        return "This loan is " + loan.status().code().toLowerCase(Locale.ROOT) + ", so " + what
                + " can no longer be changed.";
    }
}
