package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Employer;
import mainframe.client.MessageRequest;
import org.junit.jupiter.api.Test;

class EmployerGatewayTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final Employer.Details DETAILS =
            new Employer.Details(Employer.Type.PRIMARY, "Countrywide", "Teller", new BigDecimal("65000.50"));

    private final ScriptedConnection connection = new ScriptedConnection();
    private final EmployerGateway gateway = new EmployerGateway(new MainframeAdapter(connection));

    @Test
    void listReturnsOnePageOfSummaries() {
        connection.replyList("customerEmployer.list", 2,
                row("id", "4", "type", "Primary", "name", "Countrywide"),
                row("id", "5", "type", "Secondary", "name", "Cafe"));

        Page<Employer.Summary> page = gateway.list(CUSTOMER, 1, 10);

        assertAll(
                () -> assertEquals(List.of(
                        new Employer.Summary(4, CUSTOMER, Employer.Type.PRIMARY, "Countrywide"),
                        new Employer.Summary(5, CUSTOMER, Employer.Type.SECONDARY, "Cafe")), page.items()),
                () -> assertEquals(2, page.total()),
                () -> assertEquals("01-0013", connection.onlyRequest("customerEmployer.list").getValue("number"))
        );
    }

    @Test
    void retrieveReturnsTheFullEmployer() {
        connection.reply("customerEmployer.retrieve", row(
                "id", "4", "type", "Primary", "name", "Countrywide", "position", "Teller", "salary", "65000.5"));

        Employer employer = gateway.retrieve(CUSTOMER, 4);

        assertEquals(new Employer(4, CUSTOMER, Employer.Type.PRIMARY, "Countrywide", "Teller",
                new BigDecimal("65000.5")), employer);
    }

    @Test
    void addSendsEveryFieldAndReturnsTheNewEmployer() {
        connection.reply("customer.addEmployer", row("id", "12", "salary", "65000.5"));

        Employer employer = gateway.add(CUSTOMER, DETAILS);

        MessageRequest request = connection.onlyRequest("customer.addEmployer");
        assertAll(
                () -> assertEquals(12, employer.id()),
                () -> assertEquals(DETAILS, employer.details()),
                () -> assertEquals("01-0013", request.getValue("customer")),
                () -> assertEquals("Primary", request.getValue("type")),
                () -> assertEquals("Countrywide", request.getValue("name")),
                () -> assertEquals("Teller", request.getValue("position")),
                () -> assertEquals("65000.5", request.getValue("salary"))
        );
    }

    @Test
    void missingSalaryIsSentAsZero() {
        connection.reply("customer.addEmployer", row("id", "12", "salary", "65000.5"));

        gateway.add(CUSTOMER, new Employer.Details(Employer.Type.SECONDARY, "Cafe", null, null));

        assertEquals("0.0", connection.onlyRequest("customer.addEmployer").getValue("salary"));
    }

    @Test
    void updateSendsTheIdAndReturnsTheUpdatedEmployer() {
        connection.reply("customer.updateEmployer", row("id", "12", "salary", "65000.5"));

        Employer employer = gateway.update(CUSTOMER, 12, DETAILS);

        assertAll(
                () -> assertEquals(12, employer.id()),
                () -> assertEquals("12", connection.onlyRequest("customer.updateEmployer").getValue("id"))
        );
    }

    @Test
    void deleteSendsTheCustomerAndId() {
        connection.reply("customer.deleteEmployer", row("id", "12"));

        gateway.delete(CUSTOMER, 12);

        assertEquals("12", connection.onlyRequest("customer.deleteEmployer").getValue("id"));
    }

    @Test
    void requiresAdapter() {
        assertThrows(NullPointerException.class, () -> new EmployerGateway(null));
    }
}
