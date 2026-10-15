package lavs.ui.loan.people;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Customer;
import lavs.logic.model.loan.Coborrower;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.RecordTiles;

/**
 * Rows showing a person on a loan: an initials avatar, their name and customer number, a line of
 * detail, their role and (except for the applicant) a Remove button.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class PersonRows {

    /** Avatar colours for people other than the applicant (avatar-tone-1 and -2 in app.css). */
    private static final int OTHER_TONES = 2;

    private PersonRows() {
    }

    /** A person already on the loan. onRemove is null for the applicant, who can't be removed. */
    public static HBox tile(LoanPerson person, Runnable onRemove) {
        String detail = Stream.of(
                        bornOn(person.customer()),
                        person.suburb(),
                        person.employed() ? "Employed" : "Not employed")
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" · "));
        HBox row = row(person.customer(), detail, person.isApplicant());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(spacer, roleBadge(person.role()));
        if (onRemove != null) {
            Button remove = new Button("Remove");
            remove.getStyleClass().add("button-secondary");
            remove.setOnAction(event -> onRemove.run());
            row.getChildren().add(remove);
        }
        return row;
    }

    /** A search result: avatar, name and number, and date of birth. */
    public static HBox result(Customer customer) {
        HBox row = row(customer, bornOn(customer), false);
        row.getStyleClass().add("person-result");
        return row;
    }

    /** The applicant's avatar is always brand blue; anyone else gets one of the other colours. */
    private static HBox row(Customer customer, String detail, boolean applicant) {
        int tone = applicant ? 0 : 1 + Math.floorMod(customer.number().hashCode(), OTHER_TONES);
        Label avatar = new Label(DisplayFormat.initials(customer));
        avatar.getStyleClass().addAll("avatar", "avatar-tone-" + tone);

        Label name = new Label(customer.fullName());
        name.getStyleClass().add("tile-line1");
        Label number = new Label("·  " + customer.number().value());
        number.getStyleClass().addAll("mono", "person-number");
        HBox nameLine = new HBox(6, name, number);
        nameLine.setAlignment(Pos.BASELINE_LEFT);

        Label detailLine = new Label(detail);
        detailLine.getStyleClass().add("tile-line2");

        HBox row = new HBox(14, avatar, new VBox(2, nameLine, detailLine));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("record-tile");
        return row;
    }

    private static Label roleBadge(Coborrower.Type role) {
        if (role == null) {
            return RecordTiles.badge("Primary applicant", "badge-primary");
        }
        return role == Coborrower.Type.GUARANTOR
                ? RecordTiles.badge("Guarantor", "badge-outline")
                : RecordTiles.badge("Co-borrower", "badge-mailing");
    }

    private static String bornOn(Customer customer) {
        return customer.dateOfBirth() == null ? null : "Born " + DisplayFormat.date(customer.dateOfBirth());
    }
}
