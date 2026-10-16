package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lavs.logic.adapter.NoteGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.note.Note;

// Notes on a customer or a loan; K is the owner's number, e.g. new NoteService<>(NoteGateway.forLoans(adapter))
public class NoteService<K> {

    private final NoteGateway<K> notes;

    public NoteService(NoteGateway<K> notes) {
        this.notes = Objects.requireNonNull(notes, "notes");
    }

    public Result<Page<Note>> list(K owner, long page, long size) {
        return Result.attempt(() -> Result.success(notes.list(owner, page, size)));
    }

    public Result<Long> count(K owner) {
        return Result.attempt(() -> Result.success(notes.count(owner)));
    }

    public Result<Note> get(K owner, long id) {
        return Result.attempt(() -> Result.success(notes.retrieve(owner, id)));
    }

    public Result<Note> add(K owner, Note.Details details) {
        return save(details, () -> notes.add(owner, details));
    }

    public Result<Note> update(K owner, long id, Note.Details details) {
        return save(details, () -> notes.update(owner, id, details));
    }

    public Result<Void> delete(K owner, long id) {
        return Result.attempt(() -> {
            notes.delete(owner, id);
            return Result.success(null);
        });
    }

    private Result<Note> save(Note.Details details, Supplier<Note> send) {
        List<FieldError> errors = details.validate();
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> Result.success(send.get()));
    }
}
