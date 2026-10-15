package lavs.logic.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import lavs.logic.model.FieldError;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;

final class Rules {

    static final long NEW_RECORD = 0;

    private Rules() {
    }

    // Adds a type error when the mainframe no longer offers the chosen type, unless the type already has one
    static <E extends Enum<E>> List<FieldError> withTypeCheck(
            List<FieldError> errors, Constants constants, ConstantCategory<E> category, E type, String label) {
        boolean typeChecked = errors.stream().anyMatch(error -> error.field().equals("type"));
        if (typeChecked || constants.allows(category, type)) {
            return errors;
        }
        List<FieldError> withType = new ArrayList<>(errors);
        withType.add(new FieldError("type", label + " must be one of the listed options."));
        return withType;
    }

    static <S> boolean otherIsPrimary(List<S> existing, long id, Predicate<S> isPrimary, ToLongFunction<S> idOf) {
        return existing.stream().anyMatch(item -> isPrimary.test(item) && idOf.applyAsLong(item) != id);
    }

    static FieldError primaryTaken(String label) {
        return new FieldError("isPrimary", "This customer already has a primary " + label + ".");
    }
}
