package lavs.ui.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Employer;
import lavs.logic.model.customer.Phone;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;

/** Temporary sample data for designing. Replaced by CustomerService results. */
final class SampleProfiles {

    private static final CustomerNumber JOHN = new CustomerNumber("01-0013-5810482");

    private static final Map<String, Customer> CUSTOMERS = Map.of(
            JOHN.value(), customer(JOHN.value(), "Mr", "John", "Smith", LocalDate.of(1985, 3, 14)),
            "01-0013-5811967", customer("01-0013-5811967", "Mr", "John", "Smith", LocalDate.of(1962, 11, 2)),
            "01-0013-5810233", customer("01-0013-5810233", "Mrs", "Sarah", "Smith", LocalDate.of(1990, 7, 21)),
            "01-0013-5812810", customer("01-0013-5812810", "Ms", "Aroha", "Smith", LocalDate.of(1978, 1, 9)),
            "01-0013-5810775", customer("01-0013-5810775", "Mr", "David", "Smith", LocalDate.of(1971, 5, 30)),
            "01-0013-5813302", customer("01-0013-5813302", "Ms", "Mei", "Smith", LocalDate.of(1995, 9, 17)),
            "01-0013-5811408", customer("01-0013-5811408", "Mr", "Peter", "Smithson", LocalDate.of(1958, 12, 3)));

    private SampleProfiles() {
    }

    /** The sample profile for a customer number; John Smith has every section filled in. */
    static CustomerProfile find(String customerNumber) {
        Customer customer = CUSTOMERS.get(customerNumber);
        if (customer == null) {
            throw new IllegalArgumentException("No sample customer " + customerNumber);
        }
        if (customer.number().equals(JOHN)) {
            return johnSmith(customer);
        }
        return new CustomerProfile(customer, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static Customer customer(String number, String title, String firstName,
            String familyName, LocalDate dateOfBirth) {
        return new Customer(new CustomerNumber(number), title, firstName, familyName,
                Customer.Status.ACTIVE, dateOfBirth, "New Zealand");
    }

    private static CustomerProfile johnSmith(Customer customer) {
        List<Address> addresses = List.of(
                new Address(1, JOHN, Address.Type.RESIDENTIAL, null, "12 Ponsonby Road", null,
                        "Ponsonby", "Auckland", "1011", "New Zealand", true, true),
                new Address(2, JOHN, Address.Type.POSTAL, null, "PO Box 4412", null,
                        "Shortland Street", "Auckland", "1140", "New Zealand", false, false),
                new Address(3, JOHN, Address.Type.OTHER, "Holiday home", "8 Marine Parade", null,
                        null, "Mount Maunganui", "3116", "New Zealand", false, false));
        List<Phone> phones = List.of(
                new Phone(1, JOHN, Phone.Type.MOBILE, null, "64", "21 555 0182", true, true));
        List<Email> emails = List.of(
                new Email(1, JOHN, Email.Type.PERSONAL, null, "john.smith@example.co.nz", true));
        List<Employer> employers = List.of(
                new Employer(1, JOHN, Employer.Type.PRIMARY, "Harbour Engineering Ltd",
                        "Project Manager", new BigDecimal("98000")));
        List<Loan.Summary> loans = List.of(
                new Loan.Summary(new LoanNumber("L-10023"), JOHN, Loan.Status.ACTIVE,
                        new BigDecimal("450000"), LocalDate.of(2021, 6, 1)));
        return new CustomerProfile(customer, addresses, phones, emails, employers, loans);
    }
}
