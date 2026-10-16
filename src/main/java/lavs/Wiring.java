package lavs;

import java.time.Clock;
import java.util.function.Supplier;

import lavs.logic.adapter.DocumentGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.NoteGateway;
import lavs.logic.model.constants.Constants;
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
import lavs.logic.service.Session;
import lavs.ui.shell.CustomerServices;
import lavs.ui.shell.LoanServices;
import lavs.ui.shell.Services;

// Builds every service once, on one connection to the mainframe. The UI only ever sees the services;
// list checks read the constants of whoever is signed in, through the session.
final class Wiring {

    private Wiring() {
    }

    static Services connect(String mainframeUrl) {
        Session session = new Session();
        Gateways gateways = new Gateways(MainframeAdapter.connect(mainframeUrl));
        return new Services(session, new LoginService(gateways.mainframe(), session),
                customerServices(gateways, session::constants), loanServices(gateways, session::constants));
    }

    private static CustomerServices customerServices(Gateways gateways, Supplier<Constants> constants) {
        return new CustomerServices(
                new CustomerService(gateways.customers(), gateways.addresses(), gateways.phones(), gateways.emails(),
                        gateways.employers(), Clock.systemDefaultZone()),
                new AddressService(gateways.addresses(), constants),
                new PhoneService(gateways.phones(), gateways.emails(), constants),
                new EmailService(gateways.phones(), gateways.emails(), constants),
                new EmployerService(gateways.employers(), constants),
                new DocumentService<>(DocumentGateway.forCustomers(gateways.mainframe())),
                new NoteService<>(NoteGateway.forCustomers(gateways.mainframe())));
    }

    private static LoanServices loanServices(Gateways gateways, Supplier<Constants> constants) {
        return new LoanServices(
                new LoanService(gateways.loans(), gateways.repayments(), gateways.customers(), gateways.addresses(),
                        gateways.phones(), gateways.emails(), gateways.employers(), gateways.coborrowers(), constants),
                new CoborrowerService(gateways.coborrowers(), constants),
                new DocumentService<>(DocumentGateway.forLoans(gateways.mainframe())),
                new NoteService<>(NoteGateway.forLoans(gateways.mainframe())));
    }
}
