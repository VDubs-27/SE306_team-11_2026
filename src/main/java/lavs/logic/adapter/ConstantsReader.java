package lavs.logic.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongFunction;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import mainframe.client.Message;

final class ConstantsReader {

    static final long PAGE_SIZE = 100;

    private final MainframeAdapter adapter;

    ConstantsReader(MainframeAdapter adapter) {
        this.adapter = adapter;
    }

    Constants read() {
        Map<ConstantCategory<?>, List<String>> codes = new HashMap<>();
        for (mainframe.client.messages.constantCategory.List.Item item : readAll(
                page -> new mainframe.client.messages.constantCategory.List(page, PAGE_SIZE),
                mainframe.client.messages.constantCategory.List::getItems,
                mainframe.client.messages.constantCategory.List::getTotal)) {
            ConstantCategory.fromName(item.getName())
                    .ifPresent(category -> codes.put(category, codesIn(item.getId())));
        }
        return new Constants(codes);
    }

    private List<String> codesIn(long categoryId) {
        return readAll(
                page -> new mainframe.client.messages.constantItem.List(categoryId, page, PAGE_SIZE),
                mainframe.client.messages.constantItem.List::getItems,
                mainframe.client.messages.constantItem.List::getTotal).stream()
                .sorted(Comparator.comparing(mainframe.client.messages.constantItem.List.Item::getOrder))
                .map(mainframe.client.messages.constantItem.List.Item::getName)
                .toList();
    }

    private <M extends Message, T> List<T> readAll(
            LongFunction<M> pageRequest, Function<M, T[]> itemsOf, Function<M, Long> totalOf) {
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
