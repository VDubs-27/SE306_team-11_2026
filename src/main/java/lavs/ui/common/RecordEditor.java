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
 * With a service (addAndSave, editAndSave) the save runs in the background while the pop-up stays
 * open, saying "Saving…". If it fails, everything entered stays: a problem with a field shows under
 * that field, and anything else, such as the mainframe being unreachable, shows in a banner at the
 * top of the pop-up.
 * AI-assisted: written by Jack, refined with Claude.
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
        form.clear(first);
        Modal modal = new Modal(owner, "Add " + name).content(formView);
        modal.button("cancel", "Cancel", Modal.Kind.SECONDARY)
                .button(SAVE, "Save", Modal.Kind.PRIMARY, () -> form.check() && done(() -> onSave.accept(form.read())));
        modal.showAndWait();
    }

    /** Opens the form filled with the record; passes on the changed record when saved, or runs onRemove. */
    public void edit(T record, Consumer<T> onSave, Runnable onRemove) {
        form.fill(record);
        Modal modal = new Modal(owner, "Edit " + name).content(formView);
        if (removable) {
            modal.button(REMOVE, "Remove", Modal.Kind.DANGER, () -> done(onRemove));
        }
        modal.button("cancel", "Cancel", Modal.Kind.SECONDARY)
                .button(SAVE, "Save", Modal.Kind.PRIMARY, () -> form.check() && done(() -> onSave.accept(form.read())));
        modal.showAndWait();
    }

    /**
     * Opens an empty form and, on Save, saves the new record with save (e.g. a service call) in the
     * background. The pop-up closes only when that succeeds; otherwise it shows what went wrong and
     * stays open. Returns true when the record was saved, so the screen can show it.
     */
    public boolean addAndSave(boolean first, Function<T, Result<?>> save) {
        return addAndSave(first, record -> anyResult(save.apply(record)), saved -> { });
    }

    /** As addAndSave, then gives what save returned (e.g. the new record) to onSaved, on the screen's thread. */
    public <R> boolean addAndSave(boolean first, Function<T, Result<R>> save, Consumer<R> onSaved) {
        form.clear(first);
        Modal modal = new Modal(owner, "Add " + name).content(formView);
        modal.button("cancel", "Cancel", Modal.Kind.SECONDARY)
                .button(SAVE, "Save", Modal.Kind.PRIMARY,
                        () -> form.check() && saving(modal, SAVE, "Saving…", entered(save), onSaved));
        return SAVE.equals(modal.showAndWait());
    }

    /**
     * Opens the form filled with the record. On Save, saves the change with save; on Remove, removes
     * it with remove. Either runs in the background and closes the pop-up only when it succeeds, as
     * for addAndSave. Returns true when the record was saved or removed, so the screen can show it.
     */
    public boolean editAndSave(T record, Function<T, Result<?>> save, Supplier<Result<?>> remove) {
        return editAndSave(record, changed -> anyResult(save.apply(changed)), saved -> { }, remove);
    }

    /** As editAndSave, then gives what save returned (e.g. the changed record) to onSaved, on the screen's thread. */
    public <R> boolean editAndSave(T record, Function<T, Result<R>> save, Consumer<R> onSaved,
                                   Supplier<Result<?>> remove) {
        form.fill(record);
        Modal modal = new Modal(owner, "Edit " + name).content(formView);
        if (removable) {
            modal.button(REMOVE, "Remove", Modal.Kind.DANGER,
                    () -> saving(modal, REMOVE, "Removing…", () -> anyResult(remove.get()), removed -> { }));
        }
        modal.button("cancel", "Cancel", Modal.Kind.SECONDARY)
                .button(SAVE, "Save", Modal.Kind.PRIMARY,
                        () -> form.check() && saving(modal, SAVE, "Saving…", entered(save), onSaved));
        String chosen = modal.showAndWait();
        return SAVE.equals(chosen) || REMOVE.equals(chosen);
    }

    /** What Save does with the form as it is when pressed, read now, on the screen's thread. */
    private <R> Supplier<Result<R>> entered(Function<T, Result<R>> save) {
        T entered = form.read();
        return () -> save.apply(entered);
    }

    /**
     * Starts the work in the background with the pop-up locked and the button saying busyText; when it
     * succeeds, closes the pop-up as that button and passes on its value, otherwise shows why. Returns
     * false, so the button itself doesn't close the pop-up while the work is still going.
     */
    private <R> boolean saving(Modal modal, String id, String busyText, Supplier<Result<R>> work, Consumer<R> onDone) {
        modal.showError(null);
        modal.setBusy(id, busyText);
        Background.run(work, result -> {
            modal.clearBusy();
            if (tried(modal, result)) {
                modal.close(id);
                onDone.accept(result.value());
            }
        });
        return false;
    }

    /** The same result, typed so it can be passed back from the background. */
    private static Result<Object> anyResult(Result<?> result) {
        return new Result<>(result.value(), result.fieldErrors(), result.errorMessage());
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
    private static boolean done(Runnable change) {
        change.run();
        return true;
    }
}
