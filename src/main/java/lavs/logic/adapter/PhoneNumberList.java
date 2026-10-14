package lavs.logic.adapter;

import java.util.ArrayList;
import java.util.List;
import mainframe.client.Connection;
import mainframe.client.Message;
import mainframe.client.MessageRequest;
import mainframe.client.MessageResponse;
import mainframe.client.Status;

// Replaces the provided customerPhoneNumber.List, whose items all report the last item's phone number
// because its Item constructor writes the number to a field of the outer class
final class PhoneNumberList implements Message {

    private final String customerNumber;
    private final long page;
    private final long size;
    private final List<Item> items = new ArrayList<>();
    private long total;

    PhoneNumberList(String customerNumber, long page, long size) {
        this.customerNumber = customerNumber;
        this.page = page;
        this.size = size;
    }

    @Override
    public Status send(Connection connection) {
        MessageRequest request = new MessageRequest(MessageRequest.QUERY, "customerPhoneNumber.list");
        request.setValue("number", customerNumber);
        request.setValue("page", Long.toString(page));
        request.setValue("size", Long.toString(size));
        MessageResponse response = connection.send(request);
        if (response.getStatus().getWasSuccessful()) {
            String totalValue = response.getValue("total");
            total = totalValue == null ? 0 : Long.parseLong(totalValue);
            long count = Long.parseLong(response.getValue("items"));
            for (int i = 1; i <= count; i++) {
                items.add(new Item(
                        Long.parseLong(response.getValue(i + ":id")), response.getValue(i + ":type"),
                        response.getValue(i + ":countryCode"), response.getValue(i + ":number"),
                        response.getValue(i + ":isPrimary"), response.getValue(i + ":canTxt")));
            }
        }
        return response.getStatus();
    }

    Item[] getItems() {
        return items.toArray(new Item[0]);
    }

    Long getTotal() {
        return total;
    }

    record Item(long id, String type, String countryCode, String number, String isPrimary, String canTxt) {
    }
}
