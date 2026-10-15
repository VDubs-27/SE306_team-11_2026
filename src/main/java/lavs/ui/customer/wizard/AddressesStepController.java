package lavs.ui.customer.wizard;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Address;
import lavs.ui.common.AddressRows;
import lavs.ui.common.EditableList;
import lavs.ui.common.RecordEditor;
import lavs.ui.customer.forms.AddressFormController;

/**
 * Step 2 of the new customer wizard: the customer's addresses, added and edited in a pop-up.
 * At least one must be the mailing address before moving on.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class AddressesStepController {

    @FXML private Node addressForm;
    @FXML private AddressFormController addressFormController;
    @FXML private VBox addressList;
    @FXML private Label mailingChip;
    @FXML private HBox errorBanner;

    private EditableList<Address.Details> addresses;

    @FXML
    private void initialize() {
        show(errorBanner, false);
        // A customer has one primary and one mailing address
        addresses = new EditableList<>(new RecordEditor<>(addressList, "address", addressForm, addressFormController),
                addressList, AddressRows::tile, "No addresses added yet.")
                .keepOnlyOne(Address.Details::isPrimary,
                        address -> withFlags(address, false, address.isMailing()))
                .keepOnlyOne(Address.Details::isMailing,
                        address -> withFlags(address, address.isPrimary(), false))
                .onChanged(this::refresh);
    }

    /** Gives the step the draft whose addresses it edits. */
    void use(NewCustomerDraft draft) {
        addresses.use(draft.addresses());
        refresh();
    }

    /** True when there is a mailing address; otherwise shows why the step can't move on. */
    boolean check() {
        boolean ok = hasMailingAddress();
        show(errorBanner, !ok);
        return ok;
    }

    @FXML
    private void handleAddAddress() {
        addresses.add();
    }

    private void refresh() {
        show(mailingChip, hasMailingAddress());
        if (hasMailingAddress()) {
            show(errorBanner, false);
        }
    }

    private boolean hasMailingAddress() {
        return addresses.items().stream().anyMatch(Address.Details::isMailing);
    }

    private static Address.Details withFlags(Address.Details address, boolean primary, boolean mailing) {
        return new Address.Details(address.type(), address.otherType(), address.line1(), address.line2(),
                address.suburb(), address.city(), address.postCode(), address.country(), primary, mailing);
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
