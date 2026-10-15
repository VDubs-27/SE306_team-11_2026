package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import mainframe.client.MessageRequest;
import mainframe.client.StatusMessage;
import org.junit.jupiter.api.Test;

class LoanGatewayTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final LoanNumber LOAN_NUMBER = new LoanNumber("01-0013-001");
    private static final Loan.Details DETAILS = new Loan.Details(
            new BigDecimal("250000.0"), Loan.RateType.FIXED, new BigDecimal("5.5"), Loan.RateFrequency.YEARLY,
            LocalDate.of(2026, 1, 15), new BigDecimal("1500.0"), Loan.PaymentFrequency.MONTHLY,
            LocalDate.of(2027, 1, 15));
    private static final Loan LOAN = new Loan(
            LOAN_NUMBER, CUSTOMER, Loan.Status.NEW, new BigDecimal("1.0"), Loan.RateType.FLOATING,
            new BigDecimal("1.0"), Loan.RateFrequency.MONTHLY, LocalDate.of(2025, 1, 1), new BigDecimal("1.0"),
            Loan.PaymentFrequency.WEEKLY, LocalDate.of(2025, 6, 1));
    // The provided create, update and retrieve messages fail if the mainframe leaves out an amount
    private static final Map<String, String> AMOUNTS = row(
            "loanNumber", "01-0013-001", "principal", "250000.0", "rateAmount", "5.5", "paymentAmount", "1500.0");

    private final ScriptedConnection connection = new ScriptedConnection();
    private final LoanGateway gateway = new LoanGateway(new MainframeAdapter(connection));

    @Test
    void listReturnsTheCustomersLoans() {
        connection.replyList("loan.list", 3,
                row("loanNumber", "01-0013-001", "principal", "250000.00", "startDate", "2026-01-15", "status", "New"),
                row("loanNumber", "01-0013-002", "principal", "8000", "startDate", "2024-03-01", "status", "Active"));

        Page<Loan.Summary> page = gateway.list(CUSTOMER, 1, 2);

        MessageRequest request = connection.onlyRequest("loan.list");
        assertAll(
                () -> assertEquals(List.of(
                        new Loan.Summary(LOAN_NUMBER, CUSTOMER, Loan.Status.NEW, new BigDecimal("250000.00"),
                                LocalDate.of(2026, 1, 15)),
                        new Loan.Summary(new LoanNumber("01-0013-002"), CUSTOMER, Loan.Status.ACTIVE,
                                new BigDecimal("8000"), LocalDate.of(2024, 3, 1))), page.items()),
                () -> assertEquals(3, page.total()),
                () -> assertEquals("01-0013", request.getValue("number")),
                () -> assertEquals("2", request.getValue("size"))
        );
    }

    @Test
    void listLeavesOutRowsThatCannotBeRead() {
        connection.replyList("loan.list", 3,
                row("loanNumber", "01-0013-001", "startDate", "2026-01-15", "status", "New"),
                row("loanNumber", "01-0013-002", "startDate", "15/01/2026", "status", "New"),
                row("loanNumber", "", "status", "New"));

        Page<Loan.Summary> page = gateway.list(CUSTOMER, 1, 10);

        assertEquals(List.of(LOAN_NUMBER), page.items().stream().map(Loan.Summary::loanNumber).toList());
    }

    @Test
    void retrieveReturnsTheFullLoan() {
        Map<String, String> values = row(
                "customerNumber", "01-0013", "status", "Pending", "rateType", "Fixed", "rateFrequency", "Yearly",
                "startDate", "2026-01-15", "paymentFrequency", "Monthly", "renewalDate", "2027-01-15");
        values.putAll(AMOUNTS);
        connection.reply("loan.retrieve", values);

        Loan loan = gateway.retrieve(LOAN_NUMBER);

        assertAll(
                () -> assertEquals(new Loan(
                        LOAN_NUMBER, CUSTOMER, Loan.Status.PENDING, new BigDecimal("250000.0"), Loan.RateType.FIXED,
                        new BigDecimal("5.5"), Loan.RateFrequency.YEARLY, LocalDate.of(2026, 1, 15),
                        new BigDecimal("1500.0"), Loan.PaymentFrequency.MONTHLY, LocalDate.of(2027, 1, 15)), loan),
                () -> assertEquals("01-0013-001", connection.onlyRequest("loan.retrieve").getValue("number"))
        );
    }

    @Test
    void loanThatCannotBeReadIsAMainframeError() {
        Map<String, String> values = row("customerNumber", "", "status", "New");
        values.putAll(AMOUNTS);
        connection.reply("loan.retrieve", values);

        MainframeException e = assertThrows(MainframeException.class, () -> gateway.retrieve(LOAN_NUMBER));

        assertAll(
                () -> assertEquals(StatusMessage.UNABLE_TO_PARSE.getCode(), e.errorCode()),
                () -> assertEquals("The mainframe returned a loan record that could not be read.", e.getMessage())
        );
    }

    @Test
    void createSendsEveryFieldAndReturnsTheNewLoanNumber() {
        connection.reply("loan.create", AMOUNTS);

        LoanNumber created = gateway.create(CUSTOMER, DETAILS);

        MessageRequest request = connection.onlyRequest("loan.create");
        assertAll(
                () -> assertEquals(LOAN_NUMBER, created),
                () -> assertEquals("01-0013", request.getValue("customerNumber")),
                () -> assertEquals("250000.0", request.getValue("principal")),
                () -> assertEquals("Fixed", request.getValue("rateType")),
                () -> assertEquals("5.5", request.getValue("rateAmount")),
                () -> assertEquals("Yearly", request.getValue("rateFrequency")),
                () -> assertEquals("2026-01-15", request.getValue("startDate")),
                () -> assertEquals("1500.0", request.getValue("paymentAmount")),
                () -> assertEquals("Monthly", request.getValue("paymentFrequency")),
                () -> assertEquals("2027-01-15", request.getValue("renewalDate"))
        );
    }

    @Test
    void updateSendsTheNewDetailsAndKeepsTheLoansStatus() {
        // What the live mainframe returns: no customer or loan number, and New whatever the loan's status is
        Map<String, String> response = row(
                "status", "New", "rateType", "Fixed", "rateFrequency", "Yearly", "startDate", "2026-01-15",
                "paymentFrequency", "Monthly", "renewalDate", "2027-01-15", "principal", "250000.0",
                "rateAmount", "5.5", "paymentAmount", "1500.0");
        connection.reply("loan.update", response);
        Loan pending = LOAN.withStatus(Loan.Status.PENDING);

        Loan updated = gateway.update(pending, DETAILS);

        MessageRequest request = connection.onlyRequest("loan.update");
        assertAll(
                () -> assertEquals(DETAILS, updated.details()),
                () -> assertEquals(Loan.Status.PENDING, updated.status()),
                () -> assertEquals(LOAN_NUMBER, updated.loanNumber()),
                () -> assertEquals(CUSTOMER, updated.customerNumber()),
                () -> assertEquals("01-0013-001", request.getValue("loanNumber")),
                () -> assertEquals("01-0013", request.getValue("customerNumber")),
                () -> assertEquals("Fixed", request.getValue("rateType")),
                () -> assertEquals("2027-01-15", request.getValue("renewalDate"))
        );
    }

    @Test
    void updateStatusSendsTheStatusCode() {
        connection.reply("loan.updateStatus", row("loanNumber", "01-0013-001", "status", "Pending"));

        gateway.updateStatus(LOAN_NUMBER, Loan.Status.PENDING);

        MessageRequest request = connection.onlyRequest("loan.updateStatus");
        assertAll(
                () -> assertEquals("01-0013-001", request.getValue("loanNumber")),
                () -> assertEquals("Pending", request.getValue("status"))
        );
    }

    @Test
    void requiresAdapter() {
        assertThrows(NullPointerException.class, () -> new LoanGateway(null));
    }
}
