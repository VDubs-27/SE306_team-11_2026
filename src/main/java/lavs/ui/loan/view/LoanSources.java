package lavs.ui.loan.view;

import java.util.Map;
import java.util.function.Supplier;

import lavs.logic.model.Page;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.ScheduledRepayment;
import lavs.logic.service.Result;
import lavs.ui.common.DocumentFile;
import lavs.ui.common.Fetch;
import lavs.ui.common.NoteRow;
import lavs.ui.common.ServiceList;
import lavs.ui.common.Sources;
import lavs.ui.loan.people.LoanPeopleController;
import lavs.ui.loan.people.LoanPerson;
import lavs.ui.shell.Services;

/**
 * One loan's records for the loan screen, and its changes, through the services. Each method is
 * called in the background. The loan is read as it is at the time, as editing it replaces it.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class LoanSources {

    private final Services services;
    private final Supplier<Loan> loan;
    /** Where each person is on the loan, by customer number, for removing them; kept up to date as they change. */
    private final Map<String, Long> coborrowerIds;

    LoanSources(Services services, Supplier<Loan> loan, Map<String, Long> coborrowerIds) {
        this.services = services;
        this.loan = loan;
        this.coborrowerIds = coborrowerIds;
    }

    Result<Page<ScheduledRepayment>> schedule(long page, long size) {
        return services.loan().loans().schedule(loan.get().loanNumber(), page, size);
    }

    /** The loan's totals, or none (null) when the mainframe can't give them. */
    Result<Loan.Totals> totals() {
        return Result.success(LoanLoader.totals(services, loan.get()));
    }

    Result<Loan> update(Loan.Details changed) {
        return services.loan().loans().update(loan.get(), changed);
    }

    /** Moves the loan from New to Pending, once the mainframe's rules for submitting are met. */
    Result<Loan> submit() {
        return services.loan().loans().submit(loan.get().loanNumber());
    }

    ServiceList.Source<DocumentFile, DocumentFile> documents() {
        return Sources.documents(services.loan().documents(), loan.get().loanNumber());
    }

    ServiceList.Source<NoteRow, NoteRow> notes() {
        return Sources.notes(services.loan().notes(), loan.get().loanNumber());
    }

    /** Adding or removing a co-borrower or guarantor, saved to the loan straight away. */
    LoanPeopleController.Saving people() {
        return new LoanPeopleController.Saving() {
            @Override
            public Result<?> add(LoanPerson person) {
                String number = person.customer().number().value();
                return Fetch.run(() -> {
                    Coborrower added = Fetch.value(services.loan().coborrowers().add(loan.get(),
                            new Coborrower.Details(person.role(), person.customer().number())));
                    coborrowerIds.put(number, added.id());
                    return added;
                });
            }

            @Override
            public Result<?> remove(LoanPerson person) {
                String number = person.customer().number().value();
                Long id = coborrowerIds.get(number);
                if (id == null) {
                    return Result.failed("This person isn't on the loan any more. Open the loan again to check.");
                }
                return Fetch.run(() -> {
                    Fetch.value(services.loan().coborrowers().delete(loan.get().loanNumber(), id));
                    coborrowerIds.remove(number);
                    return person;
                });
            }
        };
    }
}
