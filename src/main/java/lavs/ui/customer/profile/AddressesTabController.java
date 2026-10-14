package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Address;
import lavs.ui.common.AddressRows;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

/**
 * The Addresses tab: one tile per address, plus a way to add another.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class AddressesTabController {

    @FXML private VBox addressList;
    @FXML private PagerController pagerController;

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<Address>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(addressList, items.stream()
                .map(address -> AddressRows.tile(address.details(), () -> handleEditAddress(address)))
                .toList(), "No addresses recorded."));
    }

    @FXML
    private void handleAddAddress() {
        // Later: open the add address form
    }

    private void handleEditAddress(Address address) {
        // Later: open the edit address form for this address
    }
}
