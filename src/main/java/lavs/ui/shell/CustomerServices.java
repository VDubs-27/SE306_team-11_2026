package lavs.ui.shell;

import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.service.AddressService;
import lavs.logic.service.CustomerService;
import lavs.logic.service.DocumentService;
import lavs.logic.service.EmailService;
import lavs.logic.service.EmployerService;
import lavs.logic.service.NoteService;
import lavs.logic.service.PhoneService;

/**
 * The services for customers and everything recorded about them.
 * AI-assisted: written by Jack, refined with Claude.
 */
public record CustomerServices(
        CustomerService customers,
        AddressService addresses,
        PhoneService phones,
        EmailService emails,
        EmployerService employers,
        DocumentService<CustomerNumber> documents,
        NoteService<CustomerNumber> notes) {
}
