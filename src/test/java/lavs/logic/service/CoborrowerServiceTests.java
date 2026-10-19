package lavs.logic.service;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import lavs.logic.adapter.CoborrowerGateway;
import lavs.logic.adapter.LoanGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.ScriptedConnection;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import org.junit.jupiter.api.Test;

class CoborrowerServiceTests {

    private static final LoanNumber LOAN_NUMBER = new LoanNumber("01-0013-0001");
    private static final CustomerNumber BORROWER = new CustomerNumber("01-0013");
    private static final CustomerNumber PERSON = new CustomerNumber("01-0020");
    private static final Loan LOAN = new Loan(
            LOAN_NUMBER, BORROWER, Loan.Status.NEW, null, null, null, null, null, null, null, null);
    private static final Constants CONSTANTS = new Constants(Map.of(
            ConstantCategory.COBORROWER_TYPE, List.of("Coborrower")));

    private final ScriptedConnection connection = new ScriptedConnection();
    private final MainframeAdapter adapter = new MainframeAdapter(connection);
    private final CoborrowerService service =
            new CoborrowerService(new CoborrowerGateway(adapter), new LoanGateway(adapter), () -> CONSTANTS);

    private static Coborrower.Details details(Coborrower.Type type, CustomerNumber person) {
        return new Coborrower.Details(type, person);
    }

    // The provided loan messages fail if the mainframe leaves out an amount
    private ScriptedConnection loanIs(String status) {
        return connection.reply("loan.retrieve", row("loanNumber", "01-0013-0001", "customerNumber", "01-0013",
                "status", status, "principal", "250000.0", "rateType", "Fixed", "rateAmount", "5.5",
                "rateFrequency", "Yearly", "startDate", "2026-01-15", "paymentAmount", "1500.0",
                "paymentFrequency", "Monthly", "renewalDate", "2027-01-15"));
    }

    // The loan is New, PERSON is a customer, and the loan already has the given people
    @SafeVarargs
    private ScriptedConnection loanWith(Map<String, String>... people) {
        return loanIs("New")
                .reply("customer.retrieve", row("number", "01-0020"))
                .replyList("loanCoborrower.list", people.length, people)
                .reply("loan.addCoborrower", row("id", "9"))
                .reply("loanCoborrower.retrieve", row(
                        "id", "9", "type", "Coborrower", "coborrowerNumber", "01-0020", "coborrowerName", "Jo Bloggs"));
    }

    private static final Map<String, String> ORIGINAL = row(
            "id", "9", "type", "Coborrower", "coborrowerNumber", "01-0020", "coborrowerName", "Jo Bloggs");

    private CoborrowerService serviceOffering(String... types) {
        Constants offered = new Constants(Map.of(ConstantCategory.COBORROWER_TYPE, List.of(types)));
        return new CoborrowerService(new CoborrowerGateway(adapter), new LoanGateway(adapter), () -> offered);
    }

    // Co-borrower 9 is being replaced on a New loan; the new person is a customer, and the loan has the given
    // people. The first retrieve reads the original, the second reads back the replacement.
    @SafeVarargs
    private ScriptedConnection replacing(Map<String, String>... people) {
        return loanIs("New")
                .reply("loanCoborrower.retrieve", ORIGINAL)
                .reply("loanCoborrower.retrieve", row(
                        "id", "10", "type", "Guarantor", "coborrowerNumber", "01-0020", "coborrowerName", "Jo Bloggs"))
                .reply("customer.retrieve", row("number", "01-0020"))
                .replyList("loanCoborrower.list", people.length, people)
                .reply("loan.deleteCoborrower", row("id", "9"));
    }

    @Test
    void listReturnsThePage() {
        connection.replyList("loanCoborrower.list", 1, row("id", "9", "type", "Coborrower", "coborrowerNumber", "01-0020"));

        Result<Page<Coborrower.Summary>> result = service.list(LOAN_NUMBER, 1, 10);

        assertEquals(1, result.value().total());
    }

    @Test
    void getReturnsTheCoborrower() {
        connection.reply("loanCoborrower.retrieve", row(
                "id", "9", "type", "Coborrower", "coborrowerNumber", "01-0020", "coborrowerName", "Jo Bloggs"));

        assertEquals("Jo Bloggs", service.get(LOAN_NUMBER, 9).value().coborrowerName());
    }

