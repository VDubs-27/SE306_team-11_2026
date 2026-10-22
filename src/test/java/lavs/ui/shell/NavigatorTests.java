package lavs.ui.shell;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.LoanNumber;
import lavs.logic.service.AddressService;
import lavs.logic.service.CoborrowerService;
import lavs.logic.service.CustomerService;
import lavs.logic.service.DocumentService;
import lavs.logic.service.EmailService;
import lavs.logic.service.EmployerService;
import lavs.logic.service.LoanService;
import lavs.logic.service.LoginService;
import lavs.logic.service.NoteService;
import lavs.logic.service.PhoneService;
import lavs.logic.service.Result;
import lavs.logic.service.Session;
import lavs.ui.FxTesting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Moves between real screens, as the user would, with the mainframe's services faked. */
class NavigatorTests {

    private static final String TERM = "smith";
    private static final long PAGE_SIZE = 5;
    private static final long FOUND = 12;
    private static final CustomerNumber NUMBER = new CustomerNumber("01-0001-0000015");
    private static final Customer CUSTOMER = new Customer(NUMBER, "Ms", "Ann", "Smith", Customer.Status.ACTIVE,
            LocalDate.of(1990, 3, 5), "New Zealand");

    private final CustomerService customers = mock(CustomerService.class);
    private final AddressService addresses = mock(AddressService.class);
    private final PhoneService phones = mock(PhoneService.class);
    private final EmailService emails = mock(EmailService.class);
    private final EmployerService employers = mock(EmployerService.class);
    @SuppressWarnings("unchecked")
    private final DocumentService<CustomerNumber> customerDocuments = mock(DocumentService.class);
    @SuppressWarnings("unchecked")
    private final NoteService<CustomerNumber> customerNotes = mock(NoteService.class);
    private final LoanService loans = mock(LoanService.class);
    @SuppressWarnings("unchecked")
    private final DocumentService<LoanNumber> loanDocuments = mock(DocumentService.class);
    @SuppressWarnings("unchecked")
    private final NoteService<LoanNumber> loanNotes = mock(NoteService.class);
    private final Services services = new Services(new Session(), mock(LoginService.class),
            new CustomerServices(customers, addresses, phones, emails, employers, customerDocuments, customerNotes),
            new LoanServices(loans, mock(CoborrowerService.class), loanDocuments, loanNotes));

    private Stage stage;
    private Navigator navigator;

    @BeforeAll
    static void startJavaFx() {
        FxTesting.start();
    }

    @BeforeEach
    void openCustomerSearch() {
        when(customers.search(eq(TERM), anyLong())).thenAnswer(call -> Result.success(found(call.getArgument(1))));
        when(customers.retrieve(NUMBER)).thenReturn(Result.success(CUSTOMER));
        when(addresses.list(any(), anyLong(), anyLong())).thenReturn(Result.success(new Page<>(List.of(), 1, 1, 0)));
        when(phones.list(any(), anyLong(), anyLong())).thenReturn(Result.success(new Page<>(List.of(), 1, 1, 0)));
        when(emails.list(any(), anyLong(), anyLong())).thenReturn(Result.success(new Page<>(List.of(), 1, 1, 0)));
        when(employers.list(any(), anyLong(), anyLong())).thenReturn(Result.success(new Page<>(List.of(), 1, 1, 0)));
        when(customerDocuments.list(any(), anyLong(), anyLong()))
                .thenReturn(Result.success(new Page<>(List.of(), 1, 1, 0)));
        when(customerNotes.count(any())).thenReturn(Result.success(0L));
        when(loans.listForCustomer(any(), anyLong(), anyLong()))
                .thenReturn(Result.success(new Page<>(List.of(), 1, 1, 0)));

        FxTesting.run(() -> {
            stage = new Stage();
            stage.setScene(new Scene(new StackPane()));
            navigator = new Navigator(stage, services);
            navigator.showShell();
            navigator.showCustomerSearch();
        });
    }

