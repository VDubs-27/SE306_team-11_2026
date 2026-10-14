package lavs.ui.customer.profile;

import static lavs.ui.customer.profile.ProfileFormat.date;
import static lavs.ui.customer.profile.ProfileFormat.orNotRecorded;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import lavs.logic.model.customer.Customer;

/** The Details tab: the customer's personal details, read-only. */
public class DetailsTabController {

    @FXML private Label titleValue;
    @FXML private Label customerNumberValue;
    @FXML private Label firstNameValue;
    @FXML private Label familyNameValue;
    @FXML private Label dateOfBirthValue;
    @FXML private Label nationalityValue;
    @FXML private Label statusValue;

    void show(Customer customer) {
        titleValue.setText(orNotRecorded(customer.title()));
        customerNumberValue.setText(customer.number().value());
        firstNameValue.setText(customer.firstName());
        familyNameValue.setText(customer.familyName());
        dateOfBirthValue.setText(date(customer.dateOfBirth()));
        nationalityValue.setText(orNotRecorded(customer.nationality()));
        statusValue.setText(orNotRecorded(customer.status().code()));
    }

    @FXML
    private void handleEditDetails() {
        // Later: open the edit details form
    }
}
