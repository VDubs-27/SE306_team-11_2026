package lavs.logic.service;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import lavs.logic.adapter.DocumentGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.ScriptedConnection;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.document.Document;
import lavs.logic.model.loan.LoanNumber;
import org.junit.jupiter.api.Test;

class DocumentServiceTests {

    private static final LoanNumber LOAN = new LoanNumber("01-0001-0000015-001");
    private static final Document.Details DETAILS =
            new Document.Details("Proof of Identity", "passport.pdf", "application/pdf", 123);

    private static final Map<String, String> ORIGINAL = row(
            "id", "4", "type", "Proof of Income", "fileName", "payslip.pdf", "fileType", "application/pdf", "size", "99");

    private final ScriptedConnection connection = new ScriptedConnection();
    private final DocumentService<LoanNumber> service =
            new DocumentService<>(DocumentGateway.forLoans(new MainframeAdapter(connection)));

    @Test
    void listReturnsThePage() {
        connection.replyList("loanDocument.list", 1,
                row("id", "4", "type", "Proof of Identity", "fileName", "passport.pdf"));

        Result<Page<Document.Summary>> result = service.list(LOAN, 1, 10);

        assertEquals("passport.pdf", result.value().items().get(0).fileName());
    }

    @Test
    void listFailureIsReported() {
        connection.fail("loanDocument.list", 10, "Unavailable");

        Result<Page<Document.Summary>> result = service.list(LOAN, 1, 10);

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

        Result<Document> result = service.add(LOAN, DETAILS);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(12, result.value().id())
        );
    }

    @Test
    void invalidDetailsAreRejectedWithoutCallingTheMainframe() {
        Result<Document> result =
                service.add(LOAN, new Document.Details("Proof of Identity", "", "application/pdf", 123));

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
    void updateAddsTheReplacementThenDeletesTheOriginal() {
        connection
                .reply("loanDocument.retrieve", ORIGINAL)
                .reply("loan.addDocument", row("id", "13", "size", "123"))
                .reply("loan.deleteDocument", row("id", "4"));

        Result<Document> result = service.update(LOAN, 4, DETAILS);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(new Document(13, "Proof of Identity", "passport.pdf", "application/pdf",
                        123), result.value()),
                () -> assertEquals(List.of("loanDocument.retrieve", "loan.addDocument", "loan.deleteDocument"),
                        connection.requestTypes()),
                () -> assertEquals("4", connection.onlyRequest("loan.deleteDocument").getValue("id"))
        );
    }

    @Test
    void updateWithInvalidDetailsSendsNothing() {
        Result<Document> result =
                service.update(LOAN, 4, new Document.Details("", "passport.pdf", "application/pdf", 123));

        assertAll(
                () -> assertEquals(List.of(new FieldError("type", "Document type is required.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requestTypes().isEmpty())
        );
    }

    @Test
    void updateWithNoChangesKeepsTheOriginal() {
        connection.reply("loanDocument.retrieve", ORIGINAL);

        Result<Document> result =
                service.update(LOAN, 4, new Document.Details("Proof of Income", "payslip.pdf", "application/pdf", 99));

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(4, result.value().id()),
                () -> assertEquals(List.of("loanDocument.retrieve"), connection.requestTypes())
        );
    }

    @Test
    void updateOfAMissingDocumentAddsNothing() {
        connection.fail("loanDocument.retrieve", 3, "Document with id '4' does not exist.");

        Result<Document> result = service.update(LOAN, 4, DETAILS);

        assertAll(
                () -> assertEquals("Document with id '4' does not exist.", result.errorMessage()),
                () -> assertEquals(List.of("loanDocument.retrieve"), connection.requestTypes())
        );
    }

    @Test
    void failedAddKeepsTheOriginal() {
        connection
                .reply("loanDocument.retrieve", ORIGINAL)
                .fail("loan.addDocument", 10, "Unavailable");

        Result<Document> result = service.update(LOAN, 4, DETAILS);

        assertAll(
                () -> assertEquals("Could not reach the mainframe. Check the connection and try again.",
                        result.errorMessage()),
                () -> assertTrue(connection.requests("loan.deleteDocument").isEmpty())
        );
    }

    @Test
    void failedDeleteReportsTheDuplicateAndReturnsTheReplacement() {
        connection
                .reply("loanDocument.retrieve", ORIGINAL)
                .reply("loan.addDocument", row("id", "13", "size", "123"))
                .fail("loan.deleteDocument", 10, "Unavailable");

        Result<Document> result = service.update(LOAN, 4, DETAILS);

        assertAll(
                () -> assertFalse(result.successful()),
                () -> assertEquals(DocumentService.ORIGINAL_NOT_REMOVED_MESSAGE, result.errorMessage()),
                () -> assertEquals(13, result.value().id())
        );
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
        assertThrows(NullPointerException.class, () -> new DocumentService<>(null));
    }
}
