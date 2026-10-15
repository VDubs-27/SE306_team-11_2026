package lavs.logic.service;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import lavs.logic.adapter.AddressGateway;
import lavs.logic.adapter.CoborrowerGateway;
import lavs.logic.adapter.EmployerGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.ScriptedConnection;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import org.junit.jupiter.api.Test;

class SubmitRequirementTests {

    private static final Loan NEW_LOAN = loan(Loan.Status.NEW);

    private final ScriptedConnection connection = new ScriptedConnection();
    private final MainframeAdapter adapter = new MainframeAdapter(connection);
    private final SubmitRequirement requirement = new SubmitRequirement(
            new AddressGateway(adapter), new EmployerGateway(adapter), new CoborrowerGateway(adapter));

    private static Loan loan(Loan.Status status) {
        return new Loan(new LoanNumber("01-0013-0001"), new CustomerNumber("01-0013"), status,
                null, null, null, null, null, null, null, null);
    }

    // The customer has the given mailing flag and employer count, and the loan has the given people
    private void customerWith(String isMailing, long employers, String... coborrowerTypes) {
        connection
                .replyList("customerAddress.list", 1, row("id", "1", "type", "Postal", "isMailing", isMailing))
                .replyList("customerEmployer.list", employers, row("id", "2", "type", "Primary", "name", "Cafe"));
        @SuppressWarnings("unchecked")
        Map<String, String>[] rows = new Map[coborrowerTypes.length];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = row("id", String.valueOf(i + 1), "type", coborrowerTypes[i], "coborrowerNumber", "01-0020");
        }
        connection.replyList("loanCoborrower.list", rows.length, rows);
    }

    @Test
    void readyLoanHasNoProblems() {
        customerWith("y", 1);

        assertTrue(requirement.problems(NEW_LOAN).isEmpty());
    }

    @Test
    void onlyANewLoanCanBeSubmitted() {
        assertEquals(List.of(SubmitRequirement.NOT_NEW), requirement.problems(loan(Loan.Status.ACTIVE)));
    }

    @Test
    void customerWithoutAMailingAddressCannotSubmit() {
        customerWith("n", 1);

        assertEquals(List.of(SubmitRequirement.NO_MAILING_ADDRESS), requirement.problems(NEW_LOAN));
    }

    @Test
    void unemployedCustomerWithoutAGuarantorCannotSubmit() {
        customerWith("y", 0, "Coborrower");

        assertEquals(List.of(SubmitRequirement.NO_GUARANTOR), requirement.problems(NEW_LOAN));
    }

    @Test
    void unemployedCustomerWithAGuarantorCanSubmit() {
        customerWith("y", 0, "Coborrower", "Guarantor");

        assertTrue(requirement.problems(NEW_LOAN).isEmpty());
    }

    @Test
    void employedCustomerSkipsTheGuarantorCheck() {
        customerWith("y", 1);

        requirement.problems(NEW_LOAN);

        assertTrue(connection.requests("loanCoborrower.list").isEmpty());
    }

    @Test
    void everyMissingRequirementIsReported() {
        customerWith("n", 0);

        assertEquals(List.of(SubmitRequirement.NO_MAILING_ADDRESS, SubmitRequirement.NO_GUARANTOR),
                requirement.problems(NEW_LOAN));
    }
}
