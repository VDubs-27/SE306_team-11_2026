package lavs.logic.model.customer;

import java.util.List;
import java.util.Objects;

public record CustomerProfile(
        Customer customer,
        List<Address.Summary> addresses,
        List<Phone.Summary> phones,
        List<Email.Summary> emails,
        List<Employer.Summary> employers
) {
    public CustomerProfile {
        Objects.requireNonNull(customer, "customer");
        addresses = List.copyOf(addresses);
        phones = List.copyOf(phones);
        emails = List.copyOf(emails);
        employers = List.copyOf(employers);
    }
}