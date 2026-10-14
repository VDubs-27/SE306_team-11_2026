package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Phone;

/** The Contacts tab: the customer's phone numbers and email addresses. */
public class ContactsTabController {

    @FXML private VBox phoneList;
    @FXML private VBox emailList;

    void show(List<Phone> phones, List<Email> emails) {
        RecordTiles.fill(phoneList, phones.stream()
                .map(phone -> RecordTiles.captionTile(typeName(phone), number(phone),
                        badges(phone), () -> handleEditPhone(phone)))
                .toList(), "No phone numbers recorded.");
        RecordTiles.fill(emailList, emails.stream()
                .map(email -> RecordTiles.captionTile(typeName(email), email.address(),
                        email.isPrimary() ? List.of(RecordTiles.primaryBadge()) : List.of(),
                        () -> handleEditEmail(email)))
                .toList(), "No email addresses recorded.");
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

    private static String typeName(Phone phone) {
        return phone.type() == Phone.Type.OTHER && !isBlank(phone.otherType())
                ? phone.otherType()
                : phone.type().code();
    }

    private static String typeName(Email email) {
        return email.type() == Email.Type.OTHER && !isBlank(email.otherType())
                ? email.otherType()
                : email.type().code();
    }

    /** e.g. "+64 21 555 0182". */
    private static String number(Phone phone) {
        return isBlank(phone.countryCode())
                ? phone.number()
                : "+" + phone.countryCode() + " " + phone.number();
    }

    private static List<Label> badges(Phone phone) {
        List<Label> badges = new ArrayList<>();
        if (phone.isPrimary()) {
            badges.add(RecordTiles.primaryBadge());
        }
        if (phone.canTxt()) {
            badges.add(RecordTiles.badge("Can text", "badge-mailing"));
        }
        return badges;
    }
}
