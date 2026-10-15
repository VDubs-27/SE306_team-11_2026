package lavs.logic.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import lavs.logic.adapter.CustomerGateway;
import lavs.logic.adapter.MainframeException;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerServiceTest {

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

    private CustomerGateway gateway;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        gateway = mock(CustomerGateway.class);
        service = new CustomerService(gateway);
    }

    @Test
    void searchReturnsTheGatewayPage() {
        Page<Customer.Summary> page = new Page<>(List.of(), 1, 7, 0);
        when(gateway.search(1, "Smith", 7)).thenReturn(page);

        Assertions.assertSame(page, service.search(1, "Smith", 7));
    }

    @Test
    void retrieveReturnsTheGatewayCustomer() {
        when(gateway.retrieve(NUMBER)).thenReturn(CUSTOMER);

        Assertions.assertSame(CUSTOMER, service.retrieve(NUMBER));
    }

    @Test
    void createReturnsTheNumberFromTheGateway() {
        when(gateway.create(VALID)).thenReturn(NUMBER);

        Assertions.assertEquals(NUMBER, service.create(VALID));
    }

    @Test
    void createWithInvalidDetailsSendsNothing() {
        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> service.create(INVALID));

        Assertions.assertEquals(INVALID_ERRORS, e.errors());
        verifyNoInteractions(gateway);
    }

    @Test
    void createPassesMainframeErrorsThrough() {
        MainframeException failure = new MainframeException(3, "Rejected");
        when(gateway.create(VALID)).thenThrow(failure);

        Assertions.assertSame(failure,
                Assertions.assertThrows(MainframeException.class, () -> service.create(VALID)));
    }

    @Test
    void updateReturnsTheUpdatedCustomerFromTheGateway() {
        when(gateway.update(NUMBER, VALID)).thenReturn(CUSTOMER);

        Assertions.assertSame(CUSTOMER, service.update(NUMBER, VALID));
    }

    @Test
    void updateWithInvalidDetailsSendsNothing() {
        ValidationException e = Assertions.assertThrows(
                ValidationException.class, () -> service.update(NUMBER, INVALID));

        Assertions.assertEquals(INVALID_ERRORS, e.errors());
        verifyNoInteractions(gateway);
    }

    @Test
    void updatePassesMainframeErrorsThrough() {
        MainframeException failure = new MainframeException(3, "Customer not found");
        when(gateway.update(NUMBER, VALID)).thenThrow(failure);

        Assertions.assertSame(failure,
                Assertions.assertThrows(MainframeException.class, () -> service.update(NUMBER, VALID)));
    }
}