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
import lavs.logic.model.customer.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EmailServiceTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final Constants CONSTANTS = new Constants(Map.of(ConstantCategory.EMAIL_TYPE, List.of("Personal", "Work")));

    private final ScriptedConnection connection = new ScriptedConnection();
    private final MainframeAdapter adapter = new MainframeAdapter(connection);
    private final EmailService service =
            new EmailService(new PhoneGateway(adapter), new EmailGateway(adapter), () -> CONSTANTS);

    private static Email.Details details(Email.Type type, boolean isPrimary) {
        return new Email.Details(type, null, "jo@example.com", isPrimary);
    }

    @Test
    void listReturnsThePage() {
        connection.replyList("customerEmail.list", 1, row("id", "4", "type", "Work", "isPrimary", "y"));

        Result<Page<Email.Summary>> result = service.list(CUSTOMER, 1, 10);

        assertEquals(1, result.value().items().size());
    }

    @Test
    void getReturnsTheEmail() {
        connection.reply("customerEmail.retrieve", row("id", "4", "type", "Work", "address", "jo@example.com"));

        Result<Email> result = service.get(CUSTOMER, 4);

        assertEquals("jo@example.com", result.value().address());
    }

    @Test
    void validEmailIsAdded() {
        connection.reply("customer.addEmail", row("id", "12"));

        Result<Email> result = service.add(CUSTOMER, details(Email.Type.WORK, false));

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(12, result.value().id())
        );
    }

    @Test
    void invalidDetailsAreRejectedWithoutCallingTheMainframe() {
        Result<Email> result = service.add(CUSTOMER, new Email.Details(Email.Type.WORK, null, "not-an-email", false));

        assertAll(
                () -> assertEquals(List.of(new FieldError("address", "Email address must be in the format name@example.com.")), result.fieldErrors()),
                () -> assertTrue(connection.requests("customer.addEmail").isEmpty())
        );
    }

    @Test
    void typeTheMainframeDoesNotOfferIsRejected() {
        Email.Details other = new Email.Details(
                Email.Type.OTHER, "Holiday", "jo@example.com", false);

        Result<Email> result = service.add(CUSTOMER, other);

        assertEquals(List.of(new FieldError("type", "Email type must be one of the listed options.")),
                result.fieldErrors());
    }

    @Test
    void secondPrimaryIsRejected() {
        connection.replyList("customerEmail.list", 1, row("id", "4", "type", "Work", "isPrimary", "y"));

        Result<Email> result = service.add(CUSTOMER, details(Email.Type.WORK, true));

        assertAll(
                () -> assertEquals(List.of(new FieldError("isPrimary", "This customer already has a primary email address.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requests("customer.addEmail").isEmpty())
        );
    }

    @Test
    void primaryCanBeUpdatedAndStayPrimary() {
        connection
                .replyList("customerEmail.list", 1, row("id", "4", "type", "Work", "isPrimary", "y"))
                .reply("customer.updateEmail", row("id", "4"));

        Result<Email> result = service.update(CUSTOMER, 4, details(Email.Type.WORK, true));

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("4", connection.onlyRequest("customer.updateEmail").getValue("id"))
        );
    }

    @ParameterizedTest
    @CsvSource({"2, 0", "1, 1", "0, 2"})
    void deleteIsAllowedWhileAnotherContactRemains(long phones, long emails) {
        connection
                .replyList("customerPhoneNumber.list", phones)
                .replyList("customerEmail.list", emails)
                .reply("customer.deleteEmail", row("id", "4"));

        Result<Void> result = service.delete(CUSTOMER, 4);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("4", connection.onlyRequest("customer.deleteEmail").getValue("id"))
        );
    }

    @Test
    void lastContactCannotBeDeleted() {
        connection
                .replyList("customerPhoneNumber.list", 0)
                .replyList("customerEmail.list", 1);

        Result<Void> result = service.delete(CUSTOMER, 4);

        assertAll(
                () -> assertEquals(ContactRequirement.LAST_CONTACT_MESSAGE, result.errorMessage()),
                () -> assertTrue(connection.requests("customer.deleteEmail").isEmpty())
        );
    }

    @Test
    void failureWhileCheckingContactsIsReported() {
        connection.fail("customerPhoneNumber.list", 10, "Unavailable");

        Result<Void> result = service.delete(CUSTOMER, 4);

        assertAll(
                () -> assertEquals("Could not reach the mainframe. Check the connection and try again.",
                        result.errorMessage()),
                () -> assertTrue(connection.requests("customer.deleteEmail").isEmpty())
        );
    }

    @Test
    void requiresGatewaysAndConstants() {
        PhoneGateway phones = new PhoneGateway(adapter);
        EmailGateway emails = new EmailGateway(adapter);

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new EmailService(null, emails, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class, () -> new EmailService(phones, null, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class, () -> new EmailService(phones, emails, null))
        );
    }
}