    @Test
    void newCustomerIsAdded() {
        loanWith(row("id", "3", "type", "Coborrower", "coborrowerNumber", "01-0099"));

        Result<Coborrower> result = service.add(LOAN, details(Coborrower.Type.COBORROWER, PERSON));

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(9, result.value().id()),
                () -> assertEquals("01-0020", connection.onlyRequest("loan.addCoborrower").getValue("coborrowerNumber"))
        );
    }

    @Test
    void invalidDetailsAreRejectedWithoutCallingTheMainframe() {
        Result<Coborrower> result = service.add(LOAN, details(Coborrower.Type.COBORROWER, null));

        assertAll(
                () -> assertEquals(List.of(new FieldError("coborrowerNumber", "Coborrower is required.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requests("customer.retrieve").isEmpty())
        );
    }

    @Test
    void typeTheMainframeDoesNotOfferIsRejected() {
        Result<Coborrower> result = service.add(LOAN, details(Coborrower.Type.GUARANTOR, PERSON));

        assertEquals(List.of(new FieldError("type", "Coborrower type must be one of the listed options.")),
                result.fieldErrors());
    }

    @Test
    void loansOwnCustomerCannotBeACoborrower() {
        loanWith();

        Result<Coborrower> result = service.add(LOAN, details(Coborrower.Type.COBORROWER, BORROWER));

        assertAll(
                () -> assertEquals(List.of(CoborrowerService.IS_BORROWER), result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.addCoborrower").isEmpty())
        );
    }

    @Test
    void unknownCustomerCannotBeACoborrower() {
        loanIs("New").fail("customer.retrieve", 3, "'01-0020' is not a valid customer number");

        Result<Coborrower> result = service.add(LOAN, details(Coborrower.Type.COBORROWER, PERSON));

        assertAll(
                () -> assertEquals(List.of(CoborrowerService.NOT_A_CUSTOMER), result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.addCoborrower").isEmpty())
        );
    }

    @Test
    void customerAlreadyOnTheLoanCannotBeAddedAgain() {
        loanWith(row("id", "3", "type", "Guarantor", "coborrowerNumber", "01-0020"));

        Result<Coborrower> result = service.add(LOAN, details(Coborrower.Type.COBORROWER, PERSON));

        assertAll(
                () -> assertEquals(List.of(CoborrowerService.ALREADY_ON_LOAN), result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.addCoborrower").isEmpty())
        );
    }

    @Test
    void networkFailureDuringTheChecksIsReported() {
        loanIs("New").fail("customer.retrieve", 10, "Unavailable");

        Result<Coborrower> result = service.add(LOAN, details(Coborrower.Type.COBORROWER, PERSON));

        assertEquals("Could not reach the mainframe. Check the connection and try again.", result.errorMessage());
    }

    @Test
    void updateDeletesTheOriginalThenAddsTheReplacement() {
        CoborrowerService both = serviceOffering("Coborrower", "Guarantor");
        replacing(row("id", "9", "type", "Coborrower", "coborrowerNumber", "01-0020"))
                .reply("loan.addCoborrower", row("id", "10"));

        Result<Coborrower> result = both.update(LOAN, 9, details(Coborrower.Type.GUARANTOR, PERSON));

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(10, result.value().id()),
                () -> assertEquals(Coborrower.Type.GUARANTOR, result.value().type()),
                () -> assertEquals(List.of("loanCoborrower.retrieve", "loan.retrieve", "customer.retrieve",
                        "loanCoborrower.list",
                        "loan.deleteCoborrower", "loan.addCoborrower", "loanCoborrower.retrieve"),
                        connection.requestTypes()),
                () -> assertEquals("9", connection.onlyRequest("loan.deleteCoborrower").getValue("id")),
                () -> assertEquals("Guarantor", connection.onlyRequest("loan.addCoborrower").getValue("type"))
        );
    }

    @Test
    void updateWithNoChangesSendsNothing() {
        connection.reply("loanCoborrower.retrieve", ORIGINAL);

        Result<Coborrower> result = service.update(LOAN, 9, details(Coborrower.Type.COBORROWER, PERSON));

        assertAll(
                () -> assertEquals(9, result.value().id()),
                () -> assertEquals(List.of("loanCoborrower.retrieve"), connection.requestTypes())
        );
    }

    @Test
    void updateWithInvalidDetailsSendsNothing() {
        Result<Coborrower> result = service.update(LOAN, 9, details(null, PERSON));

        assertAll(
                () -> assertEquals("type", result.fieldErrors().get(0).field()),
                () -> assertTrue(connection.requestTypes().isEmpty())
        );
    }

    @Test
    void updateCannotMakeTheBorrowerACoborrower() {
        loanIs("New").reply("loanCoborrower.retrieve", ORIGINAL);

        Result<Coborrower> result = service.update(LOAN, 9, details(Coborrower.Type.COBORROWER, BORROWER));

        assertAll(
                () -> assertEquals(List.of(CoborrowerService.IS_BORROWER), result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.deleteCoborrower").isEmpty())
        );
    }

    @Test
    void updateCannotSwapInSomeoneAlreadyOnTheLoan() {
        CustomerNumber other = new CustomerNumber("01-0030");
        replacing(row("id", "9", "type", "Coborrower", "coborrowerNumber", "01-0020"),
                row("id", "4", "type", "Coborrower", "coborrowerNumber", "01-0030"));

        Result<Coborrower> result = service.update(LOAN, 9, details(Coborrower.Type.COBORROWER, other));

        assertAll(
                () -> assertEquals(List.of(CoborrowerService.ALREADY_ON_LOAN), result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.deleteCoborrower").isEmpty())
        );
    }

    @Test
    void failedReplacementPutsTheOriginalBack() {
        replacing(row("id", "9", "type", "Coborrower", "coborrowerNumber", "01-0020"))
                .fail("loan.addCoborrower", 3, "Customer is archived")
                .reply("loan.addCoborrower", row("id", "11"));

        Result<Coborrower> result = service.update(LOAN, 9, details(Coborrower.Type.COBORROWER, new CustomerNumber("01-0030")));

        assertAll(
                () -> assertFalse(result.successful()),
                () -> assertEquals("Customer is archived", result.errorMessage()),
                () -> assertEquals(List.of("01-0030", "01-0020"), connection.requests("loan.addCoborrower").stream()
                        .map(request -> request.getValue("coborrowerNumber")).toList())
        );
    }

    @Test
    void failedReplacementAndRestoreAreReported() {
        replacing(row("id", "9", "type", "Coborrower", "coborrowerNumber", "01-0020"))
                .fail("loan.addCoborrower", 10, "Unavailable");

        Result<Coborrower> result = service.update(LOAN, 9, details(Coborrower.Type.COBORROWER, new CustomerNumber("01-0030")));

        assertEquals(CoborrowerService.NOT_RESTORED_MESSAGE, result.errorMessage());
    }

    @Test
    void deleteRemovesTheCoborrower() {
        loanIs("New").reply("loan.deleteCoborrower", row("id", "9"));

        Result<Void> result = service.delete(LOAN_NUMBER, 9);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("9", connection.onlyRequest("loan.deleteCoborrower").getValue("id"))
        );
    }

    // The screen may still hold the loan as New after it was submitted somewhere else
    @Test
    void noOneIsAddedToALoanThatIsNoLongerNew() {
        loanIs("Pending");

        Result<Coborrower> result = service.add(LOAN, details(Coborrower.Type.COBORROWER, PERSON));

        assertAll(
                () -> assertEquals(List.of(Rules.LOAN_LOCKED), result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.addCoborrower").isEmpty())
        );
    }

    @Test
    void noOneIsChangedOnALoanThatIsNoLongerNew() {
        loanIs("Pending").reply("loanCoborrower.retrieve", ORIGINAL);

        Result<Coborrower> result = serviceOffering("Coborrower", "Guarantor")
                .update(LOAN, 9, details(Coborrower.Type.GUARANTOR, PERSON));

        assertAll(
                () -> assertEquals(List.of(Rules.LOAN_LOCKED), result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.deleteCoborrower").isEmpty())
        );
    }

    @Test
    void noOneIsRemovedFromALoanThatIsNoLongerNew() {
        loanIs("Pending");

        Result<Void> result = service.delete(LOAN_NUMBER, 9);

        assertAll(
                () -> assertEquals(List.of(Rules.LOAN_LOCKED), result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.deleteCoborrower").isEmpty())
        );
    }

    @Test
    void requiresEveryDependency() {
        CoborrowerGateway coborrowers = new CoborrowerGateway(adapter);
        LoanGateway loans = new LoanGateway(adapter);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new CoborrowerService(null, loans, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class,
                        () -> new CoborrowerService(coborrowers, null, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class, () -> new CoborrowerService(coborrowers, loans, null))
        );
    }
}
