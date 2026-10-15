package lavs.logic.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.LoanNumber;
import mainframe.client.StatusMessage;
import mainframe.client.messages.loan.AddCoborrower;
import mainframe.client.messages.loan.DeleteCoborrower;
import mainframe.client.messages.loanCoborrower.Retrieve;

public class CoborrowerGateway {

    private final MainframeAdapter adapter;

    public CoborrowerGateway(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public Page<Coborrower.Summary> list(LoanNumber loan, long page, long size) {
        mainframe.client.messages.loanCoborrower.List message =
                adapter.send(new mainframe.client.messages.loanCoborrower.List(loan.value(), page, size));
        return new Page<>(summaries(loan, Arrays.asList(message.getItems())), page, size, message.getTotal());
    }

    public List<Coborrower.Summary> all(LoanNumber loan) {
        return summaries(loan, Pages.readAll(adapter,
                page -> new mainframe.client.messages.loanCoborrower.List(loan.value(), page, Pages.SIZE),
                mainframe.client.messages.loanCoborrower.List::getItems,
                mainframe.client.messages.loanCoborrower.List::getTotal));
    }

    public Coborrower retrieve(LoanNumber loan, long id) {
        Retrieve message = adapter.send(new Retrieve(id, loan.value()));
        return toCoborrower(loan, message.getId(), message.getType(), message.getCoborrowerNumber(),
                message.getCoborrowerName());
    }

    // The add message does not return the co-borrower's name, so the new record is read back
    public Coborrower add(LoanNumber loan, Coborrower.Details details) {
        AddCoborrower message = adapter.send(new AddCoborrower(
                details.coborrowerNumber().value(), loan.value(), details.type().code()));
        return retrieve(loan, message.getId());
    }

    public void delete(LoanNumber loan, long id) {
        adapter.send(new DeleteCoborrower(id, loan.value()));
    }

    // The mainframe rejects a retrieve for an unknown customer number as a server failure
    // (for example "'99-9999' is not a valid customer number"); any other failure is passed on
    public boolean isCustomer(CustomerNumber customer) {
        try {
            adapter.send(new mainframe.client.messages.customer.Retrieve(customer.value()));
            return true;
        } catch (MainframeException e) {
            if (e.isServerError()) {
                return false;
            }
            throw e;
        }
    }

    private static List<Coborrower.Summary> summaries(
            LoanNumber loan, List<mainframe.client.messages.loanCoborrower.List.Item> items) {
        return items.stream()
                .map(item -> toSummary(loan, item))
                .toList();
    }

    // A blank co-borrower number cannot become a CustomerNumber; like the loan gateway, that is reported as a record
    // the mainframe sent that could not be read, rather than escaping as an IllegalArgumentException
    private static Coborrower.Summary toSummary(
            LoanNumber loan, mainframe.client.messages.loanCoborrower.List.Item item) {
        try {
            return new Coborrower.Summary(item.getId(), loan, Coborrower.Type.fromCode(item.getType()),
                    new CustomerNumber(item.getCoborrowerNumber()), item.getCoborrowerName());
        } catch (IllegalArgumentException e) {
            throw unreadable(e);
        }
    }

    private static Coborrower toCoborrower(LoanNumber loan, long id, String type, String coborrowerNumber,
                                           String coborrowerName) {
        try {
            return new Coborrower(id, loan, Coborrower.Type.fromCode(type), new CustomerNumber(coborrowerNumber),
                    coborrowerName);
        } catch (IllegalArgumentException e) {
            throw unreadable(e);
        }
    }

    private static MainframeException unreadable(RuntimeException cause) {
        return new MainframeException(StatusMessage.UNABLE_TO_PARSE.getCode(),
                "The mainframe returned a co-borrower record that could not be read.", cause);
    }
}
