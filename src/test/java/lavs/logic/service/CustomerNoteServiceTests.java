package lavs.logic.service;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lavs.logic.adapter.CustomerNoteGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.ScriptedConnection;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNote;
import lavs.logic.model.customer.CustomerNumber;
import org.junit.jupiter.api.Test;

class CustomerNoteServiceTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final CustomerNote.Details DETAILS =
            new CustomerNote.Details(CustomerNote.Type.CONTACT, "Prefers mobile");

    private final ScriptedConnection connection = new ScriptedConnection();
    private final CustomerNoteGateway gateway = new CustomerNoteGateway(new MainframeAdapter(connection));
    private final CustomerNoteService service = new CustomerNoteService(gateway);

    @Test
    void listReturnsThePageWithItsText() {
        connection
                .replyList("customerNote.list", 1, row("id", "7"))
                .reply("customerNote.retrieve", row("id", "7", "type", "Contact", "author", "Bob Ross",
                        "dateAdded", "2026-10-05", "lines", "Prefers mobile"));

        Result<Page<CustomerNote>> result = service.list(CUSTOMER, 1, 8);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("Prefers mobile", result.value().items().get(0).lines())
        );
    }

    @Test
    void listReportsMainframeErrors() {
        connection.fail("customerNote.list", 3, "Rejected");

        Result<Page<CustomerNote>> result = service.list(CUSTOMER, 1, 8);

        assertEquals("Rejected", result.errorMessage());
    }

    @Test
    void countReturnsTheTotal() {
        connection.replyList("customerNote.list", 12);

        assertEquals(12L, service.count(CUSTOMER).value());
    }

    @Test
    void getReturnsTheNote() {
        connection.reply("customerNote.retrieve", row("id", "7", "type", "Contact", "lines", "Prefers mobile"));

        assertEquals("Prefers mobile", service.get(CUSTOMER, 7).value().lines());
    }

    @Test
    void validNoteIsAddedAndReturnedWithItsDate() {
        connection
                .reply("customer.addNote", row("id", "12", "author", "Bob Ross"))
                .reply("customerNote.retrieve", row("id", "12", "type", "Contact", "author", "Bob Ross",
                        "dateAdded", "2026-10-05", "lines", "Prefers mobile"));

        Result<CustomerNote> result = service.add(CUSTOMER, DETAILS);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(12, result.value().id()),
                () -> assertEquals(LocalDate.of(2026, 10, 5), result.value().dateAdded())
        );
    }

    @Test
    void blankNoteIsRejectedWithoutCallingTheMainframe() {
        Result<CustomerNote> result = service.add(CUSTOMER, new CustomerNote.Details(CustomerNote.Type.CONTACT, " "));

        assertAll(
                () -> assertEquals(List.of(new FieldError("lines", "Note is required.")), result.fieldErrors()),
                () -> assertTrue(connection.requests("customer.addNote").isEmpty())
        );
    }

    @Test
    void unknownTypeIsReportedOnce() {
        Result<CustomerNote> result =
                service.add(CUSTOMER, new CustomerNote.Details(CustomerNote.Type.UNKNOWN, "Hi"));

        assertEquals(1, result.fieldErrors().size());
    }

    @Test
    void validNoteIsUpdated() {
        connection
                .reply("customer.updateNote", row("id", "7", "author", "Bob Ross"))
                .reply("customerNote.retrieve", row("id", "7", "type", "Contact", "author", "Bob Ross",
                        "dateAdded", "2026-10-05", "lines", "Prefers mobile"));

        Result<CustomerNote> result = service.update(CUSTOMER, 7, DETAILS);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("7", connection.onlyRequest("customer.updateNote").getValue("id"))
        );
    }

    @Test
    void updateFailureIsReported() {
        connection.fail("customer.updateNote", 3, "Note not found");

        assertEquals("Note not found", service.update(CUSTOMER, 7, DETAILS).errorMessage());
    }

    @Test
    void noteIsDeleted() {
        connection.reply("customer.deleteNote", row("id", "7"));

        Result<Void> result = service.delete(CUSTOMER, 7);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("7", connection.onlyRequest("customer.deleteNote").getValue("id"))
        );
    }

    @Test
    void deleteFailureIsReported() {
        connection.fail("customer.deleteNote", 3, "Note not found");

        assertEquals("Note not found", service.delete(CUSTOMER, 7).errorMessage());
    }

    @Test
    void requiresAGatewayAndConstants() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new CustomerNoteService(null))
        );
    }
}