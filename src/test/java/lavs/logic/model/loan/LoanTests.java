package lavs.logic.model.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lavs.logic.model.FieldError;
import lavs.logic.model.customer.CustomerNumber;
import mainframe.client.messages.loan.Create;
import mainframe.client.messages.loan.UpdateStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LoanTests {

    private static final LoanNumber LOAN = new LoanNumber("01-0001-0000015-001");
    private static final CustomerNumber CUSTOMER = new CustomerNumber("C123");
    private static final LocalDate START = LocalDate.of(2025, 1, 2);
    private static final LocalDate RENEWAL = LocalDate.of(2030, 1, 2);
    private static final BigDecimal PRINCIPAL = new BigDecimal("1234.56");
    private static final BigDecimal RATE = new BigDecimal("2.34");
    private static final BigDecimal PAYMENT = new BigDecimal("100.00");

    private static Loan loan(Loan.Status status) {
        return new Loan(
                LOAN, CUSTOMER, status, PRINCIPAL, Loan.RateType.FIXED, RATE, Loan.RateFrequency.MONTHLY,
                START, PAYMENT, Loan.PaymentFrequency.WEEKLY, RENEWAL);
    }

    private static Loan.Details details(BigDecimal principal, BigDecimal rate, LocalDate start, LocalDate renewal) {
        return new Loan.Details(
                principal, Loan.RateType.FIXED, rate, Loan.RateFrequency.MONTHLY,
                start, PAYMENT, Loan.PaymentFrequency.WEEKLY, renewal);
    }

    @Test
    void missingLoanNumberThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new Loan(
                null, CUSTOMER, Loan.Status.NEW, null, null, null, null, null, null, null, null));
    }

    @Test
    void missingCustomerNumberThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new Loan(
                LOAN, null, Loan.Status.NEW, null, null, null, null, null, null, null, null));
    }

    @Test
    void withStatusChangesOnlyTheStatus() {
        Loan created = loan(Loan.Status.NEW);

        Loan pending = created.withStatus(Loan.Status.PENDING);

        Assertions.assertAll(
                () -> Assertions.assertEquals(loan(Loan.Status.PENDING), pending),
                () -> Assertions.assertEquals(Loan.Status.NEW, created.status(), "original should be unchanged")
        );
    }

    @ParameterizedTest
    @EnumSource(Loan.Status.class)
    void withStatusRejectsEveryOtherTransition(Loan.Status from) {
        for (Loan.Status to : Loan.Status.values()) {
            if (from == Loan.Status.NEW && to == Loan.Status.PENDING) {
                continue;
            }
            Assertions.assertThrows(
                    IllegalStateException.class, () -> loan(from).withStatus(to), from + " to " + to);
        }
    }

    @Test
    void withStatusRejectsLoanWithNoStatus() {
        Assertions.assertThrows(IllegalStateException.class, () -> loan(null).withStatus(Loan.Status.PENDING));
    }

    @Test
    void detailsCopiesEveryEditableField() {
        Assertions.assertEquals(
                new Loan.Details(
                        PRINCIPAL, Loan.RateType.FIXED, RATE, Loan.RateFrequency.MONTHLY,
                        START, PAYMENT, Loan.PaymentFrequency.WEEKLY, RENEWAL),
                loan(Loan.Status.NEW).details());
    }

    @Test
    void completeSummaryIsAccepted() {
        Loan.Summary summary = new Loan.Summary(LOAN, CUSTOMER, Loan.Status.NEW, PRINCIPAL, START);

        Assertions.assertEquals(LOAN, summary.loanNumber());
    }

    @Test
    void summaryRequiresLoanNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new Loan.Summary(
                null, CUSTOMER, Loan.Status.NEW, PRINCIPAL, START));
    }

    @Test
    void completeTotalsAreAccepted() {
        Loan.Totals totals = new Loan.Totals(LOAN, PAYMENT, RATE, 1, START, PAYMENT);

        Assertions.assertEquals(LOAN, totals.loanNumber());
    }

    @Test
    void totalsRequireLoanNumber() {
        Assertions.assertThrows(NullPointerException.class, () -> new Loan.Totals(
                null, PAYMENT, RATE, 1, START, PAYMENT));
    }

    @Nested
    class StatusTransitions {

        @Test
        void newCanMoveToPending() {
            Assertions.assertTrue(Loan.Status.NEW.canMoveTo(Loan.Status.PENDING));
        }

        @ParameterizedTest
        @EnumSource(Loan.Status.class)
        void noOtherTransitionIsAllowed(Loan.Status from) {
            for (Loan.Status to : Loan.Status.values()) {
                boolean isNewToPending = from == Loan.Status.NEW && to == Loan.Status.PENDING;
                Assertions.assertEquals(isNewToPending, from.canMoveTo(to), from + " to " + to);
            }
        }

        @ParameterizedTest
        @EnumSource(Loan.Status.class)
        void unknownCannotMoveAnywhere(Loan.Status to) {
            Assertions.assertFalse(Loan.Status.UNKNOWN.canMoveTo(to));
        }

        @Test
        void statusCodesMatchTheMainframeConstants() {
            Assertions.assertEquals(
                    List.of(
                            UpdateStatus.STATUS_NEW,
                            UpdateStatus.STATUS_PENDING,
                            UpdateStatus.STATUS_ACTIVE,
                            UpdateStatus.STATUS_DEFAULTED,
                            UpdateStatus.STATUS_CANCELLED),
                    Arrays.stream(Loan.Status.values())
                            .filter(value -> value != Loan.Status.UNKNOWN)
                            .map(Loan.Status::code)
                            .toList());
        }

        @ParameterizedTest
        @EnumSource(value = Loan.Status.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
        void everyStatusIsFoundByItsCode(Loan.Status value) {
            Assertions.assertEquals(value, Loan.Status.fromCode(value.code()));
        }

        @Test
        void statusLookupIgnoresCase() {
            Assertions.assertEquals(Loan.Status.PENDING, Loan.Status.fromCode("PENDING"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Approved", "UNKNOWN"})
        void unrecognisedStatusIsUnknown(String code) {
            Assertions.assertEquals(Loan.Status.UNKNOWN, Loan.Status.fromCode(code));
        }
    }

    @Nested
    class DetailsValidation {

        @Test
        void completeLoanHasNoErrors() {
            Assertions.assertEquals(List.of(), loan(Loan.Status.NEW).details().validate());
        }

        @Test
        void everyMissingFieldIsReported() {
            Loan.Details empty = new Loan.Details(null, null, null, null, null, null, null, null);

            Assertions.assertEquals(
                    List.of(
                            new FieldError("principal", "Principal is required."),
                            new FieldError("rateType", "Rate type is required."),
                            new FieldError("rateAmount", "Rate is required."),
                            new FieldError("rateFrequency", "Rate frequency is required."),
                            new FieldError("startDate", "Start date is required."),
                            new FieldError("paymentAmount", "Payment amount is required."),
                            new FieldError("paymentFrequency", "Payment frequency is required."),
                            new FieldError("renewalDate", "Renewal date is required.")),
                    empty.validate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.00", "-0.01"})
        void principalOfZeroOrLessIsRejected(String principal) {
            Assertions.assertEquals(
                    List.of(new FieldError("principal", "Principal must be greater than 0.")),
                    details(new BigDecimal(principal), RATE, START, RENEWAL).validate());
        }

        @Test
        void smallestPositivePrincipalIsAccepted() {
            Assertions.assertEquals(List.of(), details(new BigDecimal("0.01"), RATE, START, RENEWAL).validate());
        }

        @Test
        void negativeRateIsRejected() {
            Assertions.assertEquals(
                    List.of(new FieldError("rateAmount", "Rate must be 0 or more.")),
                    details(PRINCIPAL, new BigDecimal("-0.01"), START, RENEWAL).validate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.01"})
        void rateOfZeroOrMoreIsAccepted(String rate) {
            Assertions.assertEquals(List.of(), details(PRINCIPAL, new BigDecimal(rate), START, RENEWAL).validate());
        }

        @ParameterizedTest
        @ValueSource(longs = {0, -1})
        void renewalOnOrBeforeStartIsRejected(long daysAfterStart) {
            Assertions.assertEquals(
                    List.of(new FieldError("renewalDate", "Renewal date must be after the start date.")),
                    details(PRINCIPAL, RATE, START, START.plusDays(daysAfterStart)).validate());
        }

        @Test
        void missingStartDateWithValidRenewalReportsOnlyStartDate() {
            Assertions.assertEquals(
                    List.of(new FieldError("startDate", "Start date is required.")),
                    details(PRINCIPAL, RATE, null, RENEWAL).validate());
        }

        @Test
        void missingRenewalDateWithValidStartReportsOnlyRenewalDate() {
            Assertions.assertEquals(
                    List.of(new FieldError("renewalDate", "Renewal date is required.")),
                    details(PRINCIPAL, RATE, START, null).validate());
        }

        @Test
        void renewalOneDayAfterStartIsAccepted() {
            Assertions.assertEquals(List.of(), details(PRINCIPAL, RATE, START, START.plusDays(1)).validate());
        }
    }

    @Nested
    class EnumCodes {

        @Test
        void rateTypeCodesMatchTheMainframeConstants() {
            Assertions.assertEquals(
                    List.of(Create.RATETYPE_FIXED, Create.RATETYPE_FLOATING, Create.RATETYPE_INTERESTONLY),
                    Arrays.stream(Loan.RateType.values())
                            .filter(value -> value != Loan.RateType.UNKNOWN)
                            .map(Loan.RateType::code)
                            .toList());
        }

        @Test
        void rateFrequencyCodesMatchTheMainframeConstants() {
            Assertions.assertEquals(
                    List.of(
                            Create.RATEFREQUENCY_DAILY,
                            Create.RATEFREQUENCY_WEEKLY,
                            Create.RATEFREQUENCY_FORTNIGHTLY,
                            Create.RATEFREQUENCY_MONTHLY,
                            Create.RATEFREQUENCY_YEARLY),
                    Arrays.stream(Loan.RateFrequency.values())
                            .filter(value -> value != Loan.RateFrequency.UNKNOWN)
                            .map(Loan.RateFrequency::code)
                            .toList());
        }

        @Test
        void paymentFrequencyCodesMatchTheMainframeConstants() {
            Assertions.assertEquals(
                    List.of(
                            Create.PAYMENTFREQUENCY_WEEKLY,
                            Create.PAYMENTFREQUENCY_FORTNIGHTLY,
                            Create.PAYMENTFREQUENCY_MONTHLY,
                            Create.PAYMENTFREQUENCY_YEARLY,
                            Create.PAYMENTFREQUENCY_ONRENEWAL),
                    Arrays.stream(Loan.PaymentFrequency.values())
                            .filter(value -> value != Loan.PaymentFrequency.UNKNOWN)
                            .map(Loan.PaymentFrequency::code)
                            .toList());
        }

        @ParameterizedTest
        @EnumSource(value = Loan.RateType.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
        void everyRateTypeIsFoundByItsCode(Loan.RateType value) {
            Assertions.assertEquals(value, Loan.RateType.fromCode(value.code()));
        }

        @ParameterizedTest
        @EnumSource(value = Loan.RateFrequency.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
        void everyRateFrequencyIsFoundByItsCode(Loan.RateFrequency value) {
            Assertions.assertEquals(value, Loan.RateFrequency.fromCode(value.code()));
        }

        @ParameterizedTest
        @EnumSource(value = Loan.PaymentFrequency.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
        void everyPaymentFrequencyIsFoundByItsCode(Loan.PaymentFrequency value) {
            Assertions.assertEquals(value, Loan.PaymentFrequency.fromCode(value.code()));
        }

        @Test
        void codeLookupIgnoresCase() {
            Assertions.assertEquals(Loan.RateType.INTEREST_ONLY, Loan.RateType.fromCode("interestonly"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Hourly", "UNKNOWN"})
        void unrecognisedCodesAreUnknown(String code) {
            Assertions.assertAll(
                    () -> Assertions.assertEquals(Loan.RateType.UNKNOWN, Loan.RateType.fromCode(code)),
                    () -> Assertions.assertEquals(Loan.RateFrequency.UNKNOWN, Loan.RateFrequency.fromCode(code)),
                    () -> Assertions.assertEquals(Loan.PaymentFrequency.UNKNOWN, Loan.PaymentFrequency.fromCode(code))
            );
        }

        @Test
        void unknownValuesCannotBeSaved() {
            Loan.Details details = new Loan.Details(
                    PRINCIPAL, Loan.RateType.UNKNOWN, RATE, Loan.RateFrequency.UNKNOWN,
                    START, PAYMENT, Loan.PaymentFrequency.UNKNOWN, RENEWAL);

            Assertions.assertEquals(
                    List.of(
                            new FieldError("rateType", "Rate type must be one of the listed options."),
                            new FieldError("rateFrequency", "Rate frequency must be one of the listed options."),
                            new FieldError("paymentFrequency", "Payment frequency must be one of the listed options.")),
                    details.validate());
        }
    }
}
