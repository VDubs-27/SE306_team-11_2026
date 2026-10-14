package lavs.logic.service;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import lavs.logic.adapter.AddressGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.ScriptedConnection;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.CustomerNumber;
import org.junit.jupiter.api.Test;

class AddressServiceTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final Constants CONSTANTS = new Constants(Map.of(
            ConstantCategory.ADDRESS_TYPE, List.of("Residential", "Postal", "Other")));

    private final ScriptedConnection connection = new ScriptedConnection();
    private final AddressService service =
            new AddressService(new AddressGateway(new MainframeAdapter(connection)), () -> CONSTANTS);

    private static Address.Details details(Address.Type type, boolean isPrimary) {
        return new Address.Details(
                type, null, "1 Queen Street", null, null, "Auckland", "1010", "New Zealand", isPrimary, true);
    }

    @Test
    void listReturnsThePage() {
        connection.replyList("customerAddress.list", 1,
                row("id", "4", "type", "Postal", "isPrimary", "y", "isMailing", "y"));

        Result<Page<Address.Summary>> result = service.list(CUSTOMER, 1, 10);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(1, result.value().total())
        );
    }

    @Test
    void listFailureIsReported() {
        connection.fail("customerAddress.list", 10, "Unavailable");

        Result<Page<Address.Summary>> result = service.list(CUSTOMER, 1, 10);

        assertEquals("Could not reach the mainframe. Check the connection and try again.", result.errorMessage());
    }

    @Test
    void getReturnsTheAddress() {
        connection.reply("customerAddress.retrieve", row("id", "4", "type", "Postal", "line1", "1 Queen Street"));

        Result<Address> result = service.get(CUSTOMER, 4);

        assertEquals("1 Queen Street", result.value().line1());
    }

    @Test
    void validAddressIsAdded() {
        connection.reply("customer.addAddress", row("id", "12"));

        Result<Address> result = service.add(CUSTOMER, details(Address.Type.POSTAL, false));

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(12, result.value().id()),
                () -> assertEquals(1, connection.requests("customer.addAddress").size())
        );
    }

    @Test
    void invalidDetailsAreRejectedWithoutCallingTheMainframe() {
        Address.Details missingLine1 = new Address.Details(
                Address.Type.POSTAL, null, " ", null, null, null, null, "New Zealand", false, false);

        Result<Address> result = service.add(CUSTOMER, missingLine1);

        assertAll(
                () -> assertEquals(List.of(new FieldError("line1", "Address line 1 is required.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requests("customer.addAddress").isEmpty())
        );
    }

    @Test
    void typeTheMainframeDoesNotOfferIsRejected() {
        Result<Address> result = service.add(CUSTOMER, details(Address.Type.DELEGATED, false));

        assertAll(
                () -> assertEquals(List.of(new FieldError("type", "Address type must be one of the listed options.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requests("customer.addAddress").isEmpty())
        );
    }

    @Test
    void unknownTypeIsReportedOnce() {
        Result<Address> result = service.add(CUSTOMER, details(Address.Type.UNKNOWN, false));

        assertEquals(1, result.fieldErrors().stream().filter(error -> error.field().equals("type")).count());
    }

    @Test
    void secondPrimaryAddressIsRejected() {
        connection.replyList("customerAddress.list", 1, row("id", "4", "type", "Postal", "isPrimary", "y"));

        Result<Address> result = service.add(CUSTOMER, details(Address.Type.POSTAL, true));

        assertAll(
                () -> assertEquals(List.of(new FieldError("isPrimary", "This customer already has a primary address.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requests("customer.addAddress").isEmpty())
        );
    }

    @Test
    void firstPrimaryAddressIsAdded() {
        connection
                .replyList("customerAddress.list", 1, row("id", "4", "type", "Postal", "isPrimary", "n"))
                .reply("customer.addAddress", row("id", "12"));

        Result<Address> result = service.add(CUSTOMER, details(Address.Type.POSTAL, true));

        assertTrue(result.successful());
    }

    @Test
    void nonPrimaryAddressSkipsThePrimaryCheck() {
        connection.reply("customer.addAddress", row("id", "12"));

        service.add(CUSTOMER, details(Address.Type.POSTAL, false));

        assertTrue(connection.requests("customerAddress.list").isEmpty());
    }

    @Test
    void primaryAddressCanBeUpdatedAndStayPrimary() {
        connection
                .replyList("customerAddress.list", 1, row("id", "4", "type", "Postal", "isPrimary", "y"))
                .reply("customer.updateAddress", row("id", "4"));

        Result<Address> result = service.update(CUSTOMER, 4, details(Address.Type.POSTAL, true));

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("4", connection.onlyRequest("customer.updateAddress").getValue("id"))
        );
    }

    @Test
    void updateToPrimaryIsRejectedWhenAnotherAddressIsPrimary() {
        connection.replyList("customerAddress.list", 1, row("id", "4", "type", "Postal", "isPrimary", "y"));

        Result<Address> result = service.update(CUSTOMER, 9, details(Address.Type.POSTAL, true));

        assertAll(
                () -> assertEquals("isPrimary", result.fieldErrors().get(0).field()),
                () -> assertTrue(connection.requests("customer.updateAddress").isEmpty())
        );
    }

    @Test
    void mainframeRejectionOfASaveIsReported() {
        connection.fail("customer.addAddress", 3, "Customer is archived");

        Result<Address> result = service.add(CUSTOMER, details(Address.Type.POSTAL, false));

        assertAll(
                () -> assertFalse(result.successful()),
                () -> assertEquals("Customer is archived", result.errorMessage())
        );
    }

    @Test
    void deleteRemovesTheAddress() {
        connection.reply("customer.deleteAddress", row("id", "4"));

        Result<Void> result = service.delete(CUSTOMER, 4);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("4", connection.onlyRequest("customer.deleteAddress").getValue("id"))
        );
    }

    @Test
    void deleteFailureIsReported() {
        connection.fail("customer.deleteAddress", 3, "Address not found");

        assertEquals("Address not found", service.delete(CUSTOMER, 4).errorMessage());
    }

    @Test
    void requiresGatewayAndConstants() {
        AddressGateway gateway = new AddressGateway(new MainframeAdapter(connection));

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new AddressService(null, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class, () -> new AddressService(gateway, null))
        );
    }
}
