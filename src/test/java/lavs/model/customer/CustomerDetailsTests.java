package lavs.model.customer;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CustomerDetailsTests {

    private static CustomerDetails customer(String firstName, String familyName) {
        return new CustomerDetails(null, firstName, familyName, null, null);
    }

    @Test
    void onlyNamesAreRequired() {
        Assertions.assertEquals(List.of(), customer("Ann", "Lee").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankFirstNameIsRejected(String firstName) {
        Assertions.assertEquals(
                List.of("First name is required."),
                customer(firstName, "Lee").validate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankFamilyNameIsRejected(String familyName) {
        Assertions.assertEquals(
                List.of("Family name is required."),
                customer("Ann", familyName).validate());
    }

    @Test
    void everyMissingFieldIsReported() {
        Assertions.assertEquals(
                List.of("First name is required.", "Family name is required."),
                customer(null, null).validate());
    }
}
