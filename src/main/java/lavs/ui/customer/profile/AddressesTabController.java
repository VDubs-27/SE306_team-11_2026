package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Address;

/** The Addresses tab: one tile per address, plus a way to add another. */
public class AddressesTabController {

    private static final String HOME_COUNTRY = "New Zealand";

    @FXML private VBox addressList;
    @FXML private PagerController pagerController;

    private LongFunction<Page<Address>> loadPage = page -> new Page<>(List.of(), 1, 1, 0);

    @FXML
    private void initialize() {
        pagerController.setOnPageRequested(this::showPage);
    }

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<Address>> loadPage) {
        this.loadPage = loadPage;
        return showPage(1).total();
    }

    private Page<Address> showPage(long pageNumber) {
        Page<Address> page = loadPage.apply(pageNumber);
        RecordTiles.fill(addressList, page.items().stream()
                .map(address -> RecordTiles.tile(typeName(address), street(address), locality(address),
                        badges(address), () -> handleEditAddress(address)))
                .toList(), "No addresses recorded.");
        pagerController.show(page);
        return page;
    }

    @FXML
    private void handleAddAddress() {
        // Later: open the add address form
    }

    private void handleEditAddress(Address address) {
        // Later: open the edit address form for this address
    }

    private static String typeName(Address address) {
        return address.type() == Address.Type.OTHER && !isBlank(address.otherType())
                ? address.otherType()
                : address.type().code();
    }

    /** Line 1, with line 2 after it when there is one. */
    private static String street(Address address) {
        return joinNonBlank(", ", address.line1(), address.line2());
    }

    /** e.g. "Ponsonby, Auckland 1011", with the country only when it isn't New Zealand. */
    private static String locality(Address address) {
        String place = joinNonBlank(" ", joinNonBlank(", ", address.suburb(), address.city()), address.postCode());
        return HOME_COUNTRY.equalsIgnoreCase(address.country())
                ? place
                : joinNonBlank(", ", place, address.country());
    }

    private static List<Label> badges(Address address) {
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
