package lavs.ui.common;

import static lavs.logic.model.Validation.isBlank;

import java.util.List;

import javafx.scene.layout.HBox;

import lavs.logic.model.customer.Employer;

/**
 * How an employer is shown as a row, wherever employers are listed.
 * AI-assisted: written by Jack, refined with Claude.
 */
public final class EmployerRows {

    private EmployerRows() {
    }

    /** "Primary" on the left, the employer's name, and the job and salary under it. */
    public static HBox tile(Employer.Details employer, Runnable onEdit) {
        return RecordTiles.tile(employer.type().code(), DisplayFormat.orNotRecorded(employer.name()),
                role(employer), List.of(), onEdit);
    }

    /** e.g. "Project Manager · $98,000 a year", leaving out whichever part is missing. */
    public static String role(Employer.Details employer) {
        String salary = employer.salary() == null ? null : DisplayFormat.money(employer.salary()) + " a year";
        if (isBlank(employer.position())) {
            return salary;
        }
        return salary == null ? employer.position() : employer.position() + " · " + salary;
    }
}
