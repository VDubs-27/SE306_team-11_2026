package lavs.ui.loan.wizard;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lavs.logic.model.FieldError;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.document.Document;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import lavs.logic.service.CoborrowerService;
import lavs.logic.service.DocumentService;
import lavs.logic.service.LoanService;
import lavs.logic.service.Result;
import lavs.ui.common.DocumentFile;
import lavs.ui.loan.people.LoanPerson;
import lavs.ui.shell.LoanServices;
import org.junit.jupiter.api.Test;

class LoanSaverTests {

    private static final CustomerNumber APPLICANT = new CustomerNumber("01-0001-0000015");
    private static final CustomerNumber GUARANTOR = new CustomerNumber("01-0001-0000068");
    private static final LoanNumber NUMBER = new LoanNumber("01-0001-0000015-002");
    private static final Loan.Details DETAILS = new Loan.Details(new BigDecimal("20000"), Loan.RateType.FIXED,
            new BigDecimal("6.5"), Loan.RateFrequency.YEARLY, LocalDate.of(2026, 11, 1), new BigDecimal("400"),
            Loan.PaymentFrequency.MONTHLY, LocalDate.of(2028, 11, 1));

    private final LoanService loans = mock(LoanService.class);
    @SuppressWarnings("unchecked")
    private final DocumentService<LoanNumber> documents = mock(DocumentService.class);
    private final CoborrowerService coborrowers = mock(CoborrowerService.class);
    private final LoanServices services = new LoanServices(loans, coborrowers, documents, null);
    private final NewLoanDraft draft = new NewLoanDraft(new LoanPerson(customer(APPLICANT, "John"), null, null, false));

    LoanSaverTests() {
        draft.setDetails(DETAILS);
        draft.others().add(new LoanPerson(customer(GUARANTOR, "Jane"), Coborrower.Type.GUARANTOR, null, true));
        draft.documents().add(new DocumentFile(0, "Payslip", "payslip.pdf", "PDF", 1024));
        when(loans.create(APPLICANT, DETAILS)).thenReturn(Result.success(NUMBER));
        when(coborrowers.add(any(), any())).thenReturn(Result.success(null));
        when(documents.add(eq(NUMBER), any())).thenReturn(Result.success(null));
    }

    @Test
    void createsTheLoanAddsEveryoneAndItsDocumentsThenSubmitsIt() {
        Loan pending = loan(Loan.Status.PENDING);
        when(loans.submit(NUMBER)).thenReturn(Result.success(pending));

        Result<LoanSaver.Saved> saved = LoanSaver.save(services, draft);

        assertAll(
                () -> assertTrue(saved.successful()),
                () -> assertEquals(pending, saved.value().loan()),
                () -> assertEquals(List.of(), saved.value().notSaved()),
                () -> assertNull(saved.value().notSubmitted())
        );
        verify(coborrowers).add(any(), eq(new Coborrower.Details(Coborrower.Type.GUARANTOR, GUARANTOR)));
        verify(documents).add(NUMBER, new Document.Details("Payslip", "payslip.pdf", "PDF", 1024));
    }

    @Test
    void someoneWhoCantBeAddedIsListedAndTheRestStillGoesAhead() {
        when(coborrowers.add(any(), any())).thenReturn(Result.failed("Rejected"));
        when(loans.submit(NUMBER)).thenReturn(Result.success(loan(Loan.Status.PENDING)));

        Result<LoanSaver.Saved> saved = LoanSaver.save(services, draft);

        assertEquals(List.of("Guarantor Jane Smith: Rejected"), saved.value().notSaved());
        verify(documents).add(eq(NUMBER), any());
        verify(loans).submit(NUMBER);
    }

    @Test
    void aLoanThatCantBeSubmittedYetIsKeptAsNewWithTheReason() {
        Loan asNew = loan(Loan.Status.NEW);
        when(loans.submit(NUMBER)).thenReturn(Result.invalid(List.of(new FieldError("mailingAddress",
                "The customer needs a mailing address before the loan can be submitted."))));
        when(loans.retrieve(NUMBER)).thenReturn(Result.success(asNew));

        Result<LoanSaver.Saved> saved = LoanSaver.save(services, draft);

        assertAll(
                () -> assertTrue(saved.successful()),
                () -> assertEquals(asNew, saved.value().loan()),
                () -> assertEquals("The customer needs a mailing address before the loan can be submitted.",
                        saved.value().notSubmitted())
        );
    }

    @Test
    void nothingElseIsSentWhenTheLoanCantBeCreated() {
        when(loans.create(APPLICANT, DETAILS)).thenReturn(Result.failed("Could not reach the mainframe."));

        Result<LoanSaver.Saved> saved = LoanSaver.save(services, draft);

        assertAll(
                () -> assertFalse(saved.successful()),
                () -> assertEquals("Could not reach the mainframe.", saved.errorMessage())
        );
        verify(coborrowers, never()).add(any(), any());
        verify(documents, never()).add(any(), any());
        verify(loans, never()).submit(any());
    }

    private static Customer customer(CustomerNumber number, String firstName) {
        return new Customer(number, null, firstName, "Smith", Customer.Status.ACTIVE, null, null);
    }

    private static Loan loan(Loan.Status status) {
        return new Loan(NUMBER, APPLICANT, status, DETAILS.principal(), DETAILS.rateType(), DETAILS.rateAmount(),
                DETAILS.rateFrequency(), DETAILS.startDate(), DETAILS.paymentAmount(), DETAILS.paymentFrequency(),
                DETAILS.renewalDate());
    }
}
