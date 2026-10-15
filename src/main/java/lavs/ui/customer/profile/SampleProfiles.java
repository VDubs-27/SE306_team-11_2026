package lavs.ui.customer.profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Employer;
import lavs.logic.model.customer.Phone;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;

/**
 * Temporary sample data for designing. Replaced by CustomerService results.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
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
        return new CustomerProfile(customer, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    /** One page of a sample list, the way the mainframe returns lists a page at a time. */
    static <T> Page<T> pageOf(List<T> items, long page, long size) {
        int from = (int) Math.min((page - 1) * size, items.size());
        int to = (int) Math.min(from + size, items.size());
        return new Page<>(items.subList(from, to), page, size, items.size());
    }

    /** A simpler sample loan for John, so the Loans tab has enough to page through. */
    private static Loan loan(String suffix, Loan.Status status, String principal, String repayment,
            Loan.PaymentFrequency frequency, int startYear) {
        return new Loan(new LoanNumber(JOHN.value() + "-" + suffix), JOHN, status, new BigDecimal(principal),
                Loan.RateType.FLOATING, new BigDecimal("7.25"), Loan.RateFrequency.YEARLY,
                LocalDate.of(startYear, 3, 1), new BigDecimal(repayment), frequency,
                LocalDate.of(startYear + 5, 3, 1));
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
                new Phone(1, JOHN, Phone.Type.MOBILE, null, "64", "21 555 0182", true, true),
                new Phone(2, JOHN, Phone.Type.HOME, null, "64", "9 555 4321", false, false));
        List<Email> emails = List.of(
                new Email(1, JOHN, Email.Type.PERSONAL, null, "john.smith@example.co.nz", true));
        List<Employer> employers = List.of(
                new Employer(1, JOHN, Employer.Type.PRIMARY, "Harbour Engineering Ltd",
                        "Project Manager", new BigDecimal("98000")));
        List<Loan> loans = List.of(
                new Loan(new LoanNumber("01-0013-5810482-001"), JOHN, Loan.Status.PENDING,
                        new BigDecimal("450000"), Loan.RateType.FIXED, new BigDecimal("6.29"),
                        Loan.RateFrequency.YEARLY, LocalDate.of(2026, 11, 1), new BigDecimal("1312.14"),
                        Loan.PaymentFrequency.FORTNIGHTLY, LocalDate.of(2028, 11, 1)),
                new Loan(new LoanNumber("01-0013-5810482-002"), JOHN, Loan.Status.ACTIVE,
                        new BigDecimal("120000"), Loan.RateType.FLOATING, new BigDecimal("7.25"),
                        Loan.RateFrequency.YEARLY, LocalDate.of(2021, 6, 1), new BigDecimal("1050"),
                        Loan.PaymentFrequency.MONTHLY, LocalDate.of(2027, 6, 1)),
                loan("003", Loan.Status.ACTIVE, "35000", "412.50", Loan.PaymentFrequency.MONTHLY, 2023),
                loan("004", Loan.Status.CANCELLED, "60000", "650.00", Loan.PaymentFrequency.MONTHLY, 2020),
                loan("005", Loan.Status.ACTIVE, "18500", "98.40", Loan.PaymentFrequency.WEEKLY, 2024),
                loan("006", Loan.Status.DEFAULTED, "12000", "240.00", Loan.PaymentFrequency.FORTNIGHTLY, 2019));
        List<DocumentRow> documents = List.of(
                new DocumentRow(1, "Proof of income", "Payslip-September-2026.pdf", "application/pdf", 188_416),
                new DocumentRow(2, "Identification", "Drivers-licence.jpg", "image/jpeg", 1_258_291),
                new DocumentRow(3, "Proof of income", "Payslip-August-2026.pdf", "application/pdf", 181_248),
                new DocumentRow(4, "Proof of address", "Power-bill-July-2026.pdf", "application/pdf", 342_016),
                new DocumentRow(5, "Bank statement", "Statement-Q2-2026.pdf", "application/pdf", 604_160),
                new DocumentRow(6, "KiwiSaver", "KiwiSaver-withdrawal-estimate.pdf", "application/pdf", 96_256),
                new DocumentRow(7, "Identification", "Passport.png", "image/png", 2_411_725));
        // Newest first, as the Notes tab shows them
        List<NoteRow> notes = List.of(
                new NoteRow(12, "Application", "Bob Ross", LocalDate.of(2026, 10, 12),
                        "Customer is applying for a $450,000 mortgage on a Papamoa property. "
                                + "Partner Sarah Smith will be a co-borrower."),
                new NoteRow(11, "Contact", "Bob Ross", LocalDate.of(2026, 10, 5),
                        "Prefers to be contacted by mobile after 4pm on weekdays."),
                new NoteRow(10, "General", "Ana Tui", LocalDate.of(2026, 9, 18),
                        "Updated mailing address after the customer moved from Grey Lynn."),
                new NoteRow(9, "Contact", "Ana Tui", LocalDate.of(2026, 8, 30),
                        "Left a voicemail about the missing payslip. Will try again Monday."),
                new NoteRow(8, "Application", "Bob Ross", LocalDate.of(2026, 8, 14),
                        "Pre-approval discussed in branch. Customer is looking at properties in Papamoa and "
                                + "Mount Maunganui in the $500,000 to $650,000 range and expects a deposit of "
                                + "about 20% from savings and KiwiSaver. Asked them to bring three recent payslips, "
                                + "their KiwiSaver withdrawal estimate and bank statements for the last three months "
                                + "so we can confirm the deposit before the application goes to the credit team."),
                new NoteRow(7, "General", "Peter Smith", LocalDate.of(2026, 7, 2),
                        "Customer asked about fixed versus floating rates. Sent the rate sheet."),
                new NoteRow(6, "Contact", "Peter Smith", LocalDate.of(2026, 5, 21),
                        "Changed preferred contact from home phone to mobile."),
                new NoteRow(5, "General", "Maram\u0101 Lee", LocalDate.of(2026, 3, 9),
                        "Annual review done. No changes to employment or income."),
                new NoteRow(4, "Loan", "Maram\u0101 Lee", LocalDate.of(2025, 11, 27),
                        "Loan -002 repayments moved from fortnightly to monthly at the customer's request."),
                new NoteRow(3, "General", "Peter Smith", LocalDate.of(2025, 7, 3),
                        "Customer queried a fee on their statement. Explained the annual account fee."),
                new NoteRow(2, "Identification", "Maram\u0101 Lee", LocalDate.of(2025, 7, 1),
                        "Passport, power bill and latest payslip sighted and copied to the filestore."),
                new NoteRow(1, "Initial account setup", "Peter Smith", LocalDate.of(2025, 6, 30),
                        "New customer set up in branch."));
        return new CustomerProfile(customer, addresses, phones, emails, employers, loans, documents, notes);
    }
}
