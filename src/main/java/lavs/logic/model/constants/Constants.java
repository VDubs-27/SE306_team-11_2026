package lavs.logic.model.constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Constants {

    public static final Constants EMPTY = new Constants(Map.of());

    private final Map<ConstantCategory<?>, List<String>> codes;

    public Constants(Map<ConstantCategory<?>, List<String>> codes) {
        Map<ConstantCategory<?>, List<String>> copy = new HashMap<>();
        codes.forEach((category, values) -> copy.put(category, List.copyOf(values)));
        this.codes = Map.copyOf(copy);
    }

    // Values the mainframe offers that LAVS does not model (such as test data) are left out
    public <E extends Enum<E>> List<E> options(ConstantCategory<E> category) {
        return codes.getOrDefault(category, List.of()).stream()
                .map(category::valueOf)
                .flatMap(Optional::stream)
                .distinct()
                .toList();
    }

    public <E extends Enum<E>> boolean allows(ConstantCategory<E> category, E value) {
        return options(category).contains(value);
    }
}
