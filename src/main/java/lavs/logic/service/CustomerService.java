package lavs.logic.service;

import lavs.logic.adapter.CustomerGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;

import java.time.LocalDate;
import java.util.List;

public class CustomerService {

    private final CustomerGateway customerGateway;


    public CustomerService(CustomerGateway customerGateway) {
        this.customerGateway = customerGateway;
    }

    // TODO: Will need a team discussion regarding page size, is this fixed?
    public Page<Customer.Summary> search(long page, String searchTerm, long size) {
        return customerGateway.search(page, searchTerm, size);
    }

    public Customer retrieve(CustomerNumber customerNumber) {
        return customerGateway.retrieve(customerNumber);
    }

    public CustomerNumber create(Customer.Details customerDetails) {
        validate(customerDetails);
        return customerGateway.create(customerDetails);
    }

    public Customer update(CustomerNumber customerNumber, Customer.Details customerDetails) {
        validate(customerDetails);
        return customerGateway.update(customerNumber, customerDetails);
    }

    private static void validate(Customer.Details customerDetails) {
        List<FieldError> errors = customerDetails.validate(LocalDate.now());
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

}
