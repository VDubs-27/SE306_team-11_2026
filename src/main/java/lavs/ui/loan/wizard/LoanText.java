package lavs.ui.loan.wizard;

import java.util.Locale;

import lavs.logic.model.loan.Loan;
import lavs.ui.common.DisplayFormat;

/**
 * How a new loan's details are put into words on the wizard's steps, so each step says them the same way.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class LoanText {

    private LoanText() {
    }

    /** A rate type as people say it, e.g. "Interest only" rather than the code "InterestOnly". */
    static String rateTypeName(Loan.RateType type) {
        return type == Loan.RateType.INTEREST_ONLY ? "Interest only" : type.code();
    }

    /** A repayment frequency as people say it, e.g. "On renewal" rather than "Onrenewal". */
    static String paymentFrequencyName(Loan.PaymentFrequency frequency) {
        return frequency == Loan.PaymentFrequency.ON_RENEWAL ? "On renewal" : frequency.code();
    }

    /** e.g. "6.29% yearly · Fixed". */
    static String interest(Loan.Details details) {
        return details.rateAmount().stripTrailingZeros().toPlainString() + "% "
                + details.rateFrequency().code().toLowerCase(Locale.ROOT) + " · "
                + rateTypeName(details.rateType());
    }

    /** e.g. "$1,312.14 monthly". */
    static String repayment(Loan.Details details) {
        return DisplayFormat.moneyWithCents(details.paymentAmount()) + " "
                + paymentFrequencyName(details.paymentFrequency()).toLowerCase(Locale.ROOT);
    }
}
