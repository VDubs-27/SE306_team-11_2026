package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lavs.logic.adapter.EmailGateway;
import lavs.logic.adapter.PhoneGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Email;

public class EmailService {

    private final EmailGateway emails;
    private final ContactRequirement contacts;
    private final Supplier<Constants> constants;

    public EmailService(PhoneGateway phones, EmailGateway emails, Supplier<Constants> constants) {
        this.emails = emails;
        this.contacts = new ContactRequirement(
                Objects.requireNonNull(phones, "phones"), Objects.requireNonNull(emails, "emails"));
        this.constants = Objects.requireNonNull(constants, "constants");
    }

    public Result<Page<Email.Summary>> list(CustomerNumber customer, long page, long size) {
        return Result.attempt(() -> Result.success(emails.list(customer, page, size)));
    }

    public Result<Email> get(CustomerNumber customer, long id) {
        return Result.attempt(() -> Result.success(emails.retrieve(customer, id)));
    }

    public Result<Email> add(CustomerNumber customer, Email.Details details) {
        return save(customer, Rules.NEW_RECORD, details, () -> emails.add(customer, details));
    }

    public Result<Email> update(CustomerNumber customer, long id, Email.Details details) {
        return save(customer, id, details, () -> emails.update(customer, id, details));
    }

    public Result<Void> delete(CustomerNumber customer, long id) {
        return Result.attempt(() -> {
            if (!contacts.allowsDeletingOne(customer)) {
                return Result.failed(ContactRequirement.LAST_CONTACT_MESSAGE);
            }
            emails.delete(customer, id);
            return Result.success(null);
        });
    }

    private Result<Email> save(CustomerNumber customer, long id, Email.Details details, Supplier<Email> send) {
        List<FieldError> errors = Rules.withTypeCheck(
                details.validate(), constants.get(), ConstantCategory.EMAIL_TYPE, details.type(), "Email type");
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> {
            if (details.isPrimary() && Rules.otherIsPrimary(
                    emails.all(customer), id, Email.Summary::isPrimary, Email.Summary::id)) {
                return Result.invalid(List.of(Rules.primaryTaken("email address")));
            }
            return Result.success(send.get());
        });
    }
}
