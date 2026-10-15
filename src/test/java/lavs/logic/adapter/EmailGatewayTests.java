package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Email;
import mainframe.client.MessageRequest;
import org.junit.jupiter.api.Test;

class EmailGatewayTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final Email.Details DETAILS =
            new Email.Details(Email.Type.WORK, null, "jo.smith@example.com", true);

    private final ScriptedConnection connection = new ScriptedConnection();
    private final EmailGateway gateway = new EmailGateway(new MainframeAdapter(connection));

    @Test
    void listReturnsOnePageOfSummaries() {
        connection.replyList("customerEmail.list", 1,
                row("id", "4", "type", "Personal", "address", "jo@example.com", "isPrimary", "y"));

        Page<Email.Summary> page = gateway.list(CUSTOMER, 1, 10);

        assertAll(
                () -> assertEquals(List.of(
                        new Email.Summary(4, CUSTOMER, Email.Type.PERSONAL, "jo@example.com", true)), page.items()),
                () -> assertEquals(1, page.total()),
                () -> assertEquals("01-0013", connection.onlyRequest("customerEmail.list").getValue("number"))
        );
    }

    @Test
    void allReadsEveryPage() {
        connection
                .replyList("customerEmail.list", 2, row("id", "1", "type", "Work"))
                .replyList("customerEmail.list", 2, row("id", "2", "type", "Personal"));

        assertEquals(List.of(1L, 2L), gateway.all(CUSTOMER).stream().map(Email.Summary::id).toList());
    }

    @Test
    void countAsksForOneItemAndReturnsTheTotal() {
        connection.replyList("customerEmail.list", 3, row("id", "1", "type", "Work"));

        long count = gateway.count(CUSTOMER);

        assertAll(
                () -> assertEquals(3, count),
                () -> assertEquals("1", connection.onlyRequest("customerEmail.list").getValue("size"))
        );
    }

    @Test
    void retrieveReturnsTheFullEmail() {
        connection.reply("customerEmail.retrieve", row(
                "id", "4", "type", "Other", "otherType", "Club", "address", "jo@club.example.com", "isPrimary", "n"));

        Email email = gateway.retrieve(CUSTOMER, 4);

        assertEquals(new Email(4, CUSTOMER, Email.Type.OTHER, "Club", "jo@club.example.com", false), email);
    }

    @Test
    void addSendsEveryFieldAndReturnsTheNewEmail() {
        connection.reply("customer.addEmail", row("id", "12"));

        Email email = gateway.add(CUSTOMER, DETAILS);

        MessageRequest request = connection.onlyRequest("customer.addEmail");
        assertAll(
                () -> assertEquals(12, email.id()),
                () -> assertEquals(DETAILS, email.details()),
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("Work", request.getValue("type")),
                () -> assertEquals("jo.smith@example.com", request.getValue("address")),
                () -> assertEquals("y", request.getValue("isPrimary"))
        );
    }

    @Test
    void updateSendsTheIdAndReturnsTheUpdatedEmail() {
        connection.reply("customer.updateEmail", row("id", "12"));

        Email email = gateway.update(CUSTOMER, 12, DETAILS);

        assertAll(
                () -> assertEquals(12, email.id()),
                () -> assertEquals("12", connection.onlyRequest("customer.updateEmail").getValue("id"))
        );
    }

    @Test
    void deleteSendsTheCustomerAndId() {
        connection.reply("customer.deleteEmail", row("id", "12"));

        gateway.delete(CUSTOMER, 12);

        assertEquals("12", connection.onlyRequest("customer.deleteEmail").getValue("id"));
    }

    @Test
    void requiresAdapter() {
        assertThrows(NullPointerException.class, () -> new EmailGateway(null));
    }
}
