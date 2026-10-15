package lavs.logic.adapter;

import lavs.logic.model.Page;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.LoanNumber;
import lavs.logic.model.loan.ScheduledRepayment;
import mainframe.client.StatusMessage;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

// The figures the mainframe works out from a loan's details: its totals and repayment schedule
public class RepaymentGateway {

    private final MainframeAdapter adapter;

    public RepaymentGateway(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public Loan.Totals totals(LoanNumber loan) {
        mainframe.client.messages.loanSummary.Retrieve message =
                adapter.send(new mainframe.client.messages.loanSummary.Retrieve(loan.value()));
        return read(() -> new Loan.Totals(
                loan, Values.decimal(message.getTotalAmountPaid()), Values.decimal(message.getTotalInterestPaid()),
                message.getNumberOfPayments(), Values.date(message.getFinalPaymentDate()),
                Values.decimal(message.getFinalPaymentAmount())));
    }

    public Page<ScheduledRepayment> schedule(LoanNumber loan, long page, long size) {
        mainframe.client.messages.loanRepaymentSchedule.List message =
                adapter.send(new mainframe.client.messages.loanRepaymentSchedule.List(loan.value(), page, size));
        List<ScheduledRepayment> repayments = read(() -> Arrays.stream(message.getItems())
                .map(item -> new ScheduledRepayment(
                        loan, item.getPaymentNumber(), Values.date(item.getDate()),
                        Values.decimal(item.getAmountPaid()), Values.decimal(item.getPrincipalAmount()),
                        Values.decimal(item.getInterestAmount()), Values.decimal(item.getAmountRemaining())))
                .toList());
        return new Page<>(repayments, page, size, message.getTotal());
    }

    private static <T> T read(Supplier<T> mapping) {
        try {
            return mapping.get();
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new MainframeException(
                    StatusMessage.UNABLE_TO_PARSE.getCode(), "The mainframe returned loan figures that could " +
                    "not be read.", e);
        }
    }
}
