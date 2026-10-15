package lavs.logic.model.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import lavs.logic.model.FieldError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CustomerNoteTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");

    @Test
    void missingCustomerNumberThrows() {
        assertThrows(NullPointerException.class,
                () -> new CustomerNote(1, null, CustomerNote.Type.GENERAL, "Hi", "Bob", null));
    }

    @Test
    void missingTypeThrows() {
        assertThrows(NullPointerException.class, () -> new CustomerNote(1, CUSTOMER, null, "Hi", "Bob", null));
    }

    @Test
    void detailsCopiesTheEditableFields() {
        CustomerNote note = new CustomerNote(1, CUSTOMER, CustomerNote.Type.CONTACT, "Hi", "Bob", null);

        assertEquals(new CustomerNote.Details(CustomerNote.Type.CONTACT, "Hi"), note.details());
    }

    @ParameterizedTest
    @EnumSource(value = CustomerNote.Type.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
    void everyTypeIsFoundByItsCode(CustomerNote.Type type) {
        assertEquals(type, CustomerNote.Type.fromCode(type.code()));
    }

    @Test
    void codeLookupIgnoresCase() {
        assertEquals(CustomerNote.Type.CONTACT, CustomerNote.Type.fromCode("CONTACT"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Test note", "UNKNOWN"})
    void unrecognisedCodeIsUnknown(String code) {
        assertEquals(CustomerNote.Type.UNKNOWN, CustomerNote.Type.fromCode(code));
    }

    @Test
    void validDetailsHaveNoErrors() {
        assertEquals(List.of(), new CustomerNote.Details(CustomerNote.Type.CONTACT, "Hi").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankNoteIsRejected(String lines) {
        assertEquals(List.of(new FieldError("lines", "Note is required.")),
                new CustomerNote.Details(CustomerNote.Type.CONTACT, lines).validate());
    }

    @Test
    void missingTypeIsRejected() {
        assertEquals(List.of(new FieldError("type", "Note type is required.")),
                new CustomerNote.Details(null, "Hi").validate());
    }

    @Test
    void unknownTypeIsRejected() {
        assertEquals(List.of(new FieldError("type", "Note type must be one of the listed options.")),
                new CustomerNote.Details(CustomerNote.Type.UNKNOWN, "Hi").validate());
    }
}