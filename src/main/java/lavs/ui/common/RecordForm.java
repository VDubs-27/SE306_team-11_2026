package lavs.ui.common;

/**
 * A form for one record, such as an address or a note, that can be shown in a pop-up to add or
 * edit it. Each form is written once and reused wherever that kind of record is edited.
 * AI-assisted: written by Jack, refined with Claude.
 *
 * @param <T> what the form edits, e.g. Address.Details
 */
public interface RecordForm<T> {

    /**
     * Empties the form for a new record. first is true when it will be the only one of its kind,
     * so forms with a primary choice start with it ticked; other forms ignore it.
     */
    void clear(boolean first);

    /** Puts an existing record into the form for editing. */
    void fill(T record);

    /** Checks what has been entered, showing any problems in place. Returns true when it can be saved. */
    boolean check();

    /** What the form holds now. */
    T read();

    /** The form's error messages, so problems found when saving (e.g. by a service) show under their fields. */
    FieldErrors fieldErrors();
}
