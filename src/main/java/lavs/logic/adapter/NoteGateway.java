package lavs.logic.adapter;

import java.time.format.DateTimeParseException;
import java.util.Optional;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.LoanNumber;
import lavs.logic.model.note.Note;
import mainframe.client.StatusMessage;

// Notes on a customer or a loan; K is the owner's number. forCustomers and forLoans pick the messages to send.
public final class NoteGateway<K> {

    private static final String UNREADABLE_TEXT = "The mainframe returned a note that could not be read.";

    private final NoteMessages<K> messages;

    private NoteGateway(NoteMessages<K> messages) {
        this.messages = messages;
    }

    public static NoteGateway<CustomerNumber> forCustomers(MainframeAdapter adapter) {
        return new NoteGateway<>(new CustomerNoteMessages(adapter));
    }

    public static NoteGateway<LoanNumber> forLoans(MainframeAdapter adapter) {
        return new NoteGateway<>(new LoanNoteMessages(adapter));
    }

    // The list message has no note text, so each note on the page is fetched for its text
    public Page<Note> list(K owner, long page, long size) {
        NoteMessages.Listed listed = messages.list(owner, page, size);
        return new Page<>(
                listed.ids().stream()
                        .map(id -> readable(owner, id))
                        .flatMap(Optional::stream)
                        .toList(),
                page, size, listed.total());
    }

    public long count(K owner) {
        return messages.list(owner, 1, 1).total();
    }

    public Note retrieve(K owner, long id) {
        NoteMessages.Fields fields = messages.retrieve(owner, id);
        try {
            return new Note(
                    id, Note.Type.fromCode(fields.type()), fields.lines(), fields.author(),
                    Values.date(fields.dateAdded()));
        } catch (DateTimeParseException e) {
            throw new MainframeException(StatusMessage.UNABLE_TO_PARSE.getCode(), UNREADABLE_TEXT, e);
        }
    }

    public Note add(K owner, Note.Details details) {
        long id = messages.add(owner, details.type().code(), details.lines());
        return retrieve(owner, id);
    }

    public Note update(K owner, long id, Note.Details details) {
        messages.update(owner, id, details.type().code(), details.lines());
        return retrieve(owner, id);
    }

    public void delete(K owner, long id) {
        messages.delete(owner, id);
    }

    private Optional<Note> readable(K owner, long id) {
        try {
            return Optional.of(retrieve(owner, id));
        } catch (MainframeException e) {
            int unreadable = StatusMessage.UNABLE_TO_PARSE.getCode();
            if (e.errorCode() == unreadable) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
