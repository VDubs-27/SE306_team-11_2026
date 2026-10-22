package lavs.ui.common;

import java.util.Locale;

import javafx.scene.control.Label;

import lavs.logic.model.loan.Loan;

/**
 * How a loan's details are put into words, so the profile, the new loan wizard and the loan screen
 * all say them the same way.
 * AI-assisted: written by Jack, refined with Claude.
 */
public final class LoanText {

    private LoanText() {
    }

    /** A rate type as people say it, e.g. "Interest only" rather than the code "InterestOnly". */
    public static String rateTypeName(Loan.RateType type) {
        return type == Loan.RateType.INTEREST_ONLY ? "Interest only" : type.code();
    }

    /** A repayment frequency as people say it, e.g. "On renewal" rather than "Onrenewal". */
    public static String paymentFrequencyName(Loan.PaymentFrequency frequency) {
        return frequency == Loan.PaymentFrequency.ON_RENEWAL ? "On renewal" : frequency.code();
    }

    /** e.g. "6.29% yearly · Fixed". */
    public static String interest(Loan.Details details) {
        return details.rateAmount().stripTrailingZeros().toPlainString() + "% "
                + details.rateFrequency().code().toLowerCase(Locale.ROOT) + " · "
                + rateTypeName(details.rateType());
    }

    /** e.g. "$1,312.14 monthly", or "$1,312.14 on renewal"; a dash when there's no amount. */
    public static String repayment(Loan.Details details) {
        if (details.paymentAmount() == null) {
            return DisplayFormat.NOT_RECORDED;
        }
        String amount = DisplayFormat.moneyWithCents(details.paymentAmount());
        Loan.PaymentFrequency frequency = details.paymentFrequency();
        return frequency == null || frequency == Loan.PaymentFrequency.UNKNOWN
                ? amount
                : amount + " " + paymentFrequencyName(frequency).toLowerCase(Locale.ROOT);
    }

    /** A loan's status as a coloured badge: green when active, yellow while new or pending, red when defaulted. */
    public static Label statusBadge(Loan.Status status) {
        return RecordTiles.badge(status.code(), switch (status) {
            case ACTIVE -> "badge-active";
            case DEFAULTED -> "badge-error";
            case NEW, PENDING -> "badge-warning";
            default -> "badge-neutral";
        });
    }
}
