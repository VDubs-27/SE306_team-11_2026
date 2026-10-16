package lavs.ui.loan.view;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import lavs.logic.model.loan.Loan;
import lavs.ui.loan.people.LoanPeopleController;
import lavs.ui.loan.people.LoanPerson;

/**
 * The loan screen's Co-borrowers tab: the applicant, then everyone else on the loan. While the
 * loan is New, people can be found and added, or removed; after that the list is locked.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class CoborrowersTabController {

    @FXML private LoanPeopleController peopleController;
    @FXML private Label lockNote;

    /** What happens after someone is added or removed: the loan screen updates the tab's count. */
    void setOnChanged(Runnable onChanged) {
        // Later: save through LoanService (add and delete co-borrower); until then changes only show on this screen
        peopleController.setOnChanged(onChanged);
    }

    /** The people on the loan when the screen opens: the applicant and everyone else. */
    void use(LoanPerson applicant, List<LoanPerson> others) {
        peopleController.use(applicant, new ArrayList<>(others));
    }

    /** Shows everyone on the loan, with adding and removing only while it's New. Returns how many others there are. */
    long show(Loan loan) {
        boolean editable = LoanLock.isEditable(loan);
        peopleController.setEditable(editable);
        lockNote.setText(editable
                ? "Add each person as a co-borrower or a guarantor. An applicant with no job needs a guarantor."
                : LoanLock.lockedNote(loan, "who is on it"));
        return peopleController.count();
    }

    /** An applicant who isn't employed needs a guarantor before the loan is submitted; shows why if not. */
    boolean check() {
        return peopleController.check();
    }
}
