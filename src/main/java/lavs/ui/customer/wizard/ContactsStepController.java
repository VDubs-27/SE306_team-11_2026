package lavs.ui.customer.wizard;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Phone;
import lavs.ui.common.ContactRows;
import lavs.ui.common.EditableList;
import lavs.ui.common.Nodes;
import lavs.ui.common.RecordEditor;
import lavs.ui.customer.forms.EmailFormController;
import lavs.ui.customer.forms.PhoneFormController;

/**
 * Step 3 of the new customer wizard: phone numbers and email addresses, added and edited in a
 * pop-up. The customer needs at least one of either before moving on.
 * AI-assisted: written by Jack, refined with Claude.
 */
public class ContactsStepController {

    @FXML private Node phoneForm;
    @FXML private PhoneFormController phoneFormController;
    @FXML private Node emailForm;
    @FXML private EmailFormController emailFormController;
    @FXML private VBox contactList;
    @FXML private VBox phoneList;
    @FXML private VBox emailList;
    @FXML private Label noContacts;
    @FXML private Label contactChip;
    @FXML private HBox errorBanner;

    private EditableList<Phone.Details> phones;
    private EditableList<Email.Details> emails;

    @FXML
    private void initialize() {
        Nodes.show(errorBanner, false);
        // Phones first, then emails, as in the mockup; one primary of each
        phones = new EditableList<>(new RecordEditor<>(contactList, "phone", phoneForm, phoneFormController),
                phoneList, ContactRows::phoneTile, null)
                .keepOnlyOne(Phone.Details::isPrimary, phone -> new Phone.Details(phone.type(), phone.otherType(),
                        phone.countryCode(), phone.number(), false, phone.canTxt()))
                .onChanged(this::refresh);
        emails = new EditableList<>(new RecordEditor<>(contactList, "email", emailForm, emailFormController),
                emailList, ContactRows::emailTile, null)
                .keepOnlyOne(Email.Details::isPrimary,
                        email -> new Email.Details(email.type(), email.otherType(), email.address(), false))
                .onChanged(this::refresh);
    }

    /** Gives the step the draft whose phones and emails it edits. */
    void use(NewCustomerDraft draft) {
        phones.use(draft.phones());
        emails.use(draft.emails());
        refresh();
    }

    /** True when there is a phone or an email; otherwise shows why the step can't move on. */
    boolean check() {
        boolean ok = hasContact();
        Nodes.show(errorBanner, !ok);
        return ok;
    }

    @FXML
    private void handleAddPhone() {
        phones.add();
    }

    @FXML
    private void handleAddEmail() {
        emails.add();
    }

    private void refresh() {
        Nodes.show(noContacts, !hasContact());
        Nodes.show(contactChip, hasContact());
        if (hasContact()) {
            Nodes.show(errorBanner, false);
        }
    }

    private boolean hasContact() {
        return !phones.items().isEmpty() || !emails.items().isEmpty();
    }
}
