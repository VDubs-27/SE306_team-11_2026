package lavs.ui.common;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.scene.Node;

import lavs.logic.model.FieldError;
import lavs.logic.service.Result;

/**
 * Adds or edits one kind of record in a pop-up: fills the form, shows it with Cancel and Save (and
 * Remove when editing), and only closes once saving works. What saving does is up to the screen, so
 * the same editor serves a wizard's draft, a loan's lists and the services.
 * <p>
 * With a service (addAndSave, editAndSave) the save is tried while the pop-up is still open. If it
 * fails, everything entered stays: a problem with a field shows under that field, and anything else,
 * such as the mainframe being unreachable, shows in a banner at the top of the pop-up.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 *
 * @param <T> the record the form edits
 */
public final class RecordEditor<T> {

    private static final String SAVE = "save";
    private static final String REMOVE = "remove";

    private final Node owner;
    private final String name;
    private final Node formView;
    private final RecordForm<T> form;
    private final boolean removable;

    /**
     * An editor whose pop-ups are titled "Add name" and "Edit name", e.g. name "address".
     * owner is any node on the screen, so the pop-up knows which window to cover.
     */
    public RecordEditor(Node owner, String name, Node formView, RecordForm<T> form) {
        this(owner, name, formView, form, true);
    }

    private RecordEditor(Node owner, String name, Node formView, RecordForm<T> form, boolean removable) {
        this.owner = owner;
        this.name = name;
        this.formView = formView;
        this.form = form;
        this.removable = removable;
    }

    /** The same editor without Remove, for a record that can be changed but not removed. */
    public RecordEditor<T> withoutRemove() {
        return new RecordEditor<>(owner, name, formView, form, false);
    }

    /** Opens an empty form (see RecordForm.clear for first); when it's saved, passes on the new record. */
    public void add(boolean first, Consumer<T> onSave) {
        addAndSave(first, record -> done(() -> onSave.accept(record)));
    }

    /** Opens the form filled with the record; passes on the changed record when saved, or runs onRemove. */
    public void edit(T record, Consumer<T> onSave, Runnable onRemove) {
        editAndSave(record, changed -> done(() -> onSave.accept(changed)), () -> done(onRemove));
    }

    /**
     * Opens an empty form and, on Save, saves the new record with save (e.g. a service call). The
     * pop-up closes only when that succeeds; otherwise it shows what went wrong and stays open.
     */
    public void addAndSave(boolean first, Function<T, Result<?>> save) {
        form.clear(first);
        Modal modal = new Modal(owner, "Add " + name).content(formView);
        modal.button("cancel", "Cancel", Modal.Kind.SECONDARY)
                .button(SAVE, "Save", Modal.Kind.PRIMARY, () -> form.check() && tried(modal, save.apply(form.read())));
        modal.showAndWait();
    }

    /**
     * Opens the form filled with the record. On Save, saves the change with save; on Remove, removes
     * it with remove. Either closes the pop-up only when it succeeds, as for addAndSave.
     */
    public void editAndSave(T record, Function<T, Result<?>> save, Supplier<Result<?>> remove) {
        form.fill(record);
        Modal modal = new Modal(owner, "Edit " + name).content(formView);
        if (removable) {
            modal.button(REMOVE, "Remove", Modal.Kind.DANGER, () -> tried(modal, remove.get()));
        }
        modal.button("cancel", "Cancel", Modal.Kind.SECONDARY)
                .button(SAVE, "Save", Modal.Kind.PRIMARY, () -> form.check() && tried(modal, save.apply(form.read())));
        modal.showAndWait();
    }

    /**
     * True when the save worked. Otherwise shows why: field problems under their fields, and the
     * rest (a field the form doesn't have, or a failure such as no connection) in the banner.
     */
    private boolean tried(Modal modal, Result<?> result) {
        if (result.successful()) {
            return true;
        }
        FieldErrors fieldErrors = form.fieldErrors();
        fieldErrors.show(result.fieldErrors());
        List<String> others = fieldErrors.unplaced(result.fieldErrors()).stream()
                .map(FieldError::message)
                .collect(Collectors.toList());
        if (result.errorMessage() != null) {
            others.add(result.errorMessage());
        }
        modal.showError(others.isEmpty() ? null : String.join("\n", others));
        return false;
    }

    /** A save that only changes what's on screen, so it always works. */
    private static Result<Void> done(Runnable change) {
        change.run();
        return Result.success(null);
    }
}
