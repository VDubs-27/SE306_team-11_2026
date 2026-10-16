package lavs.logic.adapter;

import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import mainframe.client.StatusMessage;
import mainframe.client.messages.loan.Create;
import mainframe.client.messages.loan.Retrieve;
import mainframe.client.messages.loan.Update;
import mainframe.client.messages.loan.UpdateStatus;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoanGateway {

    private final MainframeAdapter adapter;

    public LoanGateway(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public Page<Loan.Summary> list(CustomerNumber customerNumber, long page, long size) {
        mainframe.client.messages.loan.List list =
                adapter.send(new mainframe.client.messages.loan.List(customerNumber.value(), page, size));
        List<Loan.Summary> summaries = new ArrayList<>();
        for (mainframe.client.messages.loan.List.Item item : list.getItems()) {
            summaries.add(toSummary(customerNumber, item));
        }
        return new Page<>(summaries, page, size, list.getTotal());
    }

    public Loan retrieve(LoanNumber loanNumber) {
        Retrieve retrieve = adapter.send(new Retrieve(loanNumber.value()));
        return toLoan(loanNumber, retrieve.getCustomerNumber(), retrieve.getStatus(), retrieve.getPrincipal(),
                retrieve.getRateType(), retrieve.getRateAmount(), retrieve.getRateFrequency(),
                retrieve.getStartDate(), retrieve.getPaymentAmount(), retrieve.getPaymentFrequency(),
                retrieve.getRenewalDate());
    }

    public LoanNumber create(CustomerNumber customerNumber, Loan.Details loanDetails) {
        Create create = adapter.send(new Create(
                customerNumber.value(), Values.amount(loanDetails.paymentAmount()),
                loanDetails.paymentFrequency().code(), Values.amount(loanDetails.principal()),
                Values.amount(loanDetails.rateAmount()), loanDetails.rateFrequency().code(),
                loanDetails.rateType().code(), Values.text(loanDetails.renewalDate()),
                Values.text(loanDetails.startDate())));
        return new LoanNumber(create.getLoanNumber());
    }

    // Unlike CustomerGateway, the result is not read from the response: the mainframe's update response leaves out
    // the customer and loan numbers and always reports the status as New, even for a Pending loan
    public Loan update(Loan loan, Loan.Details loanDetails) {
        adapter.send(new Update(
                loan.customerNumber().value(), loan.loanNumber().value(), Values.amount(loanDetails.paymentAmount()),
                loanDetails.paymentFrequency().code(), Values.amount(loanDetails.principal()),
                Values.amount(loanDetails.rateAmount()), loanDetails.rateFrequency().code(),
                loanDetails.rateType().code(), Values.text(loanDetails.renewalDate()),
                Values.text(loanDetails.startDate())));
        return new Loan(loan.loanNumber(), loan.customerNumber(), loan.status(), loanDetails.principal(),
                loanDetails.rateType(), loanDetails.rateAmount(), loanDetails.rateFrequency(), loanDetails.startDate(),
                loanDetails.paymentAmount(), loanDetails.paymentFrequency(), loanDetails.renewalDate());
    }

    public void updateStatus(LoanNumber loanNumber, Loan.Status status) {
        adapter.send(new UpdateStatus(loanNumber.value(), status.code()));
    }

    // A row that cannot be read fails the whole page, so the page never claims more loans than it shows
    private static Loan.Summary toSummary(
            CustomerNumber customerNumber, mainframe.client.messages.loan.List.Item item) {
        try {
            return new Loan.Summary(new LoanNumber(item.getLoanNumber()), customerNumber,
                    Loan.Status.fromCode(item.getStatus()), Values.decimal(item.getPrincipal()),
                    Values.date(item.getStartDate()));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw unreadable(e);
        }
    }

    private static Loan toLoan(LoanNumber loanNumber, String customerNumber, String status, Double principal,
                               String rateType, Double rateAmount, String rateFrequency, String startDate,
                               Double paymentAmount, String paymentFrequency, String renewalDate) {
        try {
            return new Loan(loanNumber, new CustomerNumber(customerNumber), Loan.Status.fromCode(status),
                    Values.decimal(principal), Loan.RateType.fromCode(rateType), Values.decimal(rateAmount),
                    Loan.RateFrequency.fromCode(rateFrequency), Values.date(startDate), Values.decimal(paymentAmount),
                    Loan.PaymentFrequency.fromCode(paymentFrequency), Values.date(renewalDate));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw unreadable(e);
        }
    }

    private static MainframeException unreadable(RuntimeException cause) {
        return new MainframeException(
                StatusMessage.UNABLE_TO_PARSE.getCode(), "The mainframe returned a loan record that could " +
                "not be read.", cause);
    }
}
