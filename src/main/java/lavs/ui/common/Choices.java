package lavs.ui.common;

import java.util.Arrays;
import java.util.function.Function;

import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 * Fills a drop-down from one of the model's type lists (such as Address.Type), showing each
 * option by its name and leaving out the "unknown" placeholder.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Choices {

    private Choices() {
    }

    public static <E extends Enum<E>> void fill(ComboBox<E> box, E[] values, E unknown, Function<E, String> name) {
        box.getItems().setAll(Arrays.stream(values).filter(value -> value != unknown).toList());
        box.setConverter(new StringConverter<>() {
            @Override
            public String toString(E value) {
                return value == null ? "" : name.apply(value);
            }

            @Override
            public E fromString(String text) {
                return box.getItems().stream().filter(value -> name.apply(value).equals(text)).findFirst().orElse(null);
            }
        });
    }
}