    @AfterEach
    void finishBackgroundWork() {
        FxTesting.settle();
    }

    @Test
    void backFromAProfileReadsTheResultsPageThatWasOpenAgain() {
        searchAndGoToPageTwo();
        FxTesting.run(() -> navigator.showCustomerProfile(NUMBER.value()));
        FxTesting.settle();

        FxTesting.run(() -> navigator.back());
        FxTesting.settle();

        assertAll(
                () -> assertNotNull(find("#searchField")),
                () -> verify(customers, times(1)).search(TERM, 1),
                () -> verify(customers, times(2)).search(TERM, 2)
        );
    }

    @Test
    void aSavedCustomerOpensInPlaceOfTheWizard() {
        searchAndGoToPageTwo();
        FxTesting.run(() -> navigator.showNewCustomerWizard());

        FxTesting.run(() -> navigator.showCustomerProfileInstead(NUMBER.value()));
        FxTesting.settle();
        assertNotNull(find("#nameLabel"));
        verify(customers, times(1)).search(TERM, 2); // the search behind isn't read again while it's out of sight

        FxTesting.run(() -> navigator.back());
        FxTesting.settle();
        assertAll(
                () -> assertNotNull(find("#searchField")),
                () -> verify(customers, times(2)).search(TERM, 2)
        );
    }

    @Test
    void aSavedCustomerWhoseProfileCantBeReadGoesBackToTheSearchReadAgain() {
        searchAndGoToPageTwo();
        FxTesting.run(() -> navigator.showNewCustomerWizard());
        when(customers.retrieve(NUMBER)).thenReturn(Result.failed("The mainframe is unavailable"));

        FxTesting.run(() -> navigator.showCustomerProfileInstead(NUMBER.value()));
        FxTesting.settle();

        assertAll(
                () -> assertNotNull(find("#searchField")),
                () -> verify(customers, times(2)).search(TERM, 2)
        );
    }

    @Test
    void aProfileStillLoadingWhenTheUserGoesBackDoesNotOpen() throws InterruptedException {
        CountDownLatch release = new CountDownLatch(1);
        when(customers.retrieve(NUMBER)).thenAnswer(call -> {
            assertTrue(release.await(10, TimeUnit.SECONDS));
            return Result.success(CUSTOMER);
        });
        FxTesting.run(() -> navigator.showNewCustomerWizard());
        FxTesting.run(() -> navigator.showCustomerProfile(NUMBER.value()));

        FxTesting.run(() -> navigator.back());
        release.countDown();
        FxTesting.settle();

        assertAll(
                () -> assertNotNull(find("#searchField")),
                () -> assertNull(find("#nameLabel"))
        );
    }

    private void searchAndGoToPageTwo() {
        FxTesting.run(() -> {
            ((TextField) find("#searchField")).setText(TERM);
            ((Button) find("#searchButton")).fire();
        });
        FxTesting.settle();
        FxTesting.run(() -> ((Button) find("#nextButton")).fire());
        FxTesting.settle();
    }

    /** A node on the screen showing now; the screens behind it aren't in the window. */
    private Node find(String selector) {
        return FxTesting.get(() -> {
            // The window is never shown, so styling runs here, which also gives scroll panes their content
            stage.getScene().getRoot().applyCss();
            return stage.getScene().getRoot().lookup(selector);
        });
    }

    private static Page<Customer.Summary> found(long page) {
        long first = (page - 1) * PAGE_SIZE + 1;
        long last = Math.min(FOUND, page * PAGE_SIZE);
        List<Customer.Summary> items = LongStream.rangeClosed(first, last)
                .mapToObj(i -> new Customer.Summary(new CustomerNumber(String.format("01-0001-%07d", i)), "Ms",
                        "Ann", "Smith " + i, LocalDate.of(1990, 3, 5), Customer.Status.ACTIVE))
                .toList();
        return new Page<>(items, page, PAGE_SIZE, FOUND);
    }
}
