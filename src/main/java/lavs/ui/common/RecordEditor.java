package lavs.ui.common;

import java.util.function.Consumer;

import javafx.scene.Node;

/**
 * Adds or edits one kind of record in a pop-up: fills the form, shows it with Cancel and Save (and
 * Remove when editing), and passes on what the user chose. What saving does is up to the screen,
 * so the same editor serves a wizard's draft, a loan's lists and the profile's services.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 *
 * @param <T> the record the form edits
 */
public final class RecordEditor<T> {

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
        if (FormDialog.show(owner, "Add " + name, formView, form::check, false) == FormDialog.Result.SAVED) {
            onSave.accept(form.read());
        }
    }

    /** Opens the form filled with the record; passes on the changed record when saved, or runs onRemove. */
    public void edit(T record, Consumer<T> onSave, Runnable onRemove) {
        form.fill(record);
        FormDialog.Result result = FormDialog.show(owner, "Edit " + name, formView, form::check, removable);
        if (result == FormDialog.Result.SAVED) {
            onSave.accept(form.read());
        } else if (result == FormDialog.Result.REMOVED) {
            onRemove.run();
        }
    }
}
