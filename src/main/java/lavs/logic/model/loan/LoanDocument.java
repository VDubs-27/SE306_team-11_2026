package lavs.logic.model.loan;

import java.util.List;
import java.util.Objects;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

public record LoanDocument(
    long id,
    LoanNumber loanNumber,
    String type,
    String fileName,
    String fileType,
    long size
) {
    public LoanDocument {
        Objects.requireNonNull(loanNumber, "loanNumber");
    }

    public record Summary(
        long id,
        LoanNumber loanNumber,
        String type,
        String fileName
    ) {
        public Summary {
            Objects.requireNonNull(loanNumber, "loanNumber");
        }
    }

    public record Details(
        String type,
        String fileName,
        String fileType,
        long size
    ) {
        public List<FieldError> validate() {
            return new Validator()
                .required("type", type, "Document type")
                .required("fileName", fileName, "File name")
                .required("fileType", fileType, "File type")
                .check(size < 0, "size", "File size must be 0 or more.")
                .errors();
        }
    }
}
