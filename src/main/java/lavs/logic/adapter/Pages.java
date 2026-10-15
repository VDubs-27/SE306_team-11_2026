package lavs.logic.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;
import mainframe.client.Message;

final class Pages {

    // The mainframe returns at most 50 items a page, whatever size is asked for
    static final long SIZE = 50;

    private Pages() {
    }

    static <M extends Message, T> List<T> readAll(
            MainframeAdapter adapter, LongFunction<M> pageRequest, Function<M, T[]> itemsOf,
            Function<M, Long> totalOf) {
        List<T> all = new ArrayList<>();
        for (long page = 1; ; page++) {
            M message = adapter.send(pageRequest.apply(page));
            T[] items = itemsOf.apply(message);
            all.addAll(Arrays.asList(items));
            if (items.length == 0 || all.size() >= totalOf.apply(message)) {
                return all;
            }
        }
    }
}
