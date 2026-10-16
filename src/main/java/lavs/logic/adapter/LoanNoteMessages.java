package lavs.logic.adapter;

import java.util.Arrays;
import java.util.Objects;
import lavs.logic.model.loan.LoanNumber;
import mainframe.client.messages.loan.AddNote;
import mainframe.client.messages.loan.DeleteNote;
import mainframe.client.messages.loan.UpdateNote;
import mainframe.client.messages.loanNote.List;
import mainframe.client.messages.loanNote.Retrieve;

final class LoanNoteMessages implements NoteMessages<LoanNumber> {

    private final MainframeAdapter adapter;

    LoanNoteMessages(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    @Override
    public Listed list(LoanNumber loan, long page, long size) {
        List message = adapter.send(new List(loan.value(), page, size));
        return new Listed(Arrays.stream(message.getItems()).map(List.Item::getId).toList(), message.getTotal());
    }

    @Override
    public Fields retrieve(LoanNumber loan, long id) {
        Retrieve message = adapter.send(new Retrieve(id, loan.value()));
        return new Fields(message.getType(), message.getLines(), message.getAuthor(), message.getDateAdded());
    }

    @Override
    public long add(LoanNumber loan, String type, String lines) {
        return adapter.send(new AddNote(lines, loan.value(), type)).getId();
    }

    @Override
    public void update(LoanNumber loan, long id, String type, String lines) {
        adapter.send(new UpdateNote(id, lines, loan.value(), type));
    }

    @Override
    public void delete(LoanNumber loan, long id) {
        adapter.send(new DeleteNote(id, loan.value()));
    }
}
