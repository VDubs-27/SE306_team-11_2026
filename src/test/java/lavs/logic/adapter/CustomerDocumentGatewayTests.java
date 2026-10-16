package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.document.Document;
import mainframe.client.MessageRequest;
import org.junit.jupiter.api.Test;

// The shared document behaviour is covered by DocumentGatewayTests and DocumentServiceTests; these check the
// customer messages are sent with the customer number
class CustomerDocumentGatewayTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final Document.Details DETAILS =
            new Document.Details("Proof of Identity", "passport.pdf", "application/pdf", 123);

    private final ScriptedConnection connection = new ScriptedConnection();
    private final DocumentGateway<CustomerNumber> gateway =
            DocumentGateway.forCustomers(new MainframeAdapter(connection));

    @Test
    void listSendsTheCustomerAndReturnsOnePageOfSummaries() {
        connection.replyList("customerDocument.list", 3,
                row("id", "4", "type", "Proof of Identity", "fileName", "passport.pdf"),
                row("id", "5", "type", "Proof of Address", "fileName", "power-bill.pdf"));

        Page<Document.Summary> page = gateway.list(CUSTOMER, 1, 2);

        MessageRequest request = connection.onlyRequest("customerDocument.list");
        assertAll(
                () -> assertEquals(List.of(
                        new Document.Summary(4, "Proof of Identity", "passport.pdf"),
                        new Document.Summary(5, "Proof of Address", "power-bill.pdf")), page.items()),
                () -> assertEquals(3, page.total()),
                () -> assertEquals("01-0013", request.getValue("number")),
                () -> assertEquals("1", request.getValue("page")),
                () -> assertEquals("2", request.getValue("size"))
        );
    }

    @Test
    void retrieveSendsTheCustomerAndId() {
        connection.reply("customerDocument.retrieve", row(
                "id", "4", "type", "Proof of Identity", "fileName", "passport.pdf", "fileType", "application/pdf",
                "size", "123"));

        Document document = gateway.retrieve(CUSTOMER, 4);

        MessageRequest request = connection.onlyRequest("customerDocument.retrieve");
        assertAll(
                () -> assertEquals(new Document(4, "Proof of Identity", "passport.pdf", "application/pdf", 123),
                        document),
                () -> assertEquals("4", request.getValue("id")),
                () -> assertEquals("01-0013", request.getValue("number"))
        );
    }

    @Test
    void addSendsEveryFieldWithTheCustomer() {
        connection.reply("customer.addDocument", row("id", "12", "size", "123"));

        Document document = gateway.add(CUSTOMER, DETAILS);

        MessageRequest request = connection.onlyRequest("customer.addDocument");
        assertAll(
                () -> assertEquals(new Document(12, "Proof of Identity", "passport.pdf", "application/pdf", 123),
                        document),
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("Proof of Identity", request.getValue("type")),
                () -> assertEquals("passport.pdf", request.getValue("fileName")),
                () -> assertEquals("application/pdf", request.getValue("fileType")),
                () -> assertEquals("123", request.getValue("size"))
        );
    }

    @Test
    void deleteSendsTheCustomerAndId() {
        connection.reply("customer.deleteDocument", row("id", "12"));

        gateway.delete(CUSTOMER, 12);

        MessageRequest request = connection.onlyRequest("customer.deleteDocument");
        assertAll(
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("12", request.getValue("id"))
        );
    }

    @Test
    void requiresAdapter() {
        assertThrows(NullPointerException.class, () -> DocumentGateway.forCustomers(null));
    }
}
