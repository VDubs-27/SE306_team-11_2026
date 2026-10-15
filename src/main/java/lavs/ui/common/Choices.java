package lavs.ui.common;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;

/**
 * Fills a drop-down with one of the model's type lists (such as Address.Type), showing each option
 * by its name. The options are the mainframe's own list, loaded when the user signs in, in its
 * order; before signing in, or for a list the mainframe doesn't send, every value but "unknown".
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Choices {

    /** The signed-in user's lists from the mainframe; empty when nobody is signed in. */
    private static Constants signedIn = Constants.EMPTY;

    private Choices() {
    }

    /** Uses these lists from now on, e.g. the ones loaded at sign-in; EMPTY after signing out. */
    public static void use(Constants constants) {
        signedIn = constants == null ? Constants.EMPTY : constants;
    }

    /** Fills the drop-down with the options for this list (see options), showing each by name. */
    public static <E extends Enum<E>> void fill(ComboBox<E> box, ConstantCategory<E> category, E[] values, E unknown,
            Function<E, String> name) {
        box.getItems().setAll(options(category, values, unknown));
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

    /** The value to start a drop-down on: wanted if it's an option, otherwise the first option. */
    public static <E> E preferred(ComboBox<E> box, E wanted) {
        if (box.getItems().contains(wanted)) {
            return wanted;
        }
        return box.getItems().isEmpty() ? null : box.getItems().get(0);
    }

    /** The options the mainframe offers for this list, or every value but unknown when it hasn't sent one. */
    public static <E extends Enum<E>> List<E> options(ConstantCategory<E> category, E[] values, E unknown) {
        List<E> offered = signedIn.options(category);
        return offered.isEmpty()
                ? Arrays.stream(values).filter(value -> value != unknown).toList()
                : offered;
    }
}
