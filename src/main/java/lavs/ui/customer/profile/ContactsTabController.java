package lavs.ui.customer.profile;

import java.util.function.LongConsumer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Phone;
import lavs.ui.common.ContactRows;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordEditor;
import lavs.ui.common.ServiceList;
import lavs.ui.customer.forms.EmailFormController;
import lavs.ui.customer.forms.PhoneFormController;

/**
 * The Contacts tab: the customer's phone numbers and email addresses, each a page at a time from the
 * mainframe. Add and Edit use the same pop-ups as the new customer wizard.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class ContactsTabController {

    private static final long PAGE_SIZE = 5;

    @FXML private VBox phoneList;
    @FXML private VBox emailList;
    @FXML private PagerController phonePagerController;
    @FXML private PagerController emailPagerController;
    @FXML private Node phoneForm;
    @FXML private PhoneFormController phoneFormController;
    @FXML private Node emailForm;
    @FXML private EmailFormController emailFormController;

    private ServiceList<Phone, Phone.Details> phones;
    private ServiceList<Email, Email.Details> emails;
    /** How many of each there are, or -1 until its first page is read. */
    private long phoneCount = -1;
    private long emailCount = -1;

    @FXML
    private void initialize() {
        phones = new ServiceList<>(new RecordEditor<>(phoneList, "phone", phoneForm, phoneFormController),
                phoneList, phonePagerController, PAGE_SIZE,
                (phone, onEdit) -> ContactRows.phoneCaptionTile(phone.details(), onEdit), Phone::details,
                "No phone numbers recorded.");
        emails = new ServiceList<>(new RecordEditor<>(emailList, "email", emailForm, emailFormController),
                emailList, emailPagerController, PAGE_SIZE,
                (email, onEdit) -> ContactRows.emailCaptionTile(email.details(), onEdit), Email::details,
                "No email addresses recorded.");
    }

    /**
     * Shows the customer's phone numbers and email addresses from these sources; onTotal hears how many
     * contacts there are altogether, for the tab's count.
     */
    void show(ServiceList.Source<Phone, Phone.Details> phoneSource, ServiceList.Source<Email, Email.Details> emailSource,
              LongConsumer onTotal) {
        phones.onTotal(count -> {
            phoneCount = count;
            tellTotal(onTotal);
        }).show(phoneSource);
        emails.onTotal(count -> {
            emailCount = count;
            tellTotal(onTotal);
        }).show(emailSource);
    }

    /** Passes on the number of contacts once both lists have been read, so the count never shows half. */
    private void tellTotal(LongConsumer onTotal) {
        if (phoneCount >= 0 && emailCount >= 0) {
            onTotal.accept(phoneCount + emailCount);
        }
    }

    @FXML
    private void handleAddPhone() {
        phones.add();
    }

    @FXML
    private void handleAddEmail() {
        emails.add();
    }
}
