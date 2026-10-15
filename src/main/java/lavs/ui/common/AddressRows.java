package lavs.ui.common;

import static lavs.logic.model.Validation.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import lavs.logic.model.customer.Address;

/**
 * How an address is shown as a row, wherever addresses are listed: the type, the street,
 * the suburb and city, and Primary / Mailing badges.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class AddressRows {

    private static final String HOME_COUNTRY = "New Zealand";

    private AddressRows() {
    }

    /** A row for one address, with an Edit button. */
    public static HBox tile(Address.Details address, Runnable onEdit) {
        return RecordTiles.tile(typeName(address), street(address), locality(address), badges(address), onEdit);
    }

    /** The address type, or the custom name for an "Other" address, e.g. "Holiday home". */
    public static String typeName(Address.Details address) {
        return address.type() == Address.Type.OTHER && !isBlank(address.otherType())
                ? address.otherType()
                : address.type().code();
    }

    /** Line 1, with line 2 after it when there is one. */
    public static String street(Address.Details address) {
        return joinNonBlank(", ", address.line1(), address.line2());
    }

    /** e.g. "Ponsonby, Auckland 1011", with the country only when it isn't New Zealand. */
    public static String locality(Address.Details address) {
        String place = joinNonBlank(" ", joinNonBlank(", ", address.suburb(), address.city()), address.postCode());
        return HOME_COUNTRY.equalsIgnoreCase(address.country())
                ? place
                : joinNonBlank(", ", place, address.country());
    }

    private static List<Label> badges(Address.Details address) {
        List<Label> badges = new ArrayList<>();
        if (address.isPrimary()) {
            badges.add(RecordTiles.primaryBadge());
        }
        if (address.isMailing()) {
            Label mailing = RecordTiles.badge("Mailing", "badge-mailing");
            mailing.setGraphic(envelopeIcon());
            badges.add(mailing);
        }
        return badges;
    }

    /** A small outlined envelope, drawn rather than a font glyph so it renders the same everywhere. */
    private static SVGPath envelopeIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M1 1h10v8H1z M1 1l5 4 5-4");
        icon.getStyleClass().add("badge-icon");
        return icon;
    }

    private static String joinNonBlank(String separator, String... parts) {
        return Stream.of(parts).filter(part -> !isBlank(part)).collect(Collectors.joining(separator));
    }
}
