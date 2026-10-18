package lavs;

import lavs.logic.adapter.AddressGateway;
import lavs.logic.adapter.CoborrowerGateway;
import lavs.logic.adapter.CustomerGateway;
import lavs.logic.adapter.EmailGateway;
import lavs.logic.adapter.EmployerGateway;
import lavs.logic.adapter.LoanGateway;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.PhoneGateway;
import lavs.logic.adapter.RepaymentGateway;

// The gateways more than one service uses, made once so every service talks through the same ones
record Gateways(
        MainframeAdapter mainframe,
        CustomerGateway customers,
        AddressGateway addresses,
        PhoneGateway phones,
        EmailGateway emails,
        EmployerGateway employers,
        LoanGateway loans,
        RepaymentGateway repayments,
        CoborrowerGateway coborrowers) {

    Gateways(MainframeAdapter mainframe) {
        this(mainframe, new CustomerGateway(mainframe), new AddressGateway(mainframe), new PhoneGateway(mainframe),
                new EmailGateway(mainframe), new EmployerGateway(mainframe), new LoanGateway(mainframe),
                new RepaymentGateway(mainframe), new CoborrowerGateway(mainframe));
    }
}
