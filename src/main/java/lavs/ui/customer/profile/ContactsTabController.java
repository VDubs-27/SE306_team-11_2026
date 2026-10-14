package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Phone;
import lavs.ui.common.ContactRows;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

/**
 * The Contacts tab: the customer's phone numbers and email addresses.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class ContactsTabController {

    @FXML private VBox phoneList;
    @FXML private VBox emailList;
    @FXML private PagerController phonePagerController;
    @FXML private PagerController emailPagerController;

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
        // Later: open the add phone form
    }

    @FXML
    private void handleAddEmail() {
        // Later: open the add email form
    }

    private void handleEditPhone(Phone phone) {
        // Later: open the edit phone form for this number
    }

    private void handleEditEmail(Email email) {
        // Later: open the edit email form for this address
    }
}
