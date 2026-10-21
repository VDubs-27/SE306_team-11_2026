package lavs.ui.common;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import javafx.application.Platform;
import javafx.scene.control.DatePicker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DateInputTests {

    private static final LocalDate DATE = LocalDate.of(1990, 3, 5);

    // Controls need JavaFX running; pom.xml runs it headless for tests
    @BeforeAll
    static void startJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException alreadyStarted) {
            // another test class started it
        }
    }

    private final DatePicker picker = new DatePicker();
    private final DateInput input = new DateInput(picker);

    @Test
    void aPickedDateIsRead() {
        picker.setValue(DATE);

        assertAll(
                () -> assertEquals(DATE, input.value()),
                () -> assertFalse(input.isBlank()),
                () -> assertTrue(input.isValid())
        );
    }

    @Test
    void setShowsTheDate() {
        input.set(DATE);

        assertAll(
                () -> assertEquals(DATE, input.value()),
                () -> assertEquals("05/03/1990", picker.getEditor().getText())
        );
    }

    // A form reopened for a record with no date must not keep a date picked before it was cancelled
    @Test
    void setNullForgetsAnEarlierDate() {
        picker.setValue(DATE);

        input.set(null);

        assertAll(
                () -> assertNull(input.value()),
                () -> assertTrue(input.isBlank()),
                () -> assertEquals("", picker.getEditor().getText())
        );
    }

    @Test
    void setNullClearsTextLeftInTheField() {
        picker.getEditor().setText("09/13/1978");

        input.set(null);

        assertEquals("", picker.getEditor().getText());
    }

    // The picker clears itself when it can't read typed text, which must not lose the entry
    @Test
    void thePickerClearingItselfKeepsTheEntry() {
        picker.setValue(DATE);

        picker.setValue(null);

        assertEquals(DATE, input.value());
    }
}
