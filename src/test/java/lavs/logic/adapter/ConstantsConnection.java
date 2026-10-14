package lavs.logic.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mainframe.client.MessageRequest;
import mainframe.client.MessageResponse;
import mainframe.client.Status;
import mainframe.client.StubConnection;

// Answers the constant category and item list messages, one page at a time, like the mainframe does
public class ConstantsConnection extends StubConnection {

    private static final String CATEGORY_LIST = "constantCategory.list";
    private static final String ITEM_LIST = "constantItem.list";

    private final Map<String, List<Item>> categories = new LinkedHashMap<>();
    private final List<MessageRequest> constantRequests = new ArrayList<>();
    private final Map<String, MessageResponse> failures = new HashMap<>();
    private Integer pageSize;
    private Long reportedCategoryTotal;

    // Items are given in mainframe order, with their order numbers counting up from 1
    public ConstantsConnection category(String name, String... itemNames) {
        List<Item> items = new ArrayList<>();
        for (String itemName : itemNames) {
            items.add(new Item(itemName, items.size() + 1));
        }
        categories.put(name, items);
        return this;
    }

    public ConstantsConnection item(String category, String name, long order) {
        categories.computeIfAbsent(category, key -> new ArrayList<>()).add(new Item(name, order));
        return this;
    }

    // Overrides the page size the client asks for, like a server that caps it
    public ConstantsConnection pageSize(int size) {
        pageSize = size;
        return this;
    }

    public ConstantsConnection reportCategoryTotal(long total) {
        reportedCategoryTotal = total;
        return this;
    }

    public ConstantsConnection failCategoryList(int errorCode, String message) {
        failures.put(CATEGORY_LIST, failure(errorCode, message));
        return this;
    }

    public ConstantsConnection failItemList(int errorCode, String message) {
        failures.put(ITEM_LIST, failure(errorCode, message));
        return this;
    }

    public List<MessageRequest> constantRequests() {
        return constantRequests;
    }

    @Override
    public MessageResponse send(MessageRequest request) {
        String type = request.getRequestType();
        if (!CATEGORY_LIST.equals(type) && !ITEM_LIST.equals(type)) {
            return super.send(request);
        }
        constantRequests.add(request);
        if (failures.containsKey(type)) {
            return failures.get(type);
        }
        return CATEGORY_LIST.equals(type) ? categoryPage(request) : itemPage(request);
    }

    private MessageResponse categoryPage(MessageRequest request) {
        List<String> names = new ArrayList<>(categories.keySet());
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            rows.add(Map.of("id", String.valueOf(i + 1), "name", names.get(i)));
        }
        long total = reportedCategoryTotal == null ? rows.size() : reportedCategoryTotal;
        return page(request, rows, total);
    }

    private MessageResponse itemPage(MessageRequest request) {
        int index = Integer.parseInt(request.getValue("category")) - 1;
        List<Map<String, String>> rows = new ArrayList<>();
        for (Item item : new ArrayList<>(categories.values()).get(index)) {
            rows.add(Map.of("name", item.name(), "order", String.valueOf(item.order())));
        }
        return page(request, rows, rows.size());
    }

    private MessageResponse page(MessageRequest request, List<Map<String, String>> rows, long total) {
        int page = Integer.parseInt(request.getValue("page"));
        int size = pageSize == null ? Integer.parseInt(request.getValue("size")) : pageSize;
        List<Map<String, String>> pageRows = rows.subList(
                Math.min((page - 1) * size, rows.size()), Math.min(page * size, rows.size()));

        HashMap<String, String> values = new HashMap<>();
        values.put("page", String.valueOf(page));
        values.put("total", String.valueOf(total));
        values.put("items", String.valueOf(pageRows.size()));
        for (int i = 0; i < pageRows.size(); i++) {
            for (Map.Entry<String, String> field : pageRows.get(i).entrySet()) {
                values.put((i + 1) + ":" + field.getKey(), field.getValue());
            }
        }
        return new MessageResponse(new Status(1), values);
    }

    private static MessageResponse failure(int errorCode, String message) {
        return new MessageResponse(new Status(errorCode, message, 1), new HashMap<>());
    }

    private record Item(String name, long order) {
    }
}
