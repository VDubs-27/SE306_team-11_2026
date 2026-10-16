package lavs.logic.model.document;

import java.util.List;
import lavs.logic.model.FieldError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DocumentTests {

    @Test
    void detailsCopiesEveryEditableField() {
        Document document = new Document(42, "Proof of Identity", "passport.pdf", "application/pdf", 123);

        Assertions.assertEquals(
                new Document.Details("Proof of Identity", "passport.pdf", "application/pdf", 123),
                document.details());
    }

    @Nested
    class DetailsValidation {

        @ParameterizedTest
        @ValueSource(longs = {0, 1, 5_000_000})
        void completeDetailsAreValid(long size) {
            Document.Details details =
                    new Document.Details("Proof of Identity", "passport.pdf", "application/pdf", size);

            Assertions.assertTrue(details.validate().isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void blankFieldsAreRequired(String blank) {
            Document.Details details = new Document.Details(blank, blank, blank, 123);

            Assertions.assertEquals(List.of(
                    new FieldError("type", "Document type is required."),
                    new FieldError("fileName", "File name is required."),
                    new FieldError("fileType", "File type is required.")), details.validate());
        }

        @Test
        void negativeSizeIsRejected() {
            Document.Details details =
                    new Document.Details("Proof of Identity", "passport.pdf", "application/pdf", -1);

            Assertions.assertEquals(List.of(new FieldError("size", "File size must be 0 or more.")),
                    details.validate());
        }
    }
}
