package lavs.logic.service;

import lavs.logic.adapter.AddressGateway;
import lavs.logic.adapter.CustomerGateway;
import lavs.logic.adapter.EmailGateway;
import lavs.logic.adapter.EmployerGateway;
import lavs.logic.adapter.PhoneGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.CustomerProfile;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class CustomerService {

    static final long PAGE_SIZE = 7;

    private final CustomerGateway customerGateway;
    private final AddressGateway addressGateway;
    private final PhoneGateway phoneGateway;
    private final EmailGateway emailGateway;
    private final EmployerGateway employerGateway;
    private final Clock clock;

    public CustomerService(CustomerGateway customerGateway, AddressGateway addressGateway,
                           PhoneGateway phoneGateway, EmailGateway emailGateway,
                           EmployerGateway employerGateway, Clock clock) {
        this.customerGateway = Objects.requireNonNull(customerGateway, "customerGateway");
        this.addressGateway = Objects.requireNonNull(addressGateway, "addressGateway");
        this.phoneGateway = Objects.requireNonNull(phoneGateway, "phoneGateway");
        this.emailGateway = Objects.requireNonNull(emailGateway, "emailGateway");
        this.employerGateway = Objects.requireNonNull(employerGateway, "employerGateway");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Result<Page<Customer.Summary>> search(String searchTerm, long page) {
        return Result.attempt(() -> Result.success(customerGateway.search(page, searchTerm, PAGE_SIZE)));
    }

    public Result<Customer> retrieve(CustomerNumber customerNumber) {
        return Result.attempt(() -> Result.success(customerGateway.retrieve(customerNumber)));
    }

    public Result<CustomerProfile> loadProfile(CustomerNumber customerNumber) {
        return Result.attempt(() -> Result.success(new CustomerProfile(
                customerGateway.retrieve(customerNumber),
                addressGateway.all(customerNumber),
                phoneGateway.all(customerNumber),
                emailGateway.all(customerNumber),
                employerGateway.all(customerNumber))));
    }

    public Result<CustomerNumber> create(Customer.Details customerDetails) {
        List<FieldError> errors = validate(customerDetails);
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> Result.success(customerGateway.create(customerDetails)));
    }

    public Result<Customer> update(CustomerNumber customerNumber, Customer.Details customerDetails) {
        List<FieldError> errors = validate(customerDetails);
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> Result.success(customerGateway.update(customerNumber, customerDetails)));
    }

    private List<FieldError> validate(Customer.Details customerDetails) {
        return customerDetails.validate(LocalDate.now(clock));
    }
}