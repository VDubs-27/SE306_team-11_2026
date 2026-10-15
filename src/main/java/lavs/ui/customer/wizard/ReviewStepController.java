package lavs.ui.customer.wizard;

import static lavs.logic.model.Validation.isBlank;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Employer;
import lavs.logic.model.customer.Phone;
import lavs.ui.common.AddressRows;
import lavs.ui.common.DisplayFormat;

/**
 * Step 5 of the new customer wizard: a checklist of what has been entered and a summary of the
 * customer, shown just before saving. It is rebuilt from the draft every time it shows.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class ReviewStepController {

    @FXML private VBox checklist;
    @FXML private Label nameLabel;
    @FXML private VBox summary;

    /** Fills the checklist and summary from what has been entered so far. */
    void show(NewCustomerDraft draft) {
        Customer.Details details = draft.details();
        nameLabel.setText(Stream.of(details.title(), details.firstName(), details.familyName())
                .filter(part -> !isBlank(part))
                .collect(Collectors.joining(" ")));

        checklist.getChildren().setAll(
                checkItem(true, "Customer details", detailsGiven(details)),
                checkItem(true, "Mailing address", mailingAddress(draft)),
                checkItem(true, "Phone or email", contactSummary(draft)),
                employmentItem(draft));

        long mailing = draft.addresses().stream().filter(Address.Details::isMailing).count();
        summary.getChildren().setAll(
                summaryRow("Date of birth", DisplayFormat.date(details.dateOfBirth())),
                summaryRow("Nationality", DisplayFormat.orNotRecorded(details.nationality())),
                summaryRow("Addresses", draft.addresses().size() + " (" + mailing + " mailing)"),
                summaryRow("Contacts", count(draft.phones().size(), "phone") + ", "
                        + count(draft.emails().size(), "email")),
                summaryRow("Employers", draft.isEmployed()
                        ? String.valueOf(draft.employers().size())
                        : "Not employed"));
    }

    /** e.g. "Name, date of birth and nationality", naming only what was filled in. */
    private static String detailsGiven(Customer.Details details) {
        List<String> given = Stream.of(
                        "Name",
                        details.dateOfBirth() == null ? null : "date of birth",
                        isBlank(details.nationality()) ? null : "nationality")
                .filter(part -> part != null)
                .toList();
        if (given.size() == 1) {
            return "Name";
        }
        return String.join(", ", given.subList(0, given.size() - 1)) + " and " + given.get(given.size() - 1);
    }

    /** e.g. "27 Kowhai Street, Papamoa". */
    private static String mailingAddress(NewCustomerDraft draft) {
        return draft.addresses().stream()
                .filter(Address.Details::isMailing)
                .findFirst()
                .map(address -> Stream.of(AddressRows.street(address), address.suburb(), address.city())
                        .filter(part -> !isBlank(part))
                        .limit(2)
                        .collect(Collectors.joining(", ")))
                .orElse("None");
    }

    /** e.g. "Mobile and email, both primary". */
    private static String contactSummary(NewCustomerDraft draft) {
        List<String> parts = Stream.concat(
                        draft.phones().stream().filter(Phone.Details::isPrimary).map(phone -> phone.type().code()),
                        draft.emails().stream().filter(Email.Details::isPrimary).map(email -> "email"))
                .toList();
        if (parts.isEmpty()) {
            return count(draft.phones().size(), "phone") + ", " + count(draft.emails().size(), "email");
        }
        String joined = String.join(" and ", parts);
        joined = Character.toUpperCase(joined.charAt(0)) + joined.substring(1);
        return parts.size() == 1 ? joined + ", primary" : joined + ", both primary";
    }

    /** The employment line: "Teacher at Papamoa Primary School", or a muted "Not employed". */
    private static HBox employmentItem(NewCustomerDraft draft) {
        if (!draft.isEmployed()) {
            return checkItem(false, "Employment", "Not employed · any loan will need a guarantor");
        }
        Employer.Details employer = draft.employers().stream()
                .filter(candidate -> candidate.type() == Employer.Type.PRIMARY)
                .findFirst()
                .orElse(draft.employers().get(0));
        String role = isBlank(employer.position()) ? employer.name() : employer.position() + " at " + employer.name();
        return checkItem(true, "Employment", role);
    }

    /** A ticked (or skipped) item with a heading and a muted line under it. */
    private static HBox checkItem(boolean done, String heading, String detail) {
        Label tick = new Label(done ? "✓" : "–");
        tick.getStyleClass().addAll("step-circle", done ? "step-done" : "review-skipped");
        Label headingLabel = new Label(heading);
        headingLabel.getStyleClass().add("review-heading");
        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().addAll("text-muted", "text-small");
        detailLabel.setWrapText(true);
        HBox item = new HBox(12, tick, new VBox(2, headingLabel, detailLabel));
        item.setAlignment(Pos.TOP_LEFT);
        return item;
    }

    /** "Date of birth ........ 09/01/1978", with a line under it. */
    private static HBox summaryRow(String name, String value) {
        Label nameText = new Label(name);
        nameText.getStyleClass().add("text-muted");
        Label valueText = new Label(value);
        valueText.getStyleClass().add("review-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(nameText, spacer, valueText);
        row.getStyleClass().add("review-row");
        return row;
    }

    /** e.g. "1 phone", "2 emails". */
    private static String count(int howMany, String thing) {
        return howMany + " " + thing + (howMany == 1 ? "" : "s");
    }
}
