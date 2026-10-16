package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import lavs.logic.model.Page;
import lavs.logic.model.loan.LoanNumber;
import lavs.logic.model.note.Note;
import mainframe.client.MessageRequest;
import org.junit.jupiter.api.Test;

// The shared note behaviour is covered by NoteGatewayTests; these check the loan messages are sent with the loan number
class LoanNoteGatewayTests {

    private static final LoanNumber LOAN = new LoanNumber("01-0013-001");
    private static final Note.Details DETAILS = new Note.Details(Note.Type.LOAN, "Valuation booked");
    private static final Note NOTE =
            new Note(7, Note.Type.LOAN, "Valuation booked", "Bob Ross", LocalDate.of(2026, 10, 5));

    private final ScriptedConnection connection = new ScriptedConnection();
    private final NoteGateway<LoanNumber> gateway = NoteGateway.forLoans(new MainframeAdapter(connection));

    private ScriptedConnection replyWithNote(long id) {
        return connection.reply("loanNote.retrieve", row("id", String.valueOf(id), "type", "Loan",
                "author", "Bob Ross", "dateAdded", "2026-10-05", "lines", "Valuation booked"));
    }

    @Test
    void listSendsTheLoanAndFetchesTheTextOfEachNote() {
        connection.replyList("loanNote.list", 1, row("id", "7"));
        replyWithNote(7);

        Page<Note> page = gateway.list(LOAN, 2, 8);

        MessageRequest request = connection.onlyRequest("loanNote.list");
        assertAll(
                () -> assertEquals(List.of(NOTE), page.items()),
                () -> assertEquals(1, page.total()),
                () -> assertEquals("01-0013-001", request.getValue("number")),
                () -> assertEquals("2", request.getValue("page")),
                () -> assertEquals("8", request.getValue("size"))
        );
    }

    @Test
    void retrieveSendsTheLoanAndId() {
        replyWithNote(7);

        Note note = gateway.retrieve(LOAN, 7);

        MessageRequest request = connection.onlyRequest("loanNote.retrieve");
        assertAll(
                () -> assertEquals(NOTE, note),
                () -> assertEquals("01-0013-001", request.getValue("number")),
                () -> assertEquals("7", request.getValue("id"))
        );
    }

    @Test
    void addSendsTheNoteThenReadsItBack() {
        connection.reply("loan.addNote", row("id", "12", "author", "Bob Ross"));
        replyWithNote(12);

        Note added = gateway.add(LOAN, DETAILS);

        MessageRequest request = connection.onlyRequest("loan.addNote");
        assertAll(
                () -> assertEquals(12, added.id()),
                () -> assertEquals("01-0013-001", request.getValue("loanNumber")),
                () -> assertEquals("Loan", request.getValue("type")),
                () -> assertEquals("Valuation booked", request.getValue("lines")),
                () -> assertEquals("12", connection.onlyRequest("loanNote.retrieve").getValue("id"))
        );
    }

    @Test
    void updateSendsTheIdAndTheNoteThenReadsItBack() {
        connection.reply("loan.updateNote", row("id", "7", "author", "Bob Ross"));
        replyWithNote(7);

        Note updated = gateway.update(LOAN, 7, DETAILS);

        MessageRequest request = connection.onlyRequest("loan.updateNote");
        assertAll(
                () -> assertEquals(NOTE, updated),
                () -> assertEquals("01-0013-001", request.getValue("loanNumber")),
                () -> assertEquals("7", request.getValue("id")),
                () -> assertEquals("Loan", request.getValue("type")),
                () -> assertEquals("Valuation booked", request.getValue("lines"))
        );
    }

    @Test
    void deleteSendsTheLoanAndId() {
        connection.reply("loan.deleteNote", row("id", "7"));

        gateway.delete(LOAN, 7);

        MessageRequest request = connection.onlyRequest("loan.deleteNote");
        assertAll(
                () -> assertEquals("01-0013-001", request.getValue("loanNumber")),
                () -> assertEquals("7", request.getValue("id"))
        );
    }

    @Test
    void countAsksForOneRowAndReturnsTheTotal() {
        connection.replyList("loanNote.list", 4);

        assertEquals(4, gateway.count(LOAN));
    }

    @Test
    void requiresAnAdapter() {
        assertThrows(NullPointerException.class, () -> NoteGateway.forLoans(null));
    }
}
