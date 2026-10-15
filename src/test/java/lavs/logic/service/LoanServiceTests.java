package lavs.logic.service;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lavs.logic.adapter.AddressGateway;
import lavs.logic.adapter.CoborrowerGateway;
import lavs.logic.adapter.EmployerGateway;
import lavs.logic.adapter.LoanGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.RepaymentGateway;
import lavs.logic.adapter.ScriptedConnection;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import lavs.logic.model.loan.ScheduledRepayment;
import org.junit.jupiter.api.Test;

class LoanServiceTests {

    private static final CustomerNumber CUSTOMER = new CustomerNumber("01-0013");
    private static final LoanNumber LOAN_NUMBER = new LoanNumber("01-0013-001");
    private static final Constants CONSTANTS = new Constants(Map.of(
            ConstantCategory.RATE_TYPE, List.of("Fixed", "Floating"),
            ConstantCategory.RATE_FREQUENCY, List.of("Yearly", "Monthly"),
            ConstantCategory.PAYMENT_FREQUENCY, List.of("Monthly", "Fortnightly")));
    private static final Loan.Details DETAILS = new Loan.Details(
            new BigDecimal("250000.0"), Loan.RateType.FIXED, new BigDecimal("5.5"), Loan.RateFrequency.YEARLY,
            LocalDate.of(2026, 1, 15), new BigDecimal("1500.0"), Loan.PaymentFrequency.MONTHLY,
            LocalDate.of(2027, 1, 15));
    private static final Loan LOAN = new Loan(
            LOAN_NUMBER, CUSTOMER, Loan.Status.NEW, DETAILS.principal(), DETAILS.rateType(), DETAILS.rateAmount(),
            DETAILS.rateFrequency(), DETAILS.startDate(), DETAILS.paymentAmount(), DETAILS.paymentFrequency(),
            DETAILS.renewalDate());

    private final ScriptedConnection connection = new ScriptedConnection();
    private final MainframeAdapter adapter = new MainframeAdapter(connection);
    private final LoanService service = new LoanService(new LoanGateway(adapter), new RepaymentGateway(adapter),
            new AddressGateway(adapter), new EmployerGateway(adapter), new CoborrowerGateway(adapter), () -> CONSTANTS);

    // The provided loan messages fail if the mainframe leaves out an amount
    private static Map<String, String> loanRow(String status) {
        return row("loanNumber", "01-0013-001", "customerNumber", "01-0013", "status", status,
                "principal", "250000.0", "rateType", "Fixed", "rateAmount", "5.5", "rateFrequency", "Yearly",
                "startDate", "2026-01-15", "paymentAmount", "1500.0", "paymentFrequency", "Monthly",
                "renewalDate", "2027-01-15");
    }

    private static Loan.Details detailsWith(Loan.RateType rateType, BigDecimal principal) {
        return new Loan.Details(principal, rateType, DETAILS.rateAmount(), DETAILS.rateFrequency(),
                DETAILS.startDate(), DETAILS.paymentAmount(), DETAILS.paymentFrequency(), DETAILS.renewalDate());
    }

    // The loan has the given status, and its customer has an employer and the given mailing flag
    private ScriptedConnection loanToSubmit(String status, String isMailing) {
        return connection
                .reply("loan.retrieve", loanRow(status))
                .replyList("customerAddress.list", 1, row("id", "1", "type", "Postal", "isMailing", isMailing))
                .replyList("customerEmployer.list", 1, row("id", "2", "type", "Primary", "name", "Cafe"));
    }

    private void readyToSubmit(String status) {
        loanToSubmit(status, "y").reply("loan.updateStatus", row("loanNumber", "01-0013-001", "status", "Pending"));
    }

    @Test
    void listForCustomerReturnsThePage() {
        connection.replyList("loan.list", 1,
                row("loanNumber", "01-0013-001", "principal", "250000", "startDate", "2026-01-15", "status", "New"));

        Result<Page<Loan.Summary>> result = service.listForCustomer(CUSTOMER, 1, 10);

        assertEquals(List.of(LOAN_NUMBER), result.value().items().stream().map(Loan.Summary::loanNumber).toList());
    }

    @Test
    void retrieveReturnsTheLoan() {
        connection.reply("loan.retrieve", loanRow("New"));

        assertEquals(LOAN, service.retrieve(LOAN_NUMBER).value());
    }

    @Test
    void retrieveFailureIsReported() {
        connection.fail("loan.retrieve", 10, "Unavailable");

        assertEquals("Could not reach the mainframe. Check the connection and try again.",
                service.retrieve(LOAN_NUMBER).errorMessage());
    }

    @Test
    void totalsComeFromTheLoanSummary() {
        connection.reply("loanSummary.retrieve", row(
                "totalAmountPaid", "540000.0", "totalInterestPaid", "290000.0", "numberOfPayments", "12",
                "finalPaymentDate", "2027-01-15", "finalPaymentAmount", "1500.0"));

        assertEquals(12, service.totals(LOAN_NUMBER).value().numberOfPayments());
    }

