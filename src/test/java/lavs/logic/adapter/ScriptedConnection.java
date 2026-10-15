package lavs.logic.adapter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mainframe.client.MessageRequest;
import mainframe.client.MessageResponse;
import mainframe.client.Status;
import mainframe.client.StubConnection;

// Replies to each request type with the responses queued for it, repeating the last one
public class ScriptedConnection extends StubConnection {

    private final Map<String, Deque<MessageResponse>> responses = new HashMap<>();
    private final List<MessageRequest> requests = new ArrayList<>();

    public static Map<String, String> row(String... keysAndValues) {
        Map<String, String> row = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            row.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return row;
    }

    public ScriptedConnection reply(String requestType, Map<String, String> values) {
        return queue(requestType, new MessageResponse(new Status(1), new HashMap<>(values)));
    }

    @SafeVarargs
    public final ScriptedConnection replyList(String requestType, long total, Map<String, String>... rows) {
        HashMap<String, String> values = new HashMap<>();
        values.put("total", String.valueOf(total));
        values.put("items", String.valueOf(rows.length));
        for (int i = 0; i < rows.length; i++) {
            for (Map.Entry<String, String> field : rows[i].entrySet()) {
                values.put((i + 1) + ":" + field.getKey(), field.getValue());
            }
        }
        return queue(requestType, new MessageResponse(new Status(1), values));
    }

    public ScriptedConnection fail(String requestType, int errorCode, String message) {
        return queue(requestType, new MessageResponse(new Status(errorCode, message, 1), new HashMap<>()));
    }

    public List<MessageRequest> requests(String requestType) {
        return requests.stream().filter(request -> requestType.equals(request.getRequestType())).toList();
    }

    public MessageRequest onlyRequest(String requestType) {
        List<MessageRequest> matching = requests(requestType);
        if (matching.size() != 1) {
            throw new AssertionError("Expected one " + requestType + " request but got " + matching.size());
        }
        return matching.get(0);
    }

    @Override
    public MessageResponse send(MessageRequest request) {
        requests.add(request);
        Deque<MessageResponse> queued = responses.get(request.getRequestType());
        if (queued == null) {
            throw new AssertionError("No reply scripted for " + request.getRequestType());
        }
        return queued.size() > 1 ? queued.poll() : queued.peek();
    }

    private ScriptedConnection queue(String requestType, MessageResponse response) {
        responses.computeIfAbsent(requestType, key -> new ArrayDeque<>()).add(response);
        return this;
    }
}
