package lavs.logic.service;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lavs.logic.adapter.LoanDocumentGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.ScriptedConnection;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.loan.LoanDocument;
import lavs.logic.model.loan.LoanNumber;
import org.junit.jupiter.api.Test;

class LoanDocumentServiceTests {

    private static final LoanNumber LOAN = new LoanNumber("01-0001-0000015-001");
    private static final LoanDocument.Details DETAILS =
            new LoanDocument.Details("Proof of Identity", "passport.pdf", "application/pdf", 123);

    private final ScriptedConnection connection = new ScriptedConnection();
    private final LoanDocumentService service =
            new LoanDocumentService(new LoanDocumentGateway(new MainframeAdapter(connection)));

    @Test
    void listReturnsThePage() {
        connection.replyList("loanDocument.list", 1,
                row("id", "4", "type", "Proof of Identity", "fileName", "passport.pdf"));

        Result<Page<LoanDocument.Summary>> result = service.list(LOAN, 1, 10);

        assertEquals("passport.pdf", result.value().items().get(0).fileName());
    }

    @Test
    void listFailureIsReported() {
        connection.fail("loanDocument.list", 10, "Unavailable");

        Result<Page<LoanDocument.Summary>> result = service.list(LOAN, 1, 10);

        assertEquals("Could not reach the mainframe. Check the connection and try again.", result.errorMessage());
    }

    @Test
    void getReturnsTheDocument() {
        connection.reply("loanDocument.retrieve", row(
                "id", "4", "type", "Proof of Identity", "fileName", "passport.pdf", "fileType", "application/pdf",
                "size", "123"));

        assertEquals("application/pdf", service.get(LOAN, 4).value().fileType());
    }

    @Test
    void validDocumentIsAdded() {
        connection.reply("loan.addDocument", row("id", "12", "size", "123"));

        Result<LoanDocument> result = service.add(LOAN, DETAILS);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(12, result.value().id())
        );
    }

    @Test
    void invalidDetailsAreRejectedWithoutCallingTheMainframe() {
        Result<LoanDocument> result =
                service.add(LOAN, new LoanDocument.Details("Proof of Identity", "", "application/pdf", 123));

        assertAll(
                () -> assertEquals(List.of(new FieldError("fileName", "File name is required.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.addDocument").isEmpty())
        );
    }

    @Test
    void mainframeRejectionOfAnAddIsReported() {
        connection.fail("loan.addDocument", 3, "Loan is archived");

        assertEquals("Loan is archived", service.add(LOAN, DETAILS).errorMessage());
    }

    @Test
    void deleteRemovesTheDocument() {
        connection.reply("loan.deleteDocument", row("id", "4"));

        Result<Void> result = service.delete(LOAN, 4);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("4", connection.onlyRequest("loan.deleteDocument").getValue("id"))
        );
    }

    @Test
    void deleteFailureIsReported() {
        connection.fail("loan.deleteDocument", 3, "Document not found");

        assertEquals("Document not found", service.delete(LOAN, 4).errorMessage());
    }

    @Test
    void requiresGateway() {
        assertThrows(NullPointerException.class, () -> new LoanDocumentService(null));
    }
}
