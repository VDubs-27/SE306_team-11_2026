package lavs.ui.loan.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.DocumentFile;
import lavs.ui.common.LoanText;
import lavs.ui.common.ReviewRows;
import lavs.ui.loan.people.LoanPerson;

/**
 * Step 4 of the new loan wizard: a checklist of what has been entered and a summary of the loan,
 * shown just before submitting. It is rebuilt from the draft every time it shows.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class SubmitStepController {

    @FXML private VBox checklist;
    @FXML private Label titleLabel;
    @FXML private VBox summary;

    /** Fills the checklist and summary from what has been entered so far. */
    void show(NewLoanDraft draft) {
        Loan.Details details = draft.details();
        LoanPerson applicant = draft.applicant();
        titleLabel.setText("Loan for " + applicant.customer().fullName());

        List<Node> items = new ArrayList<>();
        items.add(ReviewRows.checkItem(true, "Loan details",
                DisplayFormat.money(details.principal()) + " · " + LoanText.interest(details)));
        items.add(ReviewRows.checkItem(true, "Repayments",
                LoanText.repayment(details) + " from " + DisplayFormat.date(details.startDate())));
        items.add(documentsItem(draft.documents()));
        items.add(ReviewRows.checkItem(true, "People on the loan", peopleSummary(draft)));
        if (!applicant.employed()) {
            items.add(ReviewRows.checkItem(true, "Guarantor",
                    "Needed because " + applicant.customer().firstName() + " isn't employed: "
                            + namesWith(draft, Coborrower.Type.GUARANTOR)));
        }
        checklist.getChildren().setAll(items);

        summary.getChildren().setAll(
                ReviewRows.summaryRow("Principal", DisplayFormat.money(details.principal())),
                ReviewRows.summaryRow("Interest", LoanText.interest(details)),
                ReviewRows.summaryRow("Repayments", LoanText.repayment(details)),
                ReviewRows.summaryRow("Start date", DisplayFormat.date(details.startDate())),
                ReviewRows.summaryRow("Renewal date", DisplayFormat.date(details.renewalDate())),
                ReviewRows.summaryRow("Documents", String.valueOf(draft.documents().size())),
                ReviewRows.summaryRow("Co-borrowers", String.valueOf(countWith(draft, Coborrower.Type.COBORROWER))),
                ReviewRows.summaryRow("Guarantors", String.valueOf(countWith(draft, Coborrower.Type.GUARANTOR))));
    }

    /** Ticked with what was added, or a grey dash when there are none (they're optional). */
    private static Node documentsItem(List<DocumentFile> documents) {
        if (documents.isEmpty()) {
            return ReviewRows.checkItem(false, "Documents", "None added · they can be added to the loan later");
        }
        String types = documents.stream().map(DocumentFile::type).distinct().collect(Collectors.joining(", "));
        return ReviewRows.checkItem(true, "Documents", ReviewRows.count(documents.size(), "document") + ": " + types);
    }

    /** e.g. "John Smith, with 1 co-borrower and 1 guarantor", or "John Smith only". */
    private static String peopleSummary(NewLoanDraft draft) {
        String applicant = draft.applicant().customer().fullName();
        long coborrowers = countWith(draft, Coborrower.Type.COBORROWER);
        long guarantors = countWith(draft, Coborrower.Type.GUARANTOR);
        if (coborrowers + guarantors == 0) {
            return applicant + " only";
        }
        List<String> others = new ArrayList<>();
        if (coborrowers > 0) {
            others.add(ReviewRows.count((int) coborrowers, "co-borrower"));
        }
        if (guarantors > 0) {
            others.add(ReviewRows.count((int) guarantors, "guarantor"));
        }
        return applicant + ", with " + String.join(" and ", others);
    }

    private static long countWith(NewLoanDraft draft, Coborrower.Type role) {
        return draft.others().stream().filter(person -> person.role() == role).count();
    }

    private static String namesWith(NewLoanDraft draft, Coborrower.Type role) {
        return draft.others().stream()
                .filter(person -> person.role() == role)
                .map(person -> person.customer().fullName())
                .collect(Collectors.joining(", "));
    }
}
