package lavs.ui.customer.profile;

import java.util.function.LongConsumer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Address;
import lavs.ui.common.AddressRows;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordEditor;
import lavs.ui.common.ServiceList;
import lavs.ui.customer.forms.AddressFormController;

/**
 * The Addresses tab: one tile per address, a page at a time from the mainframe, plus a way to add
 * another. Add and Edit use the same address pop-up as the new customer wizard.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class AddressesTabController {

    private static final long PAGE_SIZE = 5;

    @FXML private VBox addressList;
    @FXML private PagerController pagerController;
    @FXML private Node addressForm;
    @FXML private AddressFormController addressFormController;

    private ServiceList<Address, Address.Details> addresses;

    @FXML
    private void initialize() {
        addresses = new ServiceList<>(new RecordEditor<>(addressList, "address", addressForm, addressFormController),
                addressList, pagerController, PAGE_SIZE,
                (address, onEdit) -> AddressRows.tile(address.details(), onEdit), Address::details,
                "No addresses recorded.");
    }

    /**
     * Shows the customer's addresses from this source. onTotal hears how many there are, for the tab's
     * count, and onChanged runs after each change, e.g. as the primary address may have moved.
     */
    void show(ServiceList.Source<Address, Address.Details> source, LongConsumer onTotal, Runnable onChanged) {
        addresses.onTotal(onTotal).onChanged(onChanged).show(source);
    }

    @FXML
    private void handleAddAddress() {
        addresses.add();
    }
}
