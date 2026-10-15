package lavs.logic.service;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import lavs.logic.adapter.EmailGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.PhoneGateway;
import lavs.logic.adapter.ScriptedConnection;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Phone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhoneServiceTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final Constants CONSTANTS = new Constants(Map.of(ConstantCategory.PHONE_TYPE, List.of("Home", "Mobile", "Work")));

    private final ScriptedConnection connection = new ScriptedConnection();
    private final MainframeAdapter adapter = new MainframeAdapter(connection);
    private final PhoneService service =
            new PhoneService(new PhoneGateway(adapter), new EmailGateway(adapter), () -> CONSTANTS);

    private static Phone.Details details(Phone.Type type, boolean isPrimary) {
        return new Phone.Details(type, null, "+64", "21 555 0101", isPrimary, true);
    }

    @Test
    void listReturnsThePage() {
        connection.replyList("customerPhoneNumber.list", 1, row("id", "4", "type", "Mobile", "isPrimary", "y"));

        Result<Page<Phone.Summary>> result = service.list(CUSTOMER, 1, 10);

        assertEquals(1, result.value().items().size());
    }

    @Test
    void getReturnsThePhone() {
        connection.reply("customerPhoneNumber.retrieve", row("id", "4", "type", "Mobile", "number", "21 555 0101"));

        Result<Phone> result = service.get(CUSTOMER, 4);

        assertEquals("21 555 0101", result.value().number());
    }

    @Test
    void validPhoneIsAdded() {
        connection.reply("customer.addPhoneNumber", row("id", "12"));

        Result<Phone> result = service.add(CUSTOMER, details(Phone.Type.MOBILE, false));

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(12, result.value().id())
        );
    }

    @Test
    void invalidDetailsAreRejectedWithoutCallingTheMainframe() {
        Result<Phone> result = service.add(CUSTOMER, new Phone.Details(Phone.Type.MOBILE, null, "+64", " ", false, false));

        assertAll(
                () -> assertEquals(List.of(new FieldError("number", "Phone number is required.")), result.fieldErrors()),
                () -> assertTrue(connection.requests("customer.addPhoneNumber").isEmpty())
        );
    }

    @Test
    void typeTheMainframeDoesNotOfferIsRejected() {
        Phone.Details other = new Phone.Details(
                Phone.Type.OTHER, "Holiday", "+64", "21 555 0101", false, true);

        Result<Phone> result = service.add(CUSTOMER, other);

        assertEquals(List.of(new FieldError("type", "Phone type must be one of the listed options.")),
                result.fieldErrors());
    }

    @Test
    void secondPrimaryIsRejected() {
        connection.replyList("customerPhoneNumber.list", 1, row("id", "4", "type", "Mobile", "isPrimary", "y"));

        Result<Phone> result = service.add(CUSTOMER, details(Phone.Type.MOBILE, true));

        assertAll(
                () -> assertEquals(List.of(new FieldError("isPrimary", "This customer already has a primary phone number.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requests("customer.addPhoneNumber").isEmpty())
        );
    }

    @Test
    void primaryCanBeUpdatedAndStayPrimary() {
        connection
                .replyList("customerPhoneNumber.list", 1, row("id", "4", "type", "Mobile", "isPrimary", "y"))
                .reply("customer.updatePhoneNumber", row("id", "4"));

        Result<Phone> result = service.update(CUSTOMER, 4, details(Phone.Type.MOBILE, true));

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("4", connection.onlyRequest("customer.updatePhoneNumber").getValue("id"))
        );
    }

    @ParameterizedTest
    @CsvSource({"2, 0", "1, 1", "0, 2"})
    void deleteIsAllowedWhileAnotherContactRemains(long phones, long emails) {
        connection
                .replyList("customerPhoneNumber.list", phones)
                .replyList("customerEmail.list", emails)
                .reply("customer.deletePhoneNumber", row("id", "4"));

        Result<Void> result = service.delete(CUSTOMER, 4);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("4", connection.onlyRequest("customer.deletePhoneNumber").getValue("id"))
        );
    }

    @Test
    void lastContactCannotBeDeleted() {
        connection
                .replyList("customerPhoneNumber.list", 1)
                .replyList("customerEmail.list", 0);

        Result<Void> result = service.delete(CUSTOMER, 4);

        assertAll(
                () -> assertEquals(ContactRequirement.LAST_CONTACT_MESSAGE, result.errorMessage()),
                () -> assertTrue(connection.requests("customer.deletePhoneNumber").isEmpty())
        );
    }

    @Test
    void failureWhileCheckingContactsIsReported() {
        connection.fail("customerPhoneNumber.list", 10, "Unavailable");

        Result<Void> result = service.delete(CUSTOMER, 4);

        assertAll(
                () -> assertEquals("Could not reach the mainframe. Check the connection and try again.",
                        result.errorMessage()),
                () -> assertTrue(connection.requests("customer.deletePhoneNumber").isEmpty())
        );
    }

    @Test
    void requiresGatewaysAndConstants() {
        PhoneGateway phones = new PhoneGateway(adapter);
        EmailGateway emails = new EmailGateway(adapter);

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new PhoneService(null, emails, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class, () -> new PhoneService(phones, null, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class, () -> new PhoneService(phones, emails, null))
        );
    }
}
