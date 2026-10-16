package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNote;
import lavs.logic.model.customer.CustomerNumber;
import mainframe.client.MessageRequest;
import mainframe.client.StatusMessage;
import org.junit.jupiter.api.Test;

class CustomerNoteGatewayTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final CustomerNote.Details DETAILS =
            new CustomerNote.Details(CustomerNote.Type.CONTACT, "Prefers mobile");
    private static final CustomerNote NOTE = new CustomerNote(
            7, CUSTOMER, CustomerNote.Type.CONTACT, "Prefers mobile", "Bob Ross", LocalDate.of(2026, 10, 5));

    private final ScriptedConnection connection = new ScriptedConnection();
    private final CustomerNoteGateway gateway = new CustomerNoteGateway(new MainframeAdapter(connection));

    private static void assertUnreadable(MainframeException e) {
        assertEquals(StatusMessage.UNABLE_TO_PARSE.getCode().intValue(), e.errorCode());
    }

    @Test
    void listFetchesTheTextOfEachNote() {
        connection
                .replyList("customerNote.list", 1, row("id", "7", "type", "Contact", "author", "Bob Ross"))
                .reply("customerNote.retrieve", row("id", "7", "type", "Contact", "author", "Bob Ross",
                        "dateAdded", "2026-10-05", "lines", "Prefers mobile"));

        Page<CustomerNote> page = gateway.list(CUSTOMER, 1, 8);

        assertAll(
                () -> assertEquals(List.of(NOTE), page.items()),
                () -> assertEquals(1, page.total()),
                () -> assertEquals(1, page.page()),
                () -> assertEquals(8, page.size())
        );
    }

    @Test
    void listWithNoNotesIsEmptyAndFetchesNothing() {
        connection.replyList("customerNote.list", 0);

        Page<CustomerNote> page = gateway.list(CUSTOMER, 1, 8);

        assertAll(
                () -> assertTrue(page.items().isEmpty()),
                () -> assertTrue(connection.requests("customerNote.retrieve").isEmpty())
        );
    }

    @Test
    void listSendsTheCustomerPageAndSize() {
        connection.replyList("customerNote.list", 0);

        gateway.list(CUSTOMER, 2, 8);

        MessageRequest request = connection.onlyRequest("customerNote.list");
        assertAll(
                () -> assertEquals("01-0013", request.getValue("number")),
                () -> assertEquals("2", request.getValue("page")),
                () -> assertEquals("8", request.getValue("size"))
        );
    }

    @Test
    void listSkipsANoteWithAnUnreadableDateButKeepsTheTotal() {
        connection
                .replyList("customerNote.list", 1, row("id", "7"))
                .reply("customerNote.retrieve", row("id", "7", "type", "Contact", "dateAdded", "05/10/2026",
                        "lines", "Hi"));

        Page<CustomerNote> page = gateway.list(CUSTOMER, 1, 8);

        assertAll(
                () -> assertTrue(page.items().isEmpty()),
                () -> assertEquals(1, page.total())
        );
    }

    @Test
    void listFailureWhileFetchingANoteIsThrown() {
        connection
                .replyList("customerNote.list", 1, row("id", "7"))
                .fail("customerNote.retrieve", 10, "Unavailable");

        MainframeException e = assertThrows(MainframeException.class, () -> gateway.list(CUSTOMER, 1, 8));

        assertTrue(e.isNetworkFailure());
    }

    @Test
    void countAsksForOneRowAndReturnsTheTotal() {
        connection.replyList("customerNote.list", 12);

        long count = gateway.count(CUSTOMER);

        assertAll(
                () -> assertEquals(12, count),
                () -> assertEquals("1", connection.onlyRequest("customerNote.list").getValue("size"))
        );
    }

    @Test
    void retrieveMapsTheNote() {
        connection.reply("customerNote.retrieve", row("id", "7", "type", "Contact", "author", "Bob Ross",
                "dateAdded", "2026-10-05", "lines", "Prefers mobile"));

        assertEquals(NOTE, gateway.retrieve(CUSTOMER, 7));
    }

    @Test
    void retrieveWithoutADateGivesANullDate() {
        connection.reply("customerNote.retrieve", row("id", "7", "type", "Contact", "lines", "Hi"));

        assertNull(gateway.retrieve(CUSTOMER, 7).dateAdded());
    }

    @Test
    void retrieveWithATypeLavsDoesNotModelIsUnknown() {
        connection.reply("customerNote.retrieve", row("id", "7", "type", "Test note", "lines", "Hi"));

        assertEquals(CustomerNote.Type.UNKNOWN, gateway.retrieve(CUSTOMER, 7).type());
    }

    @Test
    void retrieveWithAnUnreadableDateIsReportedAsUnreadable() {
        connection.reply("customerNote.retrieve", row("id", "7", "type", "Contact", "dateAdded", "05/10/2026"));

        assertUnreadable(assertThrows(MainframeException.class, () -> gateway.retrieve(CUSTOMER, 7)));
    }

    @Test
    void addSendsTheNoteThenReadsItBack() {
        connection
                .reply("customer.addNote", row("id", "12", "author", "Bob Ross"))
                .reply("customerNote.retrieve", row("id", "12", "type", "Contact", "author", "Bob Ross",
                        "dateAdded", "2026-10-05", "lines", "Prefers mobile"));

        CustomerNote added = gateway.add(CUSTOMER, DETAILS);

        MessageRequest request = connection.onlyRequest("customer.addNote");
        assertAll(
                () -> assertEquals(new CustomerNote(12, CUSTOMER, CustomerNote.Type.CONTACT, "Prefers mobile",
                        "Bob Ross", LocalDate.of(2026, 10, 5)), added),
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("Contact", request.getValue("type")),
                () -> assertEquals("Prefers mobile", request.getValue("lines")),
                () -> assertEquals(1, connection.requests("customerNote.retrieve").size())
        );
    }

    @Test
    void addFailureWhileReadingBackIsThrown() {
        connection
                .reply("customer.addNote", row("id", "12", "author", "Bob Ross"))
                .fail("customerNote.retrieve", 10, "Unavailable");

        MainframeException e = assertThrows(MainframeException.class, () -> gateway.add(CUSTOMER, DETAILS));

        assertTrue(e.isNetworkFailure());
    }

    @Test
    void updateSendsTheIdAndTheNoteThenReadsItBack() {
        connection
                .reply("customer.updateNote", row("id", "7", "author", "Bob Ross"))
                .reply("customerNote.retrieve", row("id", "7", "type", "Contact", "author", "Bob Ross",
                        "dateAdded", "2026-10-05", "lines", "Prefers mobile"));

        CustomerNote updated = gateway.update(CUSTOMER, 7, DETAILS);

        MessageRequest request = connection.onlyRequest("customer.updateNote");
        assertAll(
                () -> assertEquals(NOTE, updated),
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("7", request.getValue("id")),
                () -> assertEquals("Contact", request.getValue("type")),
                () -> assertEquals("Prefers mobile", request.getValue("lines"))
        );
    }

    @Test
    void deleteSendsTheCustomerAndId() {
        connection.reply("customer.deleteNote", row("id", "7"));

        gateway.delete(CUSTOMER, 7);

        MessageRequest request = connection.onlyRequest("customer.deleteNote");
        assertAll(
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("7", request.getValue("id"))
        );
    }

    @Test
    void mainframeErrorsPassThrough() {
        connection.fail("customerNote.retrieve", 3, "Note not found");

        MainframeException e = assertThrows(MainframeException.class, () -> gateway.retrieve(CUSTOMER, 7));

        assertEquals("Note not found", e.getMessage());
    }

    @Test
    void requiresAnAdapter() {
        assertThrows(NullPointerException.class, () -> new CustomerNoteGateway(null));
    }

    @Test
    void listKeepsTheMainframeOrder() {
        connection
                .replyList("customerNote.list", 2, row("id", "7"), row("id", "8"))
                .reply("customerNote.retrieve", row("id", "7", "type", "Contact", "lines", "First"))
                .reply("customerNote.retrieve", row("id", "8", "type", "General", "lines", "Second"));

        Page<CustomerNote> page = gateway.list(CUSTOMER, 1, 8);

        assertAll(
                () -> assertEquals(List.of(7L, 8L), page.items().stream().map(CustomerNote::id).toList()),
                () -> assertEquals(List.of("First", "Second"), page.items().stream().map(CustomerNote::lines).toList()),
                () -> assertEquals(2, connection.requests("customerNote.retrieve").size())
        );
    }

    @Test
    void retrieveReadsTheDateFromATimestamp() {
        connection.reply("customerNote.retrieve", row("id", "7", "type", "Contact", "author", "Bob Ross",
                "dateAdded", "2026-10-05T12:13:36.4120000+13:00", "lines", "Prefers mobile"));

        assertEquals(LocalDate.of(2026, 10, 5), gateway.retrieve(CUSTOMER, 7).dateAdded());
    }

    @Test
    void retrieveIgnoresSpacesAroundTheDate() {
        connection.reply("customerNote.retrieve", row("id", "7", "type", "Contact", "dateAdded", " 2026-10-05 ",
                "lines", "Hi"));

        assertEquals(LocalDate.of(2026, 10, 5), gateway.retrieve(CUSTOMER, 7).dateAdded());
    }

    // These two rely on MainframeAdapter.send reporting parse failures inside a message as UNABLE_TO_PARSE
    @Test
    void listResponseWithoutAnItemCountIsReportedAsUnreadable() {
        connection.reply("customerNote.list", row("total", "1"));

        assertUnreadable(assertThrows(MainframeException.class, () -> gateway.list(CUSTOMER, 1, 8)));
    }

    @Test
    void listItemWithoutAnIdIsReportedAsUnreadable() {
        connection.replyList("customerNote.list", 1, row("type", "Contact"));

        assertUnreadable(assertThrows(MainframeException.class, () -> gateway.list(CUSTOMER, 1, 8)));
    }
}