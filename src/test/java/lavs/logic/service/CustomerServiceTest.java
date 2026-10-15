package lavs.logic.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import lavs.logic.adapter.AddressGateway;
import lavs.logic.adapter.CustomerGateway;
import lavs.logic.adapter.EmailGateway;
import lavs.logic.adapter.EmployerGateway;
import lavs.logic.adapter.MainframeException;
import lavs.logic.adapter.PhoneGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.CustomerProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-15T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private static final CustomerNumber NUMBER = new CustomerNumber("C123");
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 17);
    private static final Customer.Details VALID =
            new Customer.Details("Ms", "Ann", "Lee", DATE_OF_BIRTH, "New Zealand");
    private static final Customer.Details INVALID =
            new Customer.Details(null, "", "Lee", null, null);
    private static final List<FieldError> INVALID_ERRORS =
            List.of(new FieldError("firstName", "First name is required."));
    private static final Customer CUSTOMER = new Customer(
            NUMBER, "Ms", "Ann", "Lee", Customer.Status.ACTIVE, DATE_OF_BIRTH, "New Zealand");

    private CustomerGateway customers;
    private AddressGateway addresses;
    private PhoneGateway phones;
    private EmailGateway emails;
    private EmployerGateway employers;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        customers = mock(CustomerGateway.class);
        addresses = mock(AddressGateway.class);
        phones = mock(PhoneGateway.class);
        emails = mock(EmailGateway.class);
        employers = mock(EmployerGateway.class);
        service = new CustomerService(customers, addresses, phones, emails, employers, CLOCK);
    }

    private static Customer.Details bornOn(LocalDate dateOfBirth) {
        return new Customer.Details("Ms", "Ann", "Lee", dateOfBirth, "New Zealand");
    }

    private void verifyNothingSent() {
        verifyNoInteractions(customers, addresses, phones, emails, employers);
    }

    @Test
    void searchReturnsThePageUsingTheServicePageSize() {
        Page<Customer.Summary> page = new Page<>(List.of(), 1, CustomerService.PAGE_SIZE, 0);
        when(customers.search(1, "Smith", CustomerService.PAGE_SIZE)).thenReturn(page);

        Result<Page<Customer.Summary>> result = service.search("Smith", 1);

        Assertions.assertTrue(result.successful());
        Assertions.assertSame(page, result.value());
    }

    @Test
    void searchReportsMainframeErrors() {
        when(customers.search(1, "Smith", CustomerService.PAGE_SIZE))
                .thenThrow(new MainframeException(3, "Rejected"));

        Result<Page<Customer.Summary>> result = service.search("Smith", 1);

        Assertions.assertFalse(result.successful());
        Assertions.assertEquals("Rejected", result.errorMessage());
    }

    @Test
    void retrieveReturnsTheCustomer() {
        when(customers.retrieve(NUMBER)).thenReturn(CUSTOMER);

        Result<Customer> result = service.retrieve(NUMBER);

        Assertions.assertTrue(result.successful());
        Assertions.assertSame(CUSTOMER, result.value());
    }

    @Test
    void retrieveReportsMainframeErrors() {
        when(customers.retrieve(NUMBER)).thenThrow(new MainframeException(3, "Not found"));

        Result<Customer> result = service.retrieve(NUMBER);

        Assertions.assertFalse(result.successful());
        Assertions.assertEquals("Not found", result.errorMessage());
    }

    @Test
    void loadProfileCombinesTheCustomerAndItsChildren() {
        Address.Summary address = new Address.Summary(
                1L, NUMBER, Address.Type.RESIDENTIAL, "Auckland", "New Zealand", true, true);
        when(customers.retrieve(NUMBER)).thenReturn(CUSTOMER);
        when(addresses.all(NUMBER)).thenReturn(List.of(address));
        when(phones.all(NUMBER)).thenReturn(List.of());
        when(emails.all(NUMBER)).thenReturn(List.of());
        when(employers.all(NUMBER)).thenReturn(List.of());

        Result<CustomerProfile> result = service.loadProfile(NUMBER);

        Assertions.assertTrue(result.successful());
        Assertions.assertEquals(
                new CustomerProfile(CUSTOMER, List.of(address), List.of(), List.of(), List.of()),
                result.value());
    }

    @Test
    void loadProfileFailsAsAWholeWhenOneChildFails() {
        when(customers.retrieve(NUMBER)).thenReturn(CUSTOMER);
        when(employers.all(NUMBER)).thenThrow(new MainframeException(3, "Rejected"));

        Result<CustomerProfile> result = service.loadProfile(NUMBER);

        Assertions.assertFalse(result.successful());
        Assertions.assertNull(result.value());
        Assertions.assertEquals("Rejected", result.errorMessage());
    }

    @Test
    void createReturnsTheNumberFromTheGateway() {
        when(customers.create(VALID)).thenReturn(NUMBER);

        Result<CustomerNumber> result = service.create(VALID);

        Assertions.assertTrue(result.successful());
        Assertions.assertEquals(NUMBER, result.value());
    }

    @Test
    void createWithInvalidDetailsSendsNothing() {
        Result<CustomerNumber> result = service.create(INVALID);

        Assertions.assertFalse(result.successful());
        Assertions.assertEquals(INVALID_ERRORS, result.fieldErrors());
        verifyNothingSent();
    }

    @Test
    void createReportsMainframeErrors() {
        when(customers.create(VALID)).thenThrow(new MainframeException(3, "Rejected"));

        Result<CustomerNumber> result = service.create(VALID);

        Assertions.assertFalse(result.successful());
        Assertions.assertEquals("Rejected", result.errorMessage());
        Assertions.assertTrue(result.fieldErrors().isEmpty());
    }

    @Test
    void updateReturnsTheUpdatedCustomer() {
        when(customers.update(NUMBER, VALID)).thenReturn(CUSTOMER);

        Result<Customer> result = service.update(NUMBER, VALID);

        Assertions.assertTrue(result.successful());
        Assertions.assertSame(CUSTOMER, result.value());
    }

    @Test
    void updateWithInvalidDetailsSendsNothing() {
        Result<Customer> result = service.update(NUMBER, INVALID);

        Assertions.assertFalse(result.successful());
        Assertions.assertEquals(INVALID_ERRORS, result.fieldErrors());
        verifyNothingSent();
    }

    @Test
    void updateReportsMainframeErrors() {
        when(customers.update(NUMBER, VALID)).thenThrow(new MainframeException(3, "Customer not found"));

        Result<Customer> result = service.update(NUMBER, VALID);

        Assertions.assertFalse(result.successful());
        Assertions.assertEquals("Customer not found", result.errorMessage());
    }

    @Test
    void dateOfBirthTodayIsRejected() {
        Result<CustomerNumber> result = service.create(bornOn(TODAY));

        Assertions.assertEquals(
                List.of(new FieldError("dateOfBirth", "Date of birth must be in the past.")),
                result.fieldErrors());
        verifyNothingSent();
    }

    @Test
    void dateOfBirthYesterdayIsAccepted() {
        Customer.Details details = bornOn(TODAY.minusDays(1));
        when(customers.create(details)).thenReturn(NUMBER);

        Assertions.assertTrue(service.create(details).successful());
    }
}