package lavs.logic.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import mainframe.client.Message;
import mainframe.client.MessageResponse;
import mainframe.client.Status;
import mainframe.client.StatusMessage;
import mainframe.client.StubConnection;
import mainframe.client.messages.customer.Create;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class MainframeAdapterTest {
    private StubConnection stub;
    private MainframeAdapter adapter;

    @BeforeEach
    void setUp() {
        stub = new StubConnection();
        adapter = new MainframeAdapter(stub);
    }

    private Create newCreateMessage() {
        return new Create("2000-01-01", "Smith", "John", "New Zealand", 0L, "Mr");
    }

    @Test
    void constructorRejectsNullConnection() {
        assertThrows(NullPointerException.class, () -> new MainframeAdapter(null));
    }

    @Test
    void sendReturnsMessageWithResponseValuesOnSuccess() {
        HashMap<String, String> values = new HashMap<>();
        values.put("number", "12345");
        stub.setResponse(new MessageResponse(new Status(1), values));

        Create result = adapter.send(newCreateMessage());

        assertEquals("12345", result.getNumber());
    }

    @Test
    void sendThrowsWithMainframeMessageOnServerError() {
        stub.setResponse(new MessageResponse(new Status(3, "Invalid customer", 1), new HashMap<>()));

        MainframeException e = assertThrows(MainframeException.class,
                () -> adapter.send(newCreateMessage()));

        assertEquals(3, e.errorCode());
        assertEquals("Invalid customer", e.getMessage());
        assertTrue(e.isServerError());
        assertFalse(e.isNetworkFailure());
    }

    @Test
    void sendUsesFriendlyMessageOnNetworkFailure() {
        stub.setResponse(StatusMessage.NETWORK_FAILURE_UNAVAILABLE.generateMessageResponse(1));

        MainframeException e = assertThrows(MainframeException.class,
                () -> adapter.send(newCreateMessage()));

        assertTrue(e.isNetworkFailure());
        assertTrue(e.getMessage().contains("Could not reach the mainframe"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void sendUsesGenericMessageWhenMainframeMessageIsBlank(String blank) {
        stub.setResponse(new MessageResponse(new Status(3, blank, 1), new HashMap<>()));

        MainframeException e = assertThrows(MainframeException.class,
                () -> adapter.send(newCreateMessage()));

        assertEquals("The mainframe rejected the request.", e.getMessage());
    }

    @Test
    void sendTreatsIoExceptionAsNetworkFailure() {
        Message failing = connection -> { throw new IOException("socket closed"); };

        MainframeException e = assertThrows(MainframeException.class,
                () -> adapter.send(failing));

        assertTrue(e.isNetworkFailure());
        assertInstanceOf(IOException.class, e.getCause());
    }
}