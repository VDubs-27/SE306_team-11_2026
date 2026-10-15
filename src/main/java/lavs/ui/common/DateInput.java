package lavs.ui.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

/**
 * A date picker that takes typed dates as dd/MM/yyyy. A DatePicker on its own quietly clears text it
 * can't read (such as 09/13/1978), so this keeps what was typed and can say whether it was a real date.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class DateInput {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final DatePicker picker;
    private String typed = "";

    public DateInput(DatePicker picker) {
        this.picker = picker;
        picker.setPromptText("dd/mm/yyyy");
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : FORMAT.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                return parse(text);
            }
        });
        picker.getEditor().textProperty().addListener((obs, oldText, text) -> {
            if (picker.getEditor().isFocused()) {
                typed = text;
            }
        });
        picker.valueProperty().addListener((obs, oldDate, date) -> {
            if (date != null) {
                typed = FORMAT.format(date);
            }
        });
    }

    /** True when nothing has been entered. */
    public boolean isBlank() {
        return typed.isBlank();
    }

    /** True when the entry is blank or a real dd/MM/yyyy date. */
    public boolean isValid() {
        return isBlank() || parse(typed) != null;
    }

    /** The date entered, or null when it is blank or not a real date. */
    public LocalDate value() {
        return parse(typed);
    }

    /** Puts back what the user typed, so they can see and fix a date the picker couldn't read. */
    public void showTyped() {
        picker.getEditor().setText(typed);
    }

    private static LocalDate parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
