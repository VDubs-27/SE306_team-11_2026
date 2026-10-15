package lavs.ui.common;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import lavs.logic.model.FieldError;

/**
 * The form for one document, used in a pop-up to add or edit: a document type and a file chosen
 * from the computer. Only the file's name, type and size are kept, within the mainframe's limits.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DocumentFormController {

    /** Temporary choices; replaced by the mainframe's category list once there is a service to read it. */
    private static final List<String> TYPES = List.of(
            "Proof of income", "Identification", "Proof of address", "Bank statement", "KiwiSaver", "Other");

    /** Lengths the mainframe accepts for a document's type and file name. */
    private static final int TYPE_MAX = 20;
    private static final int FILE_NAME_MAX = 50;

    /** File types for common extensions, for when the computer can't tell. */
    private static final Map<String, String> KNOWN_TYPES = Map.of(
            "pdf", "application/pdf", "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png",
            "doc", "application/msword", "txt", "text/plain");

    @FXML private ComboBox<String> typeBox;
    @FXML private Button chooseButton;
    @FXML private Label fileNameLabel;
    @FXML private Label fileDetailsLabel;
    @FXML private Label typeError;
    @FXML private Label fileError;

    private FieldErrors errors;
    private String fileName;
    private String fileType;
    private long size;

    @FXML
    private void initialize() {
        typeBox.getItems().setAll(TYPES);
        errors = new FieldErrors()
                .add("type", typeBox, typeError)
                .add("file", chooseButton, fileError);
    }

    /** Empties the form for a new document. */
    public void clear() {
        fill(new DocumentFile(null, null, null, 0));
    }

    /** Puts an existing document into the form for editing. */
    public void fill(DocumentFile document) {
        typeBox.setValue(document.type());
        showFile(document.fileName(), document.fileType(), document.size());
        errors.show(List.of());
    }

    /** Checks there is a type and a file, within the mainframe's limits. Returns true when it can be saved. */
    public boolean check() {
        List<FieldError> problems = new ArrayList<>();
        String type = typeBox.getValue();
        if (type == null) {
            problems.add(new FieldError("type", "Document type is required."));
        } else if (type.length() > TYPE_MAX) {
            problems.add(new FieldError("type", "Document type must be " + TYPE_MAX + " characters or fewer."));
        }
        if (fileName == null) {
            problems.add(new FieldError("file", "Choose the file to record."));
        } else if (fileName.length() > FILE_NAME_MAX) {
            problems.add(new FieldError("file", "The file name must be " + FILE_NAME_MAX
                    + " characters or fewer. Rename the file, then choose it again."));
        }
        return errors.show(problems);
    }

    /** What the form holds now. */
    public DocumentFile read() {
        return new DocumentFile(typeBox.getValue(), fileName, fileType, size);
    }

    /** Records a file's name, type and size; used by Choose file, and by tests with a known file. */
    public void useFile(Path file) {
        try {
            showFile(file.getFileName().toString(), fileTypeOf(file), Files.size(file));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        errors.show(List.of());
    }

    @FXML
    private void handleChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a document");
        File chosen = chooser.showOpenDialog(chooseButton.getScene().getWindow());
        if (chosen != null) {
            useFile(chosen.toPath());
        }
    }

    private void showFile(String name, String type, long bytes) {
        fileName = name;
        fileType = type;
        size = bytes;
        fileNameLabel.setText(name == null ? "No file chosen" : name);
        fileDetailsLabel.setText(name == null ? "" : DocumentRows.details(name, type, bytes));
        chooseButton.setText(name == null ? "Choose file…" : "Choose another…");
    }

    /** The file's type, e.g. "application/pdf", from the computer or else from its extension. */
    private static String fileTypeOf(Path file) throws IOException {
        String detected = Files.probeContentType(file);
        if (detected != null) {
            return detected;
        }
        String name = file.getFileName().toString();
        String extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return KNOWN_TYPES.getOrDefault(extension, "application/octet-stream");
    }
}
