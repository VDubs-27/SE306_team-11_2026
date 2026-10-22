package lavs.ui.shell;

import lavs.logic.model.loan.LoanNumber;
import lavs.logic.service.CoborrowerService;
import lavs.logic.service.DocumentService;
import lavs.logic.service.LoanService;
import lavs.logic.service.NoteService;

/**
 * The services for loans and everything recorded about them.
 * AI-assisted: written by Jack, refined with Claude.
 */
public record LoanServices(
        LoanService loans,
        CoborrowerService coborrowers,
        DocumentService<LoanNumber> documents,
        NoteService<LoanNumber> notes) {
}
