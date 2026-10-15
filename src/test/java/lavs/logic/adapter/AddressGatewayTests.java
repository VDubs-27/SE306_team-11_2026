package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import lavs.logic.model.Page;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.CustomerNumber;
import mainframe.client.MessageRequest;
import org.junit.jupiter.api.Test;

class AddressGatewayTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final Address.Details DETAILS = new Address.Details(
            Address.Type.OTHER, "Holiday home", "1 Queen Street", "Flat 2", "Ponsonby", "Auckland", "1011",
            "New Zealand", true, false);

    private final ScriptedConnection connection = new ScriptedConnection();
    private final AddressGateway gateway = new AddressGateway(new MainframeAdapter(connection));

    @Test
    void listReturnsOnePageOfSummaries() {
        connection.replyList("customerAddress.list", 7,
                row("id", "4", "type", "Postal", "city", "Auckland", "country", "New Zealand",
                        "isPrimary", "y", "isMailing", "n"),
                row("id", "9", "type", "Residential", "city", "Sydney", "country", "Australia",
                        "isPrimary", "n", "isMailing", "y"));

        Page<Address.Summary> page = gateway.list(CUSTOMER, 2, 5);

        MessageRequest request = connection.onlyRequest("customerAddress.list");
        assertAll(
                () -> assertEquals(List.of(
                        new Address.Summary(4, CUSTOMER, Address.Type.POSTAL, "Auckland", "New Zealand", true, false),
                        new Address.Summary(9, CUSTOMER, Address.Type.RESIDENTIAL, "Sydney", "Australia", false, true)),
                        page.items()),
                () -> assertEquals(2, page.page()),
                () -> assertEquals(5, page.size()),
                () -> assertEquals(7, page.total()),
                () -> assertEquals("01-0013", request.getValue("number")),
                () -> assertEquals("2", request.getValue("page")),
                () -> assertEquals("5", request.getValue("size"))
        );
    }

    @Test
    void allReadsEveryPage() {
        connection
                .replyList("customerAddress.list", 2, row("id", "1", "type", "Postal", "isPrimary", "n"))
                .replyList("customerAddress.list", 2, row("id", "2", "type", "Postal", "isPrimary", "y"));

        List<Address.Summary> all = gateway.all(CUSTOMER);

        assertAll(
                () -> assertEquals(List.of(1L, 2L), all.stream().map(Address.Summary::id).toList()),
                () -> assertEquals(2, connection.requests("customerAddress.list").size())
        );
    }

    @Test
    void retrieveReturnsTheFullAddress() {
        connection.reply("customerAddress.retrieve", row(
                "id", "4", "type", "Other", "otherType", "Holiday home", "line1", "1 Queen Street",
                "line2", "Flat 2", "suburb", "Ponsonby", "city", "Auckland", "postCode", "1011",
                "country", "New Zealand", "isPrimary", "y", "isMailing", "n"));

        Address address = gateway.retrieve(CUSTOMER, 4);

        MessageRequest request = connection.onlyRequest("customerAddress.retrieve");
        assertAll(
                () -> assertEquals(new Address(4, CUSTOMER, Address.Type.OTHER, "Holiday home", "1 Queen Street",
                        "Flat 2", "Ponsonby", "Auckland", "1011", "New Zealand", true, false), address),
                () -> assertEquals("4", request.getValue("id")),
                () -> assertEquals("01-0013", request.getValue("number"))
        );
    }

    @Test
    void addSendsEveryFieldAndReturnsTheNewAddress() {
        connection.reply("customer.addAddress", row("id", "12"));

        Address address = gateway.add(CUSTOMER, DETAILS);

        MessageRequest request = connection.onlyRequest("customer.addAddress");
        assertAll(
                () -> assertEquals(12, address.id()),
                () -> assertEquals(DETAILS, address.details()),
                () -> assertEquals(CUSTOMER, address.customerNumber()),
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("Other", request.getValue("type")),
                () -> assertEquals("Holiday home", request.getValue("otherType")),
                () -> assertEquals("1 Queen Street", request.getValue("line1")),
                () -> assertEquals("Flat 2", request.getValue("line2")),
                () -> assertEquals("Ponsonby", request.getValue("suburb")),
                () -> assertEquals("Auckland", request.getValue("city")),
                () -> assertEquals("1011", request.getValue("postCode")),
                () -> assertEquals("New Zealand", request.getValue("country")),
                () -> assertEquals("y", request.getValue("isPrimary")),
                () -> assertEquals("n", request.getValue("isMailing"))
        );
    }

    @Test
    void updateSendsTheIdAndReturnsTheUpdatedAddress() {
        connection.reply("customer.updateAddress", row("id", "12"));

        Address address = gateway.update(CUSTOMER, 12, DETAILS);

        MessageRequest request = connection.onlyRequest("customer.updateAddress");
        assertAll(
                () -> assertEquals(12, address.id()),
                () -> assertEquals(DETAILS, address.details()),
                () -> assertEquals("12", request.getValue("id")),
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("1 Queen Street", request.getValue("line1"))
        );
    }

    @Test
    void deleteSendsTheCustomerAndId() {
        connection.reply("customer.deleteAddress", row("id", "12"));

        gateway.delete(CUSTOMER, 12);

        MessageRequest request = connection.onlyRequest("customer.deleteAddress");
        assertAll(
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("12", request.getValue("id"))
        );
    }

    @Test
    void mainframeErrorsAreThrown() {
        connection.fail("customerAddress.retrieve", 3, "Address not found");

        MainframeException e = assertThrows(MainframeException.class, () -> gateway.retrieve(CUSTOMER, 4));

        assertEquals("Address not found", e.getMessage());
    }

    @Test
    void requiresAdapter() {
        assertThrows(NullPointerException.class, () -> new AddressGateway(null));
    }
}
