package lavs.logic.adapter;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lavs.logic.model.Page;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import lavs.logic.model.loan.ScheduledRepayment;
import mainframe.client.MessageRequest;
import org.junit.jupiter.api.Test;

class RepaymentGatewayTests {

    private static final LoanNumber LOAN_NUMBER = new LoanNumber("01-0013-001");

    private final ScriptedConnection connection = new ScriptedConnection();
    private final RepaymentGateway gateway = new RepaymentGateway(new MainframeAdapter(connection));

    @Test
    void totalsComeFromTheLoanSummary() {
        connection.reply("loanSummary.retrieve", row(
                "totalAmountPaid", "540000.0", "totalInterestPaid", "290000.0", "numberOfPayments", "360",
                "finalPaymentDate", "2056-01-15", "finalPaymentAmount", "1234.56"));

        Loan.Totals totals = gateway.totals(LOAN_NUMBER);

        assertAll(
                () -> assertEquals(new Loan.Totals(LOAN_NUMBER, new BigDecimal("540000.0"),
                        new BigDecimal("290000.0"), 360, LocalDate.of(2056, 1, 15), new BigDecimal("1234.56")), totals),
                () -> assertEquals("01-0013-001", connection.onlyRequest("loanSummary.retrieve").getValue("number"))
        );
    }

    @Test
    void scheduleReturnsOnePageOfRepayments() {
        connection.replyList("loanRepaymentSchedule.list", 360,
                row("paymentNumber", "13", "date", "2027-02-15", "amountPaid", "1500.0", "principalAmount", "400.0",
                        "interestAmount", "1100.0", "amountRemaining", "245000.0"));

        Page<ScheduledRepayment> page = gateway.schedule(LOAN_NUMBER, 2, 12);

        MessageRequest request = connection.onlyRequest("loanRepaymentSchedule.list");
        assertAll(
                () -> assertEquals(List.of(new ScheduledRepayment(LOAN_NUMBER, 13, LocalDate.of(2027, 2, 15),
                        new BigDecimal("1500.0"), new BigDecimal("400.0"), new BigDecimal("1100.0"),
                        new BigDecimal("245000.0"))), page.items()),
                () -> assertEquals(360, page.total()),
                () -> assertEquals(30, page.totalPages()),
                () -> assertEquals("01-0013-001", request.getValue("number")),
                () -> assertEquals("2", request.getValue("page")),
                () -> assertEquals("12", request.getValue("size"))
        );
    }

    @Test
    void scheduleRowThatCannotBeReadIsAMainframeError() {
        connection.replyList("loanRepaymentSchedule.list", 1,
                row("paymentNumber", "1", "date", "15/02/2027", "amountPaid", "1.0", "principalAmount", "1.0",
                        "interestAmount", "1.0", "amountRemaining", "1.0"));

        assertThrows(MainframeException.class, () -> gateway.schedule(LOAN_NUMBER, 1, 12));
    }

    @Test
    void requiresAdapter() {
        assertThrows(NullPointerException.class, () -> new RepaymentGateway(null));
    }
}
