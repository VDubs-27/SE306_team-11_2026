package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Address;
import lavs.ui.common.AddressRows;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordEditor;
import lavs.ui.common.RecordTiles;
import lavs.ui.customer.forms.AddressFormController;

/**
 * The Addresses tab: one tile per address, plus a way to add another. Add and Edit use the same
 * address pop-up as the new customer wizard.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class AddressesTabController {

    @FXML private VBox addressList;
    @FXML private PagerController pagerController;
    @FXML private Node addressForm;
    @FXML private AddressFormController addressFormController;

    private RecordEditor<Address.Details> editor;

    @FXML
    private void initialize() {
        editor = new RecordEditor<>(addressList, "address", addressForm, addressFormController);
    }

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<Address>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(addressList, items.stream()
                .map(address -> AddressRows.tile(address.details(), () -> handleEditAddress(address)))
                .toList(), "No addresses recorded."));
    }

    @FXML
    private void handleAddAddress() {
        editor.add(false, address -> {
            // Later: CustomerService.addAddress(customerNumber, address), then reload the tab
        });
    }

    private void handleEditAddress(Address address) {
        editor.edit(address.details(), changed -> {
            // Later: CustomerService.updateAddress(address.id(), changed), then reload the tab
        }, () -> {
            // Later: CustomerService.removeAddress(address.id()), then reload the tab
        });
    }
}
