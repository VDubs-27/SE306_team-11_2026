package lavs.logic.adapter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;

final class ConstantsReader {

    private final MainframeAdapter adapter;

    ConstantsReader(MainframeAdapter adapter) {
        this.adapter = adapter;
    }

    Constants read() {
        Map<ConstantCategory<?>, List<String>> codes = new HashMap<>();
        for (mainframe.client.messages.constantCategory.List.Item item : Pages.readAll(adapter,
                page -> new mainframe.client.messages.constantCategory.List(page, Pages.SIZE),
                mainframe.client.messages.constantCategory.List::getItems,
                mainframe.client.messages.constantCategory.List::getTotal)) {
            ConstantCategory.fromName(item.getName())
                    .ifPresent(category -> codes.put(category, codesIn(item.getId())));
        }
        return new Constants(codes);
    }

    private List<String> codesIn(long categoryId) {
        return Pages.readAll(adapter,
                page -> new mainframe.client.messages.constantItem.List(categoryId, page, Pages.SIZE),
                mainframe.client.messages.constantItem.List::getItems,
                mainframe.client.messages.constantItem.List::getTotal).stream()
                .sorted(Comparator.comparing(mainframe.client.messages.constantItem.List.Item::getOrder))
                .map(mainframe.client.messages.constantItem.List.Item::getName)
                .toList();
    }
}
