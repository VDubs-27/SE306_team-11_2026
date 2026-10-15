package lavs.ui.customer.profile;

import static lavs.ui.customer.profile.ProfileFormat.date;
import static lavs.ui.customer.profile.ProfileFormat.orNotRecorded;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import lavs.logic.model.customer.Customer;

/**
 * The Details tab: the customer's personal details, read-only.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DetailsTabController {

    @FXML private Label titleValue;
    @FXML private Label firstNameValue;
    @FXML private Label familyNameValue;
    @FXML private Label dateOfBirthValue;
    @FXML private Label nationalityValue;
    @FXML private Label statusBadge;

    void show(Customer customer) {
        titleValue.setText(orNotRecorded(customer.title()));
        firstNameValue.setText(customer.firstName());
        familyNameValue.setText(customer.familyName());
        dateOfBirthValue.setText(date(customer.dateOfBirth()));
        nationalityValue.setText(orNotRecorded(customer.nationality()));
        statusBadge.setText(orNotRecorded(customer.status().code()));
        statusBadge.getStyleClass().add(
                customer.status() == Customer.Status.ACTIVE ? "badge-active" : "badge-neutral");
    }

    @FXML
    private void handleEditDetails() {
        // Later: open the edit details form
    }

    @FXML
    private void handleChangeStatus() {
        // Later: confirm and switch the customer between Active and Inactive
    }
}
