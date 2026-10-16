package lavs.logic.model.constants;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Employer;
import lavs.logic.model.customer.Phone;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;

public final class ConstantCategory<E extends Enum<E>> {

    public static final ConstantCategory<Customer.Status> CUSTOMER_STATUS =
            new ConstantCategory<>("CustomerStatus", Customer.Status::fromCode, Customer.Status.UNKNOWN);
    public static final ConstantCategory<Address.Type> ADDRESS_TYPE =
            new ConstantCategory<>("CustomerAddressType", Address.Type::fromCode, Address.Type.UNKNOWN);
    public static final ConstantCategory<Email.Type> EMAIL_TYPE =
            new ConstantCategory<>("CustomerEmailType", Email.Type::fromCode, Email.Type.UNKNOWN);
    public static final ConstantCategory<Employer.Type> EMPLOYER_TYPE =
            new ConstantCategory<>("CustomerEmployerType", Employer.Type::fromCode, Employer.Type.UNKNOWN);
    public static final ConstantCategory<Phone.Type> PHONE_TYPE =
            new ConstantCategory<>("CustomerPhoneNumberType", Phone.Type::fromCode, Phone.Type.UNKNOWN);
    public static final ConstantCategory<Loan.Status> LOAN_STATUS =
            new ConstantCategory<>("LoanStatus", Loan.Status::fromCode, Loan.Status.UNKNOWN);
    public static final ConstantCategory<Loan.RateType> RATE_TYPE =
            new ConstantCategory<>("LoanRateType", Loan.RateType::fromCode, Loan.RateType.UNKNOWN);
    public static final ConstantCategory<Loan.RateFrequency> RATE_FREQUENCY =
            new ConstantCategory<>("LoanRateFrequency", Loan.RateFrequency::fromCode, Loan.RateFrequency.UNKNOWN);
    public static final ConstantCategory<Loan.PaymentFrequency> PAYMENT_FREQUENCY =
            new ConstantCategory<>("LoanPaymentFrequency", Loan.PaymentFrequency::fromCode,
                    Loan.PaymentFrequency.UNKNOWN);
    public static final ConstantCategory<Coborrower.Type> COBORROWER_TYPE =
            new ConstantCategory<>("LoanCoborrowerType", Coborrower.Type::fromCode, Coborrower.Type.UNKNOWN);

    public static final List<ConstantCategory<?>> ALL = List.of(
            CUSTOMER_STATUS, ADDRESS_TYPE, EMAIL_TYPE, EMPLOYER_TYPE, PHONE_TYPE,
            LOAN_STATUS, RATE_TYPE, RATE_FREQUENCY, PAYMENT_FREQUENCY, COBORROWER_TYPE);

    private final String name;
    private final Function<String, E> fromCode;
    private final E unknown;

    private ConstantCategory(String name, Function<String, E> fromCode, E unknown) {
        this.name = name;
        this.fromCode = fromCode;
        this.unknown = unknown;
    }

    public static Optional<ConstantCategory<?>> fromName(String name) {
        return ALL.stream().filter(category -> category.name.equals(name)).findFirst();
    }

    public String name() {
        return name;
    }

    Optional<E> valueOf(String code) {
        E value = fromCode.apply(code);
        return value == unknown ? Optional.empty() : Optional.of(value);
    }

    @Override
    public String toString() {
        return name;
    }
}
