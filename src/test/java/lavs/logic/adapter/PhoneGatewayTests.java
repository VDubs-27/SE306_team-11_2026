package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Phone;
import mainframe.client.MessageRequest;
import org.junit.jupiter.api.Test;

class PhoneGatewayTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final Phone.Details DETAILS =
            new Phone.Details(Phone.Type.MOBILE, null, "+64", "21 555 0101", true, true);

    private final ScriptedConnection connection = new ScriptedConnection();
    private final PhoneGateway gateway = new PhoneGateway(new MainframeAdapter(connection));

    @Test
    void listReturnsOnePageOfSummaries() {
        connection.replyList("customerPhoneNumber.list", 3,
                row("id", "4", "type", "Mobile", "countryCode", "+64", "number", "21 555 0101",
                        "isPrimary", "y", "canTxt", "y"),
                row("id", "5", "type", "Home", "countryCode", "+64", "number", "9 555 0101",
                        "isPrimary", "n", "canTxt", "n"));

        Page<Phone.Summary> page = gateway.list(CUSTOMER, 1, 2);

        MessageRequest request = connection.onlyRequest("customerPhoneNumber.list");
        assertAll(
                () -> assertEquals(List.of(
                        new Phone.Summary(4, CUSTOMER, Phone.Type.MOBILE, "+64", "21 555 0101", true, true),
                        new Phone.Summary(5, CUSTOMER, Phone.Type.HOME, "+64", "9 555 0101", false, false)),
                        page.items()),
                () -> assertEquals(3, page.total()),
                () -> assertEquals("01-0013", request.getValue("number")),
                () -> assertEquals("2", request.getValue("size"))
        );
    }

    @Test
    void allReadsEveryPage() {
        connection
                .replyList("customerPhoneNumber.list", 2, row("id", "1", "type", "Home"))
                .replyList("customerPhoneNumber.list", 2, row("id", "2", "type", "Work"));

        assertEquals(List.of(1L, 2L), gateway.all(CUSTOMER).stream().map(Phone.Summary::id).toList());
    }

    @Test
    void countAsksForOneItemAndReturnsTheTotal() {
        connection.replyList("customerPhoneNumber.list", 6, row("id", "1", "type", "Home"));

        long count = gateway.count(CUSTOMER);

        assertAll(
                () -> assertEquals(6, count),
                () -> assertEquals("1", connection.onlyRequest("customerPhoneNumber.list").getValue("size"))
        );
    }

    @Test
    void retrieveReturnsTheFullPhone() {
        connection.reply("customerPhoneNumber.retrieve", row(
                "id", "4", "type", "Other", "otherType", "Holiday house", "countryCode", "+61",
                "number", "2 5550 1234", "isPrimary", "n", "canTxt", "y"));

        Phone phone = gateway.retrieve(CUSTOMER, 4);

        assertAll(
                () -> assertEquals(new Phone(4, CUSTOMER, Phone.Type.OTHER, "Holiday house", "+61", "2 5550 1234",
                        false, true), phone),
                () -> assertEquals("4", connection.onlyRequest("customerPhoneNumber.retrieve").getValue("id"))
        );
    }

    @Test
    void addSendsEveryFieldAndReturnsTheNewPhone() {
        connection.reply("customer.addPhoneNumber", row("id", "12"));

        Phone phone = gateway.add(CUSTOMER, DETAILS);

        MessageRequest request = connection.onlyRequest("customer.addPhoneNumber");
        assertAll(
                () -> assertEquals(12, phone.id()),
                () -> assertEquals(DETAILS, phone.details()),
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("Mobile", request.getValue("type")),
                () -> assertEquals("+64", request.getValue("countryCode")),
                () -> assertEquals("21 555 0101", request.getValue("number")),
                () -> assertEquals("y", request.getValue("isPrimary")),
                () -> assertEquals("y", request.getValue("canTxt"))
        );
    }

    @Test
    void updateSendsTheIdAndReturnsTheUpdatedPhone() {
        connection.reply("customer.updatePhoneNumber", row("id", "12"));

        Phone phone = gateway.update(CUSTOMER, 12, DETAILS);

        assertAll(
                () -> assertEquals(12, phone.id()),
                () -> assertEquals(DETAILS, phone.details()),
                () -> assertEquals("12", connection.onlyRequest("customer.updatePhoneNumber").getValue("id"))
        );
    }

    @Test
    void deleteSendsTheCustomerAndId() {
        connection.reply("customer.deletePhoneNumber", row("id", "12"));

        gateway.delete(CUSTOMER, 12);

        MessageRequest request = connection.onlyRequest("customer.deletePhoneNumber");
        assertAll(
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("12", request.getValue("id"))
        );
    }

    @Test
    void requiresAdapter() {
        assertThrows(NullPointerException.class, () -> new PhoneGateway(null));
    }
}
