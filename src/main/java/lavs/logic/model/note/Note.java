package lavs.logic.model.note;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.Codes;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

// A note on a customer or a loan; whose note it is comes from the request, so it is not stored here.
// dateAdded is null when the mainframe does not send one
public record Note(
        long id,
        Type type,
        String lines,
        String author,
        LocalDate dateAdded
) {
    public Note {
        Objects.requireNonNull(type, "type");
    }

    public Details details() {
        return new Details(type, lines);
    }

    public enum Type {
        GENERAL("General"),
        APPLICATION("Application"),
        CONTACT("Contact"),
        IDENTIFICATION("Identification"),
        LOAN("Loan"),
        UNKNOWN("");

        private final String code;

        Type(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static Type fromCode(String code) {
            return Codes.fromCode(values(), Type::code, code, UNKNOWN);
        }
    }

    public record Details(Type type, String lines) {

        public List<FieldError> validate() {
            return new Validator()
                    .required("type", type, "Note type")
                    .check(type == Type.UNKNOWN, "type", "Note type must be one of the listed options.")
                    .required("lines", lines, "Note")
                    .errors();
        }
    }
}
