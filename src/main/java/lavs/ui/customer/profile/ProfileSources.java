package lavs.ui.customer.profile;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Employer;
import lavs.logic.model.customer.Phone;
import lavs.logic.model.loan.Loan;
import lavs.logic.service.LoanService;
import lavs.logic.service.Result;
import lavs.ui.common.DocumentFile;
import lavs.ui.common.Fetch;
import lavs.ui.common.NoteRow;
import lavs.ui.common.ServiceList;
import lavs.ui.common.Sources;
import lavs.ui.shell.CustomerServices;
import lavs.ui.shell.Services;

/**
 * One customer's records for the profile's tabs, through the services. The mainframe lists each
 * record as a summary, and the tiles show more than that, so each record on a page is read in full.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class ProfileSources {

    private final CustomerServices services;
    private final LoanService loans;
    private final CustomerNumber number;

    ProfileSources(Services all, CustomerNumber number) {
        this.services = all.customer();
        this.loans = all.loan().loans();
        this.number = number;
    }

    ServiceList.Source<Address, Address.Details> addresses() {
        return new ServiceList.Source<>() {
            @Override
            public Result<Page<Address>> page(long page, long size) {
                return Fetch.page(services.addresses().list(number, page, size),
                        address -> services.addresses().get(number, address.id()));
            }

            @Override
            public Result<?> add(Address.Details details) {
                return services.addresses().add(number, details);
            }

            @Override
            public Result<?> update(Address address, Address.Details details) {
                return services.addresses().update(number, address.id(), details);
            }

            @Override
            public Result<?> remove(Address address) {
                return services.addresses().delete(number, address.id());
            }
        };
    }

    ServiceList.Source<Phone, Phone.Details> phones() {
        return new ServiceList.Source<>() {
            @Override
            public Result<Page<Phone>> page(long page, long size) {
                return Fetch.page(services.phones().list(number, page, size),
                        phone -> services.phones().get(number, phone.id()));
            }

            @Override
            public Result<?> add(Phone.Details details) {
                return services.phones().add(number, details);
            }

            @Override
            public Result<?> update(Phone phone, Phone.Details details) {
                return services.phones().update(number, phone.id(), details);
            }

            @Override
            public Result<?> remove(Phone phone) {
                return services.phones().delete(number, phone.id());
            }
        };
    }

    ServiceList.Source<Email, Email.Details> emails() {
        return new ServiceList.Source<>() {
            @Override
            public Result<Page<Email>> page(long page, long size) {
                return Fetch.page(services.emails().list(number, page, size),
                        email -> services.emails().get(number, email.id()));
            }

            @Override
            public Result<?> add(Email.Details details) {
                return services.emails().add(number, details);
            }

            @Override
            public Result<?> update(Email email, Email.Details details) {
                return services.emails().update(number, email.id(), details);
            }

            @Override
            public Result<?> remove(Email email) {
                return services.emails().delete(number, email.id());
            }
        };
    }

    ServiceList.Source<Employer, Employer.Details> employers() {
        return new ServiceList.Source<>() {
            @Override
            public Result<Page<Employer>> page(long page, long size) {
                return Fetch.page(services.employers().list(number, page, size),
                        employer -> services.employers().get(number, employer.id()));
            }

            @Override
            public Result<?> add(Employer.Details details) {
                return services.employers().add(number, details);
            }

            @Override
            public Result<?> update(Employer employer, Employer.Details details) {
                return services.employers().update(number, employer.id(), details);
            }

            @Override
            public Result<?> remove(Employer employer) {
                return services.employers().delete(number, employer.id());
            }
        };
    }

    /** A page of the customer's loans, each read in full for its repayments. */
    Result<Page<Loan>> loans(long page, long size) {
        return Fetch.page(loans.listForCustomer(number, page, size), loan -> loans.retrieve(loan.loanNumber()));
    }

    ServiceList.Source<DocumentFile, DocumentFile> documents() {
        return Sources.documents(services.documents(), number);
    }

    ServiceList.Source<NoteRow, NoteRow> notes() {
        return Sources.notes(services.notes(), number);
    }
}
