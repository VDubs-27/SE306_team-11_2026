package lavs.logic.model.customer;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.Codes;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

// dateAdded is null on the result of an add or update, because the mainframe does not return it there
public record CustomerNote(
        long id,
        CustomerNumber customerNumber,
        Type type,
        String lines,
        String author,
        LocalDate dateAdded
) {
    public CustomerNote {
        Objects.requireNonNull(customerNumber, "customerNumber");
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