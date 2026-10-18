package lavs.ui.customer.wizard;

import static lavs.logic.adapter.ScriptedConnection.row;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import lavs.logic.adapter.AddressGateway;
import lavs.logic.adapter.CustomerGateway;
import lavs.logic.adapter.EmailGateway;
import lavs.logic.adapter.EmployerGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.PhoneGateway;
import lavs.logic.adapter.ScriptedConnection;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Employer;
import lavs.logic.model.customer.Phone;
import lavs.logic.service.AddressService;
import lavs.logic.service.CustomerService;
import lavs.logic.service.EmailService;
import lavs.logic.service.EmployerService;
import lavs.logic.service.PhoneService;
import lavs.logic.service.Result;
import lavs.ui.shell.CustomerServices;
import org.junit.jupiter.api.Test;

class CustomerSaverTests {

    private static final String NUMBER = "01-0001-0009999";

    private final ScriptedConnection connection = new ScriptedConnection();
    private final CustomerServices services = services(new MainframeAdapter(connection));
    private final NewCustomerDraft draft = new NewCustomerDraft();

    CustomerSaverTests() {
        draft.setDetails(new Customer.Details("Ms", "Aroha", "Smith", LocalDate.of(1990, 1, 2), "New Zealand"));
        draft.phones().add(new Phone.Details(Phone.Type.MOBILE, null, "64", "215550182", false, true));
        draft.emails().add(new Email.Details(Email.Type.PERSONAL, null, "aroha@example.com", false));
    }

    @Test
    void createsTheCustomerThenAddsTheirRecordsAndReadsThemBack() {
        connection.reply("customer.create", row("number", NUMBER))
                .reply("customer.addPhoneNumber", row("id", "1"))
                .reply("customer.addEmail", row("id", "2"))
                .reply("customer.retrieve", row("number", NUMBER, "title", "Ms", "firstName", "Aroha",
                        "familyName", "Smith", "status", "Active", "dateOfBirth", "1990-01-02"));

        Result<CustomerSaver.Saved> saved = CustomerSaver.save(services, draft);

        assertAll(
                () -> assertTrue(saved.successful()),
                () -> assertEquals(NUMBER, saved.value().customer().number().value()),
                () -> assertEquals(Customer.Status.ACTIVE, saved.value().customer().status()),
                () -> assertEquals(List.of(), saved.value().notSaved()),
                () -> assertEquals(NUMBER, connection.onlyRequest("customer.addPhoneNumber").getValue("customer")),
                () -> assertEquals(NUMBER, connection.onlyRequest("customer.addEmail").getValue("customer"))
        );
    }

    @Test
    void aRecordThatFailsIsListedAndTheRestStillSave() {
        connection.reply("customer.create", row("number", NUMBER))
                .fail("customer.addPhoneNumber", 3, "Rejected")
                .reply("customer.addEmail", row("id", "2"))
                .reply("customer.retrieve", row("number", NUMBER, "firstName", "Aroha", "familyName", "Smith",
                        "status", "Active"));

        Result<CustomerSaver.Saved> saved = CustomerSaver.save(services, draft);

        assertAll(
                () -> assertTrue(saved.successful()),
                () -> assertEquals(List.of("Phone +64 215550182: Rejected"), saved.value().notSaved()),
                () -> assertEquals(1, connection.requests("customer.addEmail").size()),
                () -> assertEquals(1, connection.requests("customer.create").size())
        );
    }

    @Test
    void nothingElseIsSavedWhenTheCustomerCantBeCreated() {
        connection.fail("customer.create", 3, "Rejected");

        Result<CustomerSaver.Saved> saved = CustomerSaver.save(services, draft);

        assertAll(
                () -> assertFalse(saved.successful()),
                () -> assertEquals("Rejected", saved.errorMessage()),
                () -> assertEquals(List.of("customer.create"), connection.requestTypes())
        );
    }

    @Test
    void employersAreLeftOutWhenTheCustomerIsntEmployed() {
        draft.setEmployed(false);
        draft.employers().add(new Employer.Details(Employer.Type.PRIMARY, "Acme", null, null));
        connection.reply("customer.create", row("number", NUMBER))
                .reply("customer.addPhoneNumber", row("id", "1"))
                .reply("customer.addEmail", row("id", "2"))
                .reply("customer.retrieve", row("number", NUMBER, "firstName", "Aroha", "familyName", "Smith"));

        CustomerSaver.save(services, draft);

        assertTrue(connection.requests("customer.addEmployer").isEmpty());
    }

    private static CustomerServices services(MainframeAdapter mainframe) {
        Supplier<Constants> constants = () -> new Constants(Map.of(
                ConstantCategory.PHONE_TYPE, List.of("Mobile"),
                ConstantCategory.EMAIL_TYPE, List.of("Personal"),
                ConstantCategory.EMPLOYER_TYPE, List.of("Primary")));
        PhoneGateway phones = new PhoneGateway(mainframe);
        EmailGateway emails = new EmailGateway(mainframe);
        return new CustomerServices(
                new CustomerService(new CustomerGateway(mainframe), new AddressGateway(mainframe), phones, emails,
                        new EmployerGateway(mainframe), Clock.systemDefaultZone()),
                new AddressService(new AddressGateway(mainframe), constants),
                new PhoneService(phones, emails, constants),
                new EmailService(phones, emails, constants),
                new EmployerService(new EmployerGateway(mainframe), constants),
                null, null);
    }
}
