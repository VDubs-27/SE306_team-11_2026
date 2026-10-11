package lavs.logic.model.loan;

import java.util.List;
import java.util.Objects;
import lavs.logic.model.Codes;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;
import lavs.logic.model.customer.CustomerNumber;

public record Coborrower(
    long id,
    LoanNumber loanNumber,
    Type type,
    CustomerNumber coborrowerNumber,
    String coborrowerName
) {
    public Coborrower {
        Objects.requireNonNull(loanNumber, "loanNumber");
        Objects.requireNonNull(type, "type");
    }

    public enum Type {
        COBORROWER("Coborrower"),
        GUARANTOR("Guarantor"),
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

    public record Summary(
        long id,
        LoanNumber loanNumber,
        Type type,
        CustomerNumber coborrowerNumber,
        String coborrowerName
    ) {
        public Summary {
            Objects.requireNonNull(loanNumber, "loanNumber");
            Objects.requireNonNull(type, "type");
        }
    }

    public record Details(
        Type type,
        CustomerNumber coborrowerNumber
    ) {
        public List<FieldError> validate() {
            return new Validator()
                .required("type", type, "Coborrower type")
                .check(type == Type.UNKNOWN, "type", "Coborrower type must be one of the listed options.")
                .required("coborrowerNumber", coborrowerNumber, "Coborrower")
                .errors();
        }
    }
}
