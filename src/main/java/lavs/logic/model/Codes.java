package lavs.logic.model;

import java.util.function.Function;

public final class Codes {

    private Codes() {
    }

    public static <E extends Enum<E>> E fromCode(E[] values, Function<E, String> codeOf, String code, E unknown) {
        for (E value : values) {
            if (value != unknown && codeOf.apply(value).equalsIgnoreCase(code)) {
                return value;
            }
        }
        return unknown;
    }
}
