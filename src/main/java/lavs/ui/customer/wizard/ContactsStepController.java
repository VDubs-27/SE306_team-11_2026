package lavs.ui.customer.wizard;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Phone;
import lavs.ui.common.ContactRows;
import lavs.ui.common.FormDialog;
import lavs.ui.common.RecordTiles;
import lavs.ui.customer.forms.EmailFormController;
import lavs.ui.customer.forms.PhoneFormController;

/**
 * Step 3 of the new customer wizard: phone numbers and email addresses, added and edited in a
 * pop-up. The customer needs at least one of either before moving on.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class ContactsStepController {

    @FXML private Node phoneForm;
    @FXML private PhoneFormController phoneFormController;
    @FXML private Node emailForm;
    @FXML private EmailFormController emailFormController;
    @FXML private VBox contactList;
    @FXML private Label contactChip;
    @FXML private HBox errorBanner;

    private NewCustomerDraft draft;

    @FXML
    private void initialize() {
        show(errorBanner, false);
    }

    /** Gives the step the draft whose phones and emails it edits. */
    void use(NewCustomerDraft draft) {
        this.draft = draft;
        refresh();
    }

    /** True when there is a phone or an email; otherwise shows why the step can't move on. */
    boolean check() {
        boolean ok = hasContact();
        show(errorBanner, !ok);
        return ok;
    }

    @FXML
    private void handleAddPhone() {
        List<Phone.Details> phones = draft.phones();
        phoneFormController.clear(phones.isEmpty());
        if (openPhoneForm("Add phone", false) == FormDialog.Result.SAVED) {
            phones.add(phoneFormController.read());
            keepOnePrimaryPhone(phones.size() - 1);
        }
        refresh();
    }

    @FXML
    private void handleAddEmail() {
        List<Email.Details> emails = draft.emails();
        emailFormController.clear(emails.isEmpty());
        if (openEmailForm("Add email", false) == FormDialog.Result.SAVED) {
            emails.add(emailFormController.read());
            keepOnePrimaryEmail(emails.size() - 1);
        }
        refresh();
    }

    private void handleEditPhone(int index) {
        List<Phone.Details> phones = draft.phones();
        phoneFormController.fill(phones.get(index));
        FormDialog.Result result = openPhoneForm("Edit phone", true);
        if (result == FormDialog.Result.SAVED) {
            phones.set(index, phoneFormController.read());
            keepOnePrimaryPhone(index);
        } else if (result == FormDialog.Result.REMOVED) {
            phones.remove(index);
        }
        refresh();
    }

    private void handleEditEmail(int index) {
        List<Email.Details> emails = draft.emails();
        emailFormController.fill(emails.get(index));
        FormDialog.Result result = openEmailForm("Edit email", true);
        if (result == FormDialog.Result.SAVED) {
            emails.set(index, emailFormController.read());
            keepOnePrimaryEmail(index);
        } else if (result == FormDialog.Result.REMOVED) {
            emails.remove(index);
        }
        refresh();
    }

    private FormDialog.Result openPhoneForm(String title, boolean canRemove) {
        return FormDialog.show(contactList, title, phoneForm, phoneFormController::check, canRemove);
    }

    private FormDialog.Result openEmailForm(String title, boolean canRemove) {
        return FormDialog.show(contactList, title, emailForm, emailFormController::check, canRemove);
    }

    /** One primary phone: when the phone just saved is primary, the others stop being it. */
    private void keepOnePrimaryPhone(int savedIndex) {
        List<Phone.Details> phones = draft.phones();
        if (!phones.get(savedIndex).isPrimary()) {
            return;
        }
        for (int i = 0; i < phones.size(); i++) {
            Phone.Details phone = phones.get(i);
            if (i != savedIndex && phone.isPrimary()) {
                phones.set(i, new Phone.Details(phone.type(), phone.otherType(), phone.countryCode(),
                        phone.number(), false, phone.canTxt()));
            }
        }
    }

    /** One primary email: when the email just saved is primary, the others stop being it. */
    private void keepOnePrimaryEmail(int savedIndex) {
        List<Email.Details> emails = draft.emails();
        if (!emails.get(savedIndex).isPrimary()) {
            return;
        }
        for (int i = 0; i < emails.size(); i++) {
            Email.Details email = emails.get(i);
            if (i != savedIndex && email.isPrimary()) {
                emails.set(i, new Email.Details(email.type(), email.otherType(), email.address(), false));
            }
        }
    }

    /** Phones first, then emails, as in the mockup. */
    private void refresh() {
        List<HBox> rows = new ArrayList<>();
        List<Phone.Details> phones = draft.phones();
        for (int i = 0; i < phones.size(); i++) {
            int index = i;
            rows.add(ContactRows.phoneTile(phones.get(i), () -> handleEditPhone(index)));
        }
        List<Email.Details> emails = draft.emails();
        for (int i = 0; i < emails.size(); i++) {
            int index = i;
            rows.add(ContactRows.emailTile(emails.get(i), () -> handleEditEmail(index)));
        }
        RecordTiles.fill(contactList, rows, "No phone numbers or email addresses added yet.");
        show(contactChip, hasContact());
        if (hasContact()) {
            show(errorBanner, false);
        }
    }

    private boolean hasContact() {
        return !draft.phones().isEmpty() || !draft.emails().isEmpty();
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
