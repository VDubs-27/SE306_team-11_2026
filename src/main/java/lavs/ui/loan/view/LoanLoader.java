package lavs.ui.loan.view;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lavs.logic.model.Page;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.logic.service.Result;
import lavs.ui.common.Fetch;
import lavs.ui.loan.people.LoanPerson;
import lavs.ui.loan.people.PersonLoader;
import lavs.ui.shell.Services;

/**
 * Reads what the loan screen needs before it opens: the loan's totals, everyone on it, and how many
 * documents and notes it has. The schedule, documents and notes are read when their tabs are opened.
 * Run it in the background.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class LoanLoader {

    private LoanLoader() {
    }

    /**
     * What the loan screen shows when it opens. totals is null when the mainframe can't work them out;
     * coborrowerIds gives each person's place on the loan by customer number, for removing them.
     */
    public record LoanScreen(Loan loan, LoanPerson applicant, Loan.Totals totals, List<LoanPerson> others,
                             Map<String, Long> coborrowerIds, long documents, long notes) {
    }

    /** The screen for this loan, which the applicant has, or why it couldn't be read. */
    public static Result<LoanScreen> load(Services all, Loan loan, LoanPerson applicant) {
        return Fetch.run(() -> {
            List<Coborrower.Summary> coborrowers = Fetch.all(
                    page -> all.loan().coborrowers().list(loan.loanNumber(), page, Fetch.PAGE_SIZE));
            Map<String, Long> ids = new ConcurrentHashMap<>();
            coborrowers.forEach(person -> ids.put(person.coborrowerNumber().value(), person.id()));
            return new LoanScreen(loan, applicant, totals(all, loan),
                    Fetch.each(coborrowers,
                            person -> PersonLoader.load(all.customer(), person.coborrowerNumber(), person.type())),
                    ids,
                    count(all.loan().documents().list(loan.loanNumber(), 1, 1)),
                    Fetch.value(all.loan().notes().count(loan.loanNumber())));
        });
    }

    /** The loan's totals, or null when the mainframe can't give them, so the rest of the loan still shows. */
    static Loan.Totals totals(Services all, Loan loan) {
        Result<Loan.Totals> totals = all.loan().loans().totals(loan.loanNumber());
        return totals.successful() ? totals.value() : null;
    }

    private static long count(Result<? extends Page<?>> firstPage) {
        return Fetch.value(firstPage).total();
    }
}
