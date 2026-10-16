package lavs.logic.model.note;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import lavs.logic.model.FieldError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NoteTests {

    @Test
    void missingTypeThrows() {
        assertThrows(NullPointerException.class, () -> new Note(1, null, "Hi", "Bob", null));
    }

    @Test
    void detailsCopiesTheEditableFields() {
        Note note = new Note(1, Note.Type.CONTACT, "Hi", "Bob", null);

        assertEquals(new Note.Details(Note.Type.CONTACT, "Hi"), note.details());
    }

    @ParameterizedTest
    @EnumSource(value = Note.Type.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
    void everyTypeIsFoundByItsCode(Note.Type type) {
        assertEquals(type, Note.Type.fromCode(type.code()));
    }

    @Test
    void codeLookupIgnoresCase() {
        assertEquals(Note.Type.CONTACT, Note.Type.fromCode("CONTACT"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Test note", "UNKNOWN"})
    void unrecognisedCodeIsUnknown(String code) {
        assertEquals(Note.Type.UNKNOWN, Note.Type.fromCode(code));
    }

    @Test
    void validDetailsHaveNoErrors() {
        assertEquals(List.of(), new Note.Details(Note.Type.CONTACT, "Hi").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankNoteIsRejected(String lines) {
        assertEquals(List.of(new FieldError("lines", "Note is required.")),
                new Note.Details(Note.Type.CONTACT, lines).validate());
    }

    @Test
    void missingTypeIsRejected() {
        assertEquals(List.of(new FieldError("type", "Note type is required.")),
                new Note.Details(null, "Hi").validate());
    }

    @Test
    void unknownTypeIsRejected() {
        assertEquals(List.of(new FieldError("type", "Note type must be one of the listed options.")),
                new Note.Details(Note.Type.UNKNOWN, "Hi").validate());
    }
}