package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import mainframe.client.MessageRequest;
import mainframe.client.Status;
import org.junit.jupiter.api.Test;

class PhoneNumberListTests {

    private final ScriptedConnection connection = new ScriptedConnection();

    @Test
    void eachItemKeepsItsOwnNumber() {
        connection.replyList("customerPhoneNumber.list", 2,
                row("id", "4", "type", "Mobile", "countryCode", "+64", "number", "21 555 0101",
                        "isPrimary", "y", "canTxt", "y"),
                row("id", "5", "type", "Home", "countryCode", "+61", "number", "2 5550 1234",
                        "isPrimary", "n", "canTxt", "n"));
        PhoneNumberList message = new PhoneNumberList("01-0013", 3, 20);

        message.send(connection);

        MessageRequest request = connection.onlyRequest("customerPhoneNumber.list");
        assertAll(
                () -> assertEquals(new PhoneNumberList.Item(4, "Mobile", "+64", "21 555 0101", "y", "y"),
                        message.getItems()[0]),
                () -> assertEquals(new PhoneNumberList.Item(5, "Home", "+61", "2 5550 1234", "n", "n"),
                        message.getItems()[1]),
                () -> assertEquals(2, message.getTotal()),
                () -> assertEquals("01-0013", request.getValue("number")),
                () -> assertEquals("3", request.getValue("page")),
                () -> assertEquals("20", request.getValue("size"))
        );
    }

    @Test
    void missingTotalIsZero() {
        connection.reply("customerPhoneNumber.list", row("items", "0"));
        PhoneNumberList message = new PhoneNumberList("01-0013", 1, 20);

        message.send(connection);

        assertEquals(0, message.getTotal());
    }

    @Test
    void failedListHasNoItems() {
        connection.fail("customerPhoneNumber.list", 3, "Unknown customer");
        PhoneNumberList message = new PhoneNumberList("01-0013", 1, 20);

        Status status = message.send(connection);

        assertAll(
                () -> assertEquals(3, status.getErrorCode()),
                () -> assertEquals(0, message.getItems().length)
        );
    }
}
