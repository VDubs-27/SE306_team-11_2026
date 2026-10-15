package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Address;
import lavs.logic.service.Result;
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
        editor.addAndSave(false, address -> {
            // Later: return addressService.add(customerNumber, address), then reload the tab
            return Result.success(null);
        });
    }

    private void handleEditAddress(Address address) {
        editor.editAndSave(address.details(), changed -> {
            // Later: return addressService.update(customerNumber, address.id(), changed), then reload the tab
            return Result.success(null);
        }, () -> {
            // Later: return addressService.delete(customerNumber, address.id()), then reload the tab
            return Result.success(null);
        });
    }
}
