package lavs.ui.loan.view;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lavs.logic.model.Page;
import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.ScheduledRepayment;

/**
 * Temporary sample data for the loan screen, worked out from a loan's own details: a standard
 * repayment schedule and its totals. Replaced by LoanService results (the mainframe's repayment
 * schedule and loan summary) once that exists.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class SampleLoans {

    /** No sample schedule runs longer than this. */
    private static final int MAX_PAYMENTS = 30 * 52;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private SampleLoans() {
    }

    /** The loan's repayment schedule: each payment split into principal and interest, until it's paid off. */
    static List<ScheduledRepayment> schedule(Loan loan) {
        List<ScheduledRepayment> payments = new ArrayList<>();
        BigDecimal rate = ratePerPayment(loan);
        BigDecimal balance = loan.principal();
        LocalDate date = nextDate(loan.startDate(), loan.paymentFrequency());
        while (balance.signum() > 0 && payments.size() < MAX_PAYMENTS) {
            BigDecimal interest = money(balance.multiply(rate));
            BigDecimal paid = loan.paymentFrequency() == Loan.PaymentFrequency.ON_RENEWAL
                    ? balance.add(interest)
                    : loan.paymentAmount().min(balance.add(interest));
            BigDecimal principal = paid.subtract(interest);
            if (principal.signum() <= 0 && date.isAfter(loan.renewalDate())) {
                // Repayments that only cover interest: show them up to the renewal date
                break;
            }
            balance = balance.subtract(principal.max(BigDecimal.ZERO));
            payments.add(new ScheduledRepayment(loan.loanNumber(), payments.size() + 1L, date,
                    paid, principal.max(BigDecimal.ZERO), interest, balance));
            date = nextDate(date, loan.paymentFrequency());
        }
        return payments;
    }

    /** One page of the schedule, the way the mainframe returns it a page at a time. */
    static Page<ScheduledRepayment> schedulePage(Loan loan, long page, long size) {
        List<ScheduledRepayment> schedule = schedule(loan);
        int from = (int) Math.min((page - 1) * size, schedule.size());
        int to = (int) Math.min(from + size, schedule.size());
        return new Page<>(schedule.subList(from, to), page, size, schedule.size());
    }

    /** Totals over the whole schedule: everything repaid, the interest in it, and the last payment. */
    static Loan.Totals totals(Loan loan) {
        List<ScheduledRepayment> schedule = schedule(loan);
        BigDecimal paid = schedule.stream().map(ScheduledRepayment::amountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal interest = schedule.stream().map(ScheduledRepayment::interestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ScheduledRepayment last = schedule.isEmpty() ? null : schedule.get(schedule.size() - 1);
        return new Loan.Totals(loan.loanNumber(), paid, interest, schedule.size(),
                last == null ? null : last.date(), last == null ? null : last.amountPaid());
    }

    /** The interest rate for one repayment period, e.g. 6.29% a year paid monthly is 0.5242% a month. */
    private static BigDecimal ratePerPayment(Loan loan) {
        BigDecimal yearly = loan.rateAmount().multiply(BigDecimal.valueOf(timesAYear(loan.rateFrequency())));
        int paymentsAYear = loan.paymentFrequency() == Loan.PaymentFrequency.ON_RENEWAL
                ? 1
                : timesAYear(loan.paymentFrequency());
        return yearly.divide(HUNDRED.multiply(BigDecimal.valueOf(paymentsAYear)), MathContext.DECIMAL64);
    }

    private static int timesAYear(Loan.RateFrequency frequency) {
        return switch (frequency) {
            case DAILY -> 365;
            case WEEKLY -> 52;
            case FORTNIGHTLY -> 26;
            case MONTHLY -> 12;
            default -> 1;
        };
    }

    private static int timesAYear(Loan.PaymentFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> 52;
            case FORTNIGHTLY -> 26;
            case MONTHLY -> 12;
            default -> 1;
        };
    }

    private static LocalDate nextDate(LocalDate date, Loan.PaymentFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> date.plusWeeks(1);
            case FORTNIGHTLY -> date.plusWeeks(2);
            case MONTHLY -> date.plusMonths(1);
            default -> date.plusYears(1);
        };
    }

    private static BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
