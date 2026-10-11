package lavs.model.customer;

import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CustomerSummaryTests {

    @Test
    void fullNameJoinsFirstAndFamilyName() {
        CustomerSummary summary = new CustomerSummary(
                new CustomerNumber("C123"), "Ms", "Ann", "Lee", LocalDate.of(1990, 5, 17), CustomerStatus.ACTIVE);

        Assertions.assertEquals("Ann Lee", summary.fullName());
    }
}
