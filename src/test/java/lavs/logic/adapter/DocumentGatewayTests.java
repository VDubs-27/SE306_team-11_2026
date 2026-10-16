package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import lavs.logic.model.Page;
import lavs.logic.model.document.Document;
import lavs.logic.model.loan.LoanNumber;
import mainframe.client.MessageRequest;
import org.junit.jupiter.api.Test;

class DocumentGatewayTests {

    private static final LoanNumber LOAN = new LoanNumber("01-0001-0000015-001");
    private static final Document.Details DETAILS =
            new Document.Details("Proof of Identity", "passport.pdf", "application/pdf", 123);

    private final ScriptedConnection connection = new ScriptedConnection();
    private final DocumentGateway<LoanNumber> gateway = DocumentGateway.forLoans(new MainframeAdapter(connection));

    @Test
    void listReturnsOnePageOfSummaries() {
        connection.replyList("loanDocument.list", 3,
                row("id", "4", "type", "Proof of Identity", "fileName", "passport.pdf"),
                row("id", "5", "type", "Proof of Income", "fileName", "payslip.pdf"));

        Page<Document.Summary> page = gateway.list(LOAN, 1, 2);

        MessageRequest request = connection.onlyRequest("loanDocument.list");
        assertAll(
                () -> assertEquals(List.of(
                        new Document.Summary(4, "Proof of Identity", "passport.pdf"),
                        new Document.Summary(5, "Proof of Income", "payslip.pdf")), page.items()),
                () -> assertEquals(3, page.total()),
                () -> assertEquals("01-0001-0000015-001", request.getValue("number")),
                () -> assertEquals("1", request.getValue("page")),
                () -> assertEquals("2", request.getValue("size"))
        );
    }

    @Test
    void retrieveReturnsTheFullDocument() {
        connection.reply("loanDocument.retrieve", row(
                "id", "4", "type", "Proof of Identity", "fileName", "passport.pdf", "fileType", "application/pdf",
                "size", "123"));

        Document document = gateway.retrieve(LOAN, 4);

        MessageRequest request = connection.onlyRequest("loanDocument.retrieve");
        assertAll(
                () -> assertEquals(new Document(4, "Proof of Identity", "passport.pdf", "application/pdf",
                        123), document),
                () -> assertEquals("4", request.getValue("id")),
                () -> assertEquals("01-0001-0000015-001", request.getValue("number"))
        );
    }

    @Test
    void addSendsEveryFieldAndReturnsTheNewDocument() {
        connection.reply("loan.addDocument", row("id", "12", "size", "123"));

        Document document = gateway.add(LOAN, DETAILS);

        MessageRequest request = connection.onlyRequest("loan.addDocument");
        assertAll(
                () -> assertEquals(new Document(12, "Proof of Identity", "passport.pdf", "application/pdf",
                        123), document),
                () -> assertEquals("01-0001-0000015-001", request.getValue("loanNumber")),
                () -> assertEquals("Proof of Identity", request.getValue("type")),
                () -> assertEquals("passport.pdf", request.getValue("fileName")),
                () -> assertEquals("application/pdf", request.getValue("fileType")),
                () -> assertEquals("123", request.getValue("size"))
        );
    }

    @Test
    void deleteSendsTheLoanAndId() {
        connection.reply("loan.deleteDocument", row("id", "12"));

        gateway.delete(LOAN, 12);

        MessageRequest request = connection.onlyRequest("loan.deleteDocument");
        assertAll(
                () -> assertEquals("01-0001-0000015-001", request.getValue("loanNumber")),
                () -> assertEquals("12", request.getValue("id"))
        );
    }

    @Test
    void mainframeErrorsAreThrown() {
        connection.fail("loanDocument.retrieve", 3, "Document not found");

        MainframeException e = assertThrows(MainframeException.class, () -> gateway.retrieve(LOAN, 4));

        assertEquals("Document not found", e.getMessage());
    }

    @Test
    void requiresAdapter() {
        assertThrows(NullPointerException.class, () -> DocumentGateway.forLoans(null));
    }
}
