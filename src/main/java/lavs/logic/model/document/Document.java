package lavs.logic.model.document;

import java.util.List;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

// A document on a customer or a loan; whose document it is comes from the request, so it is not stored here
public record Document(
    long id,
    String type,
    String fileName,
    String fileType,
    long size
) {
    public Details details() {
        return new Details(type, fileName, fileType, size);
    }

    public record Summary(
        long id,
        String type,
        String fileName
    ) {
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
