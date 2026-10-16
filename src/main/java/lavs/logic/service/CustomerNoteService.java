package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lavs.logic.adapter.CustomerNoteGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNote;
import lavs.logic.model.customer.CustomerNumber;

public class CustomerNoteService {

    private final CustomerNoteGateway notes;

    public CustomerNoteService(CustomerNoteGateway notes) {
        this.notes = Objects.requireNonNull(notes, "notes");
    }

    public Result<Page<CustomerNote>> list(CustomerNumber customer, long page, long size) {
        return Result.attempt(() -> Result.success(notes.list(customer, page, size)));
    }

    public Result<Long> count(CustomerNumber customer) {
        return Result.attempt(() -> Result.success(notes.count(customer)));
    }

    public Result<CustomerNote> get(CustomerNumber customer, long id) {
        return Result.attempt(() -> Result.success(notes.retrieve(customer, id)));
    }

    public Result<CustomerNote> add(CustomerNumber customer, CustomerNote.Details details) {
        return save(details, () -> notes.add(customer, details));
    }

    public Result<CustomerNote> update(CustomerNumber customer, long id, CustomerNote.Details details) {
        return save(details, () -> notes.update(customer, id, details));
    }

    public Result<Void> delete(CustomerNumber customer, long id) {
        return Result.attempt(() -> {
            notes.delete(customer, id);
            return Result.success(null);
        });
    }

    private Result<CustomerNote> save(CustomerNote.Details details, Supplier<CustomerNote> send) {
        List<FieldError> errors = details.validate();
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> Result.success(send.get()));
    }
}