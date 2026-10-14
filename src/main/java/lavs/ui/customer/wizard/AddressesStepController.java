package lavs.ui.customer.wizard;

import java.util.List;
import java.util.stream.IntStream;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Address;
import lavs.ui.common.AddressRows;
import lavs.ui.common.FormDialog;
import lavs.ui.common.RecordTiles;
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

    private NewCustomerDraft draft;

    @FXML
    private void initialize() {
        show(errorBanner, false);
    }

    /** Gives the step the draft whose addresses it edits. */
    void use(NewCustomerDraft draft) {
        this.draft = draft;
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
        List<Address.Details> addresses = draft.addresses();
        addressFormController.clear(addresses.isEmpty());
        if (openForm("Add address", false) == FormDialog.Result.SAVED) {
            addresses.add(addressFormController.read());
            keepOnlyOne(addresses.size() - 1);
        }
        refresh();
    }

    private void handleEditAddress(int index) {
        List<Address.Details> addresses = draft.addresses();
        addressFormController.fill(addresses.get(index));
        FormDialog.Result result = openForm("Edit address", true);
        if (result == FormDialog.Result.SAVED) {
            addresses.set(index, addressFormController.read());
            keepOnlyOne(index);
        } else if (result == FormDialog.Result.REMOVED) {
            addresses.remove(index);
        }
        refresh();
    }

    private FormDialog.Result openForm(String title, boolean canRemove) {
        return FormDialog.show(addressList, title, addressForm, addressFormController::check, canRemove);
    }

    /**
     * A customer has one primary and one mailing address. When the address just saved is either,
     * the others stop being it.
     */
    private void keepOnlyOne(int savedIndex) {
        List<Address.Details> addresses = draft.addresses();
        Address.Details saved = addresses.get(savedIndex);
        for (int i = 0; i < addresses.size(); i++) {
            if (i != savedIndex) {
                Address.Details other = addresses.get(i);
                addresses.set(i, withFlags(other,
                        other.isPrimary() && !saved.isPrimary(),
                        other.isMailing() && !saved.isMailing()));
            }
        }
    }

    private void refresh() {
        List<Address.Details> addresses = draft.addresses();
        RecordTiles.fill(addressList, IntStream.range(0, addresses.size())
                .mapToObj(i -> AddressRows.tile(addresses.get(i), () -> handleEditAddress(i)))
                .toList(), "No addresses added yet.");
        show(mailingChip, hasMailingAddress());
        if (hasMailingAddress()) {
            show(errorBanner, false);
        }
    }

    private boolean hasMailingAddress() {
        return draft.addresses().stream().anyMatch(Address.Details::isMailing);
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
