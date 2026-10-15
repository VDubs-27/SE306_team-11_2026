package lavs.ui.customer.profile;

import static lavs.ui.common.DisplayFormat.date;
import static lavs.ui.common.DisplayFormat.orNotRecorded;

import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

import lavs.logic.model.customer.Customer;
import lavs.ui.common.Dialogs;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.RecordEditor;
import lavs.ui.customer.forms.CustomerDetailsFormController;

/**
 * The Details tab: the customer's personal details, read-only until Edit, which opens the same
 * form as the new customer wizard in a pop-up. The customer can also be made active or inactive.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DetailsTabController {

    @FXML private Label titleValue;
    @FXML private Label firstNameValue;
    @FXML private Label familyNameValue;
    @FXML private Label dateOfBirthValue;
    @FXML private Label nationalityValue;
    @FXML private Label statusBadge;
    @FXML private Hyperlink statusLink;
    @FXML private Node detailsForm;
    @FXML private CustomerDetailsFormController detailsFormController;

    private Customer customer;
    private RecordEditor<Customer.Details> editor;
    private Consumer<Customer.Details> onSaved = details -> { };
    private Consumer<Customer.Status> onStatusChanged = status -> { };

    @FXML
    private void initialize() {
        editor = new RecordEditor<>(titleValue, "customer details", detailsForm, detailsFormController)
                .withoutRemove();
    }

    /** What happens after Edit is saved: the profile updates itself. */
    void setOnSaved(Consumer<Customer.Details> onSaved) {
        this.onSaved = onSaved;
    }

    /** What happens once a status change is confirmed: the profile updates itself. */
    void setOnStatusChanged(Consumer<Customer.Status> onStatusChanged) {
        this.onStatusChanged = onStatusChanged;
    }

    void show(Customer customer) {
        this.customer = customer;
        titleValue.setText(orNotRecorded(customer.title()));
        firstNameValue.setText(customer.firstName());
        familyNameValue.setText(customer.familyName());
        dateOfBirthValue.setText(date(customer.dateOfBirth()));
        nationalityValue.setText(orNotRecorded(customer.nationality()));
        statusBadge.setText(orNotRecorded(customer.status().code()));
        statusBadge.getStyleClass().removeAll("badge-active", "badge-neutral");
        statusBadge.getStyleClass().add(
                customer.status() == Customer.Status.ACTIVE ? "badge-active" : "badge-neutral");
        statusLink.setText(isActive() ? "Make inactive" : "Make active");
    }

    @FXML
    private void handleEditDetails() {
        editor.edit(customer.details(), changed -> onSaved.accept(changed), () -> { });
    }

    @FXML
    private void handleChangeStatus() {
        String name = DisplayFormat.fullNameWithTitle(customer);
        boolean confirmed = isActive()
                ? Dialogs.confirm(statusLink, "Make " + name + " inactive?",
                        "Their details, loans and history stay on record. You can make them active again at any time.",
                        "Make inactive")
                : Dialogs.confirm(statusLink, "Make " + name + " active again?",
                        "They'll show as an active customer again.", "Make active");
        if (confirmed) {
            onStatusChanged.accept(isActive() ? Customer.Status.INACTIVE : Customer.Status.ACTIVE);
        }
    }

    private boolean isActive() {
        return customer.status() == Customer.Status.ACTIVE;
    }
}
