package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lavs.logic.adapter.EmployerGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Employer;

public class EmployerService {

    private final EmployerGateway employers;
    private final Supplier<Constants> constants;

    public EmployerService(EmployerGateway employers, Supplier<Constants> constants) {
        this.employers = Objects.requireNonNull(employers, "employers");
        this.constants = Objects.requireNonNull(constants, "constants");
    }

    public Result<Page<Employer.Summary>> list(CustomerNumber customer, long page, long size) {
        return Result.attempt(() -> Result.success(employers.list(customer, page, size)));
    }

    public Result<Employer> get(CustomerNumber customer, long id) {
        return Result.attempt(() -> Result.success(employers.retrieve(customer, id)));
    }

    public Result<Employer> add(CustomerNumber customer, Employer.Details details) {
        return save(details, () -> employers.add(customer, details));
    }

    public Result<Employer> update(CustomerNumber customer, long id, Employer.Details details) {
        return save(details, () -> employers.update(customer, id, details));
    }

    public Result<Void> delete(CustomerNumber customer, long id) {
        return Result.attempt(() -> {
            employers.delete(customer, id);
            return Result.success(null);
        });
    }

    private Result<Employer> save(Employer.Details details, Supplier<Employer> send) {
        List<FieldError> errors = Rules.withTypeCheck(
                details.validate(), constants.get(), ConstantCategory.EMPLOYER_TYPE, details.type(), "Employer type");
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> Result.success(send.get()));
    }
}
