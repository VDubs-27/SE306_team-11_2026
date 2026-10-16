package lavs.ui.common;

import static lavs.logic.model.Validation.isBlank;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Phone;

/**
 * How phone numbers and email addresses are shown as rows, wherever they are listed.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class ContactRows {

    private ContactRows() {
    }

    /** A full-width row: "Mobile" on the left, the number, and "Can receive texts" under it. */
    public static HBox phoneTile(Phone.Details phone, Runnable onEdit) {
        return RecordTiles.tile(typeName(phone), number(phone), phone.canTxt() ? "Can receive texts" : null,
                phone.isPrimary() ? List.of(RecordTiles.primaryBadge()) : List.of(), onEdit);
    }

    /** A full-width row: "Email" on the left, the address, and its type (e.g. "Personal") under it. */
    public static HBox emailTile(Email.Details email, Runnable onEdit) {
        return RecordTiles.tile("Email", email.address(), typeName(email),
                email.isPrimary() ? List.of(RecordTiles.primaryBadge()) : List.of(), onEdit);
    }

    /** A compact row for a narrow column: the type as a small caption above the number. */
    public static HBox phoneCaptionTile(Phone.Details phone, Runnable onEdit) {
        List<Label> badges = new ArrayList<>();
        if (phone.isPrimary()) {
            badges.add(RecordTiles.primaryBadge());
        }
        if (phone.canTxt()) {
            badges.add(RecordTiles.badge("Can text", "badge-mailing"));
        }
        return RecordTiles.captionTile(typeName(phone), number(phone), badges, onEdit);
    }

    /** A compact row for a narrow column: the type as a small caption above the address. */
    public static HBox emailCaptionTile(Email.Details email, Runnable onEdit) {
        return RecordTiles.captionTile(typeName(email), email.address(),
                email.isPrimary() ? List.of(RecordTiles.primaryBadge()) : List.of(), onEdit);
    }

    /** e.g. "+64 21 555 0182", whether the country code was saved as "64" or "+64". */
    public static String number(Phone.Details phone) {
        return isBlank(phone.countryCode())
                ? phone.number()
                : "+" + phone.countryCode().trim().replaceFirst("^\\+", "") + " " + phone.number();
    }

    private static String typeName(Phone.Details phone) {
        return phone.type() == Phone.Type.OTHER && !isBlank(phone.otherType())
                ? phone.otherType()
                : phone.type().code();
    }

    private static String typeName(Email.Details email) {
        return email.type() == Email.Type.OTHER && !isBlank(email.otherType())
                ? email.otherType()
                : email.type().code();
    }
}
