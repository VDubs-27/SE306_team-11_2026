package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lavs.logic.adapter.AddressGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.CustomerNumber;

public class AddressService {

    private final AddressGateway addresses;
    private final Supplier<Constants> constants;

    public AddressService(AddressGateway addresses, Supplier<Constants> constants) {
        this.addresses = Objects.requireNonNull(addresses, "addresses");
        this.constants = Objects.requireNonNull(constants, "constants");
    }

    public Result<Page<Address.Summary>> list(CustomerNumber customer, long page, long size) {
        return Result.attempt(() -> Result.success(addresses.list(customer, page, size)));
    }

    public Result<Address> get(CustomerNumber customer, long id) {
        return Result.attempt(() -> Result.success(addresses.retrieve(customer, id)));
    }

    public Result<Address> add(CustomerNumber customer, Address.Details details) {
        return save(customer, Rules.NEW_RECORD, details, () -> addresses.add(customer, details));
    }

    public Result<Address> update(CustomerNumber customer, long id, Address.Details details) {
        return save(customer, id, details, () -> addresses.update(customer, id, details));
    }

    public Result<Void> delete(CustomerNumber customer, long id) {
        return Result.attempt(() -> {
            addresses.delete(customer, id);
            return Result.success(null);
        });
    }

    private Result<Address> save(CustomerNumber customer, long id, Address.Details details, Supplier<Address> send) {
        List<FieldError> errors = Rules.withTypeCheck(
                details.validate(), constants.get(), ConstantCategory.ADDRESS_TYPE, details.type(), "Address type");
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> {
            if (details.isPrimary() && Rules.otherIsPrimary(
                    addresses.all(customer), id, Address.Summary::isPrimary, Address.Summary::id)) {
                return Result.invalid(List.of(Rules.primaryTaken("address")));
            }
            return Result.success(send.get());
        });
    }
}
