package lavs.logic.model.customer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.Codes;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

public record Employer(
    long id,
    CustomerNumber customerNumber,
    Type type,
    String name,
    String position,
    BigDecimal salary
) {
    public Employer {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }

    public Details details() {
        return new Details(type, name, position, salary);
    }

    public enum Type {
        PRIMARY("Primary"),
        SECONDARY("Secondary"),
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
        CustomerNumber customerNumber,
        Type type,
        String name
    ) {
        public Summary {
            Objects.requireNonNull(customerNumber, "customerNumber");
            Objects.requireNonNull(type, "type");
        }
    }

    public record Details(
        Type type,
        String name,
        String position,
        BigDecimal salary
    ) {
        public List<FieldError> validate() {
            return new Validator()
                .required("type", type, "Employer type")
                .check(type == Type.UNKNOWN, "type", "Employer type must be one of the listed options.")
                .required("name", name, "Employer name")
                .errors();
        }
    }
}
