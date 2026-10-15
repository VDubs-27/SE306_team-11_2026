package lavs.logic.service;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lavs.logic.adapter.EmployerGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.ScriptedConnection;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Employer;
import org.junit.jupiter.api.Test;

class EmployerServiceTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final Constants CONSTANTS =
            new Constants(Map.of(ConstantCategory.EMPLOYER_TYPE, List.of("Primary")));
    private static final Employer.Details DETAILS =
            new Employer.Details(Employer.Type.PRIMARY, "Countrywide", "Teller", new BigDecimal("65000"));

    private final ScriptedConnection connection = new ScriptedConnection();
    private final EmployerService service =
            new EmployerService(new EmployerGateway(new MainframeAdapter(connection)), () -> CONSTANTS);

    @Test
    void listReturnsThePage() {
        connection.replyList("customerEmployer.list", 1, row("id", "4", "type", "Primary", "name", "Countrywide"));

        Result<Page<Employer.Summary>> result = service.list(CUSTOMER, 1, 10);

        assertEquals("Countrywide", result.value().items().get(0).name());
    }

    @Test
    void getReturnsTheEmployer() {
        connection.reply("customerEmployer.retrieve", row("id", "4", "type", "Primary", "name", "Countrywide",
                "salary", "65000"));

        assertEquals("Countrywide", service.get(CUSTOMER, 4).value().name());
    }

    @Test
    void validEmployerIsAdded() {
        connection.reply("customer.addEmployer", row("id", "12", "salary", "65000"));

        Result<Employer> result = service.add(CUSTOMER, DETAILS);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(12, result.value().id())
        );
    }

    @Test
    void validEmployerIsUpdated() {
        connection.reply("customer.updateEmployer", row("id", "4", "salary", "65000"));

        Result<Employer> result = service.update(CUSTOMER, 4, DETAILS);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("4", connection.onlyRequest("customer.updateEmployer").getValue("id"))
        );
    }

    @Test
    void invalidDetailsAreRejectedWithoutCallingTheMainframe() {
        Result<Employer> result = service.add(CUSTOMER, new Employer.Details(Employer.Type.PRIMARY, "", null, null));

        assertAll(
                () -> assertEquals(List.of(new FieldError("name", "Employer name is required.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requests("customer.addEmployer").isEmpty())
        );
    }

    @Test
    void typeTheMainframeDoesNotOfferIsRejected() {
        Result<Employer> result = service.add(
                CUSTOMER, new Employer.Details(Employer.Type.SECONDARY, "Cafe", null, null));

        assertEquals(List.of(new FieldError("type", "Employer type must be one of the listed options.")),
                result.fieldErrors());
    }

    @Test
    void mainframeRejectionOfASaveIsReported() {
        connection.fail("customer.addEmployer", 3, "Customer is archived");

        assertEquals("Customer is archived", service.add(CUSTOMER, DETAILS).errorMessage());
    }

    @Test
    void deleteRemovesTheEmployer() {
        connection.reply("customer.deleteEmployer", row("id", "4"));

        Result<Void> result = service.delete(CUSTOMER, 4);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("4", connection.onlyRequest("customer.deleteEmployer").getValue("id"))
        );
    }

    @Test
    void requiresGatewayAndConstants() {
        EmployerGateway gateway = new EmployerGateway(new MainframeAdapter(connection));

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new EmployerService(null, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class, () -> new EmployerService(gateway, null))
        );
    }
}
