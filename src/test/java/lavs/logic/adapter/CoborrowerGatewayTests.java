package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.LoanNumber;
import mainframe.client.MessageRequest;
import org.junit.jupiter.api.Test;

class CoborrowerGatewayTests {

    private static final LoanNumber LOAN = new LoanNumber("01-0013-0001");
    private static final CustomerNumber PERSON = new CustomerNumber("01-0020");

    private final ScriptedConnection connection = new ScriptedConnection();
    private final CoborrowerGateway gateway = new CoborrowerGateway(new MainframeAdapter(connection));

    @Test
    void listReturnsOnePageOfSummaries() {
        connection.replyList("loanCoborrower.list", 3,
                row("id", "7", "type", "Guarantor", "coborrowerNumber", "01-0020", "coborrowerName", "Jo Bloggs"));

        Page<Coborrower.Summary> page = gateway.list(LOAN, 2, 1);

        MessageRequest request = connection.onlyRequest("loanCoborrower.list");
        assertAll(
                () -> assertEquals(List.of(new Coborrower.Summary(
                        7, LOAN, Coborrower.Type.GUARANTOR, PERSON, "Jo Bloggs")), page.items()),
                () -> assertEquals(3, page.total()),
                () -> assertEquals("01-0013-0001", request.getValue("number")),
                () -> assertEquals("2", request.getValue("page"))
        );
    }

    @Test
    void allReadsEveryPage() {
        connection
                .replyList("loanCoborrower.list", 2, row("id", "1", "type", "Coborrower", "coborrowerNumber", "01-0020"))
                .replyList("loanCoborrower.list", 2, row("id", "2", "type", "Guarantor", "coborrowerNumber", "01-0021"));

        List<Coborrower.Summary> all = gateway.all(LOAN);

        assertEquals(List.of(1L, 2L), all.stream().map(Coborrower.Summary::id).toList());
    }

    @Test
    void retrieveReturnsTheCoborrower() {
        connection.reply("loanCoborrower.retrieve", row(
                "id", "7", "type", "Coborrower", "coborrowerNumber", "01-0020", "coborrowerName", "Jo Bloggs"));

        Coborrower coborrower = gateway.retrieve(LOAN, 7);

        MessageRequest request = connection.onlyRequest("loanCoborrower.retrieve");
        assertAll(
                () -> assertEquals(new Coborrower(7, LOAN, Coborrower.Type.COBORROWER, PERSON, "Jo Bloggs"), coborrower),
                () -> assertEquals("7", request.getValue("id")),
                () -> assertEquals("01-0013-0001", request.getValue("number"))
        );
    }

    @Test
    void addSendsTheDetailsAndReadsBackTheNewCoborrower() {
        connection
                .reply("loan.addCoborrower", row("id", "7"))
                .reply("loanCoborrower.retrieve", row(
                        "id", "7", "type", "Guarantor", "coborrowerNumber", "01-0020", "coborrowerName", "Jo Bloggs"));

        Coborrower coborrower = gateway.add(LOAN, new Coborrower.Details(Coborrower.Type.GUARANTOR, PERSON));

        MessageRequest request = connection.onlyRequest("loan.addCoborrower");
        assertAll(
                () -> assertEquals("Jo Bloggs", coborrower.coborrowerName()),
                () -> assertEquals("01-0013-0001", request.getValue("loanNumber")),
                () -> assertEquals("01-0020", request.getValue("coborrowerNumber")),
                () -> assertEquals("Guarantor", request.getValue("type")),
                () -> assertEquals("7", connection.onlyRequest("loanCoborrower.retrieve").getValue("id"))
        );
    }

    @Test
    void deleteSendsTheLoanAndId() {
        connection.reply("loan.deleteCoborrower", row("id", "7"));

        gateway.delete(LOAN, 7);

        MessageRequest request = connection.onlyRequest("loan.deleteCoborrower");
        assertAll(
                () -> assertEquals("7", request.getValue("id")),
                () -> assertEquals("01-0013-0001", request.getValue("loanNumber"))
        );
    }

    @Test
    void knownCustomerIsACustomer() {
        connection.reply("customer.retrieve", row("number", "01-0020", "firstName", "Jo"));

        assertAll(
                () -> assertTrue(gateway.isCustomer(PERSON)),
                () -> assertEquals("01-0020", connection.onlyRequest("customer.retrieve").getValue("number"))
        );
    }

    @Test
    void customerNumberTheMainframeRejectsIsNotACustomer() {
        connection.fail("customer.retrieve", 3, "'01-0020' is not a valid customer number");

        assertFalse(gateway.isCustomer(PERSON));
    }

    @Test
    void networkFailureDuringTheCustomerCheckIsPassedOn() {
        connection.fail("customer.retrieve", 10, "Unavailable");

        assertThrows(MainframeException.class, () -> gateway.isCustomer(PERSON));
    }

    @Test
    void requiresAdapter() {
        assertThrows(NullPointerException.class, () -> new CoborrowerGateway(null));
    }
}
