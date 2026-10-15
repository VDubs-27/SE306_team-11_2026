package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Phone;
import lavs.ui.common.ContactRows;
import lavs.ui.common.FormDialog;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;
import lavs.ui.customer.forms.EmailFormController;
import lavs.ui.customer.forms.PhoneFormController;

/**
 * The Contacts tab: the customer's phone numbers and email addresses. Add and Edit use the same
 * pop-ups as the new customer wizard.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class ContactsTabController {

    @FXML private VBox phoneList;
    @FXML private VBox emailList;
    @FXML private PagerController phonePagerController;
    @FXML private PagerController emailPagerController;
    @FXML private Node phoneForm;
    @FXML private PhoneFormController phoneFormController;
    @FXML private Node emailForm;
    @FXML private EmailFormController emailFormController;

    /** Shows the first page of each list and returns how many contacts there are altogether, for the tab's count. */
    long show(LongFunction<Page<Phone>> loadPhones, LongFunction<Page<Email>> loadEmails) {
        long phones = phonePagerController.show(loadPhones, items -> RecordTiles.fill(phoneList, items.stream()
                .map(phone -> ContactRows.phoneCaptionTile(phone.details(), () -> handleEditPhone(phone)))
                .toList(), "No phone numbers recorded."));
        long emails = emailPagerController.show(loadEmails, items -> RecordTiles.fill(emailList, items.stream()
                .map(email -> ContactRows.emailCaptionTile(email.details(), () -> handleEditEmail(email)))
                .toList(), "No email addresses recorded."));
        return phones + emails;
    }

    @FXML
    private void handleAddPhone() {
        phoneFormController.clear(false);
        if (FormDialog.show(phoneList, "Add phone", phoneForm, phoneFormController::check, false)
                == FormDialog.Result.SAVED) {
            // Later: CustomerService.addPhone(customerNumber, phoneFormController.read()), then reload the tab
        }
    }

    @FXML
    private void handleAddEmail() {
        emailFormController.clear(false);
        if (FormDialog.show(emailList, "Add email", emailForm, emailFormController::check, false)
                == FormDialog.Result.SAVED) {
            // Later: CustomerService.addEmail(customerNumber, emailFormController.read()), then reload the tab
        }
    }

    private void handleEditPhone(Phone phone) {
        phoneFormController.fill(phone.details());
        switch (FormDialog.show(phoneList, "Edit phone", phoneForm, phoneFormController::check, true)) {
            case SAVED -> {
                // Later: CustomerService.updatePhone(phone.id(), phoneFormController.read()), then reload the tab
            }
            case REMOVED -> {
                // Later: CustomerService.removePhone(phone.id()), then reload the tab
            }
            default -> {
                // Cancelled: nothing to do
            }
        }
    }

    private void handleEditEmail(Email email) {
        emailFormController.fill(email.details());
        switch (FormDialog.show(emailList, "Edit email", emailForm, emailFormController::check, true)) {
            case SAVED -> {
                // Later: CustomerService.updateEmail(email.id(), emailFormController.read()), then reload the tab
            }
            case REMOVED -> {
                // Later: CustomerService.removeEmail(email.id()), then reload the tab
            }
            default -> {
                // Cancelled: nothing to do
            }
        }
    }
}