    @Test
    void scheduleReturnsThePage() {
        connection.replyList("loanRepaymentSchedule.list", 12,
                row("paymentNumber", "1", "date", "2026-02-15", "amountPaid", "1500.0", "principalAmount", "400.0",
                        "interestAmount", "1100.0", "amountRemaining", "249600.0"));

        Result<Page<ScheduledRepayment>> result = service.schedule(LOAN_NUMBER, 1, 12);

        assertEquals(1, result.value().items().get(0).paymentNumber());
    }

    @Test
    void validLoanIsCreated() {
        connection.reply("loan.create", loanRow("New"));

        Result<LoanNumber> result = service.create(CUSTOMER, DETAILS);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(LOAN_NUMBER, result.value()),
                () -> assertEquals("01-0013", connection.onlyRequest("loan.create").getValue("customerNumber"))
        );
    }

    @Test
    void invalidLoanIsRejectedWithoutCallingTheMainframe() {
        Result<LoanNumber> result = service.create(CUSTOMER, detailsWith(Loan.RateType.FIXED, BigDecimal.ZERO));

        assertAll(
                () -> assertEquals(List.of(new FieldError("principal", "Principal must be greater than 0.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requestTypes().isEmpty())
        );
    }

    @Test
    void loanNeedsACustomer() {
        Result<LoanNumber> result = service.create(null, DETAILS);

        assertEquals(List.of(LoanRules.CUSTOMER_REQUIRED), result.fieldErrors());
    }

    @Test
    void rateTypeTheMainframeDoesNotOfferIsRejected() {
        Result<LoanNumber> result = service.create(
                CUSTOMER, detailsWith(Loan.RateType.INTEREST_ONLY, DETAILS.principal()));

        assertEquals(List.of(new FieldError("rateType", "Rate type must be one of the listed options.")),
                result.fieldErrors());
    }

    @Test
    void validChangesAreSaved() {
        Map<String, String> saved = loanRow("New");
        saved.putAll(row("principal", "300000.0", "rateType", "Floating"));
        connection.reply("loan.update", saved);
        Loan.Details changed = detailsWith(Loan.RateType.FLOATING, new BigDecimal("300000.0"));

        Result<Loan> result = service.update(LOAN, changed);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(changed, result.value().details()),
                () -> assertEquals("Floating", connection.onlyRequest("loan.update").getValue("rateType"))
        );
    }

    @Test
    void invalidChangesAreRejectedWithoutCallingTheMainframe() {
        Result<Loan> result = service.update(LOAN, detailsWith(null, DETAILS.principal()));

        assertAll(
                () -> assertEquals(List.of(new FieldError("rateType", "Rate type is required.")),
                        result.fieldErrors()),
                () -> assertTrue(connection.requestTypes().isEmpty())
        );
    }

    @Test
    void readyLoanIsSubmitted() {
        readyToSubmit("New");

        Result<Loan> result = service.submit(LOAN_NUMBER);

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals(Loan.Status.PENDING, result.value().status()),
                () -> assertEquals("Pending", connection.onlyRequest("loan.updateStatus").getValue("status"))
        );
    }

    @Test
    void submitUsesTheLoansCurrentStatus() {
        readyToSubmit("Pending");

        Result<Loan> result = service.submit(LOAN_NUMBER);

        assertAll(
                () -> assertEquals(List.of(SubmitRequirement.NOT_NEW), result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.updateStatus").isEmpty())
        );
    }

    @Test
    void loanThatIsNotReadyIsNotSubmitted() {
        loanToSubmit("New", "n");

        Result<Loan> result = service.submit(LOAN_NUMBER);

        assertAll(
                () -> assertEquals(List.of(SubmitRequirement.NO_MAILING_ADDRESS), result.fieldErrors()),
                () -> assertTrue(connection.requests("loan.updateStatus").isEmpty())
        );
    }

    @Test
    void rejectedStatusChangeIsReported() {
        loanToSubmit("New", "y").fail("loan.updateStatus", 3, "Invalid status change");

        assertEquals("Invalid status change", service.submit(LOAN_NUMBER).errorMessage());
    }

    @Test
    void requiresEveryDependency() {
        LoanGateway loans = new LoanGateway(adapter);
        RepaymentGateway repayments = new RepaymentGateway(adapter);
        AddressGateway addresses = new AddressGateway(adapter);
        EmployerGateway employers = new EmployerGateway(adapter);
        CoborrowerGateway coborrowers = new CoborrowerGateway(adapter);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new LoanService(null, repayments, addresses, employers, coborrowers, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class,
                        () -> new LoanService(loans, null, addresses, employers, coborrowers, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class,
                        () -> new LoanService(loans, repayments, null, employers, coborrowers, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class,
                        () -> new LoanService(loans, repayments, addresses, null, coborrowers, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class,
                        () -> new LoanService(loans, repayments, addresses, employers, null, () -> CONSTANTS)),
                () -> assertThrows(NullPointerException.class,
                        () -> new LoanService(loans, repayments, addresses, employers, coborrowers, null))
        );
    }
}
