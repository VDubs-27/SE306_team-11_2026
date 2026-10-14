package lavs.logic.model.constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConstantCategoryTests {

    @ParameterizedTest
    @CsvSource({
        "CustomerStatus", "CustomerAddressType", "CustomerEmailType", "CustomerEmployerType",
        "CustomerPhoneNumberType", "LoanStatus", "LoanRateType", "LoanRateFrequency",
        "LoanPaymentFrequency", "LoanCoborrowerType"
    })
    void findsEachMainframeCategoryByName(String name) {
        Optional<ConstantCategory<?>> category = ConstantCategory.fromName(name);

        assertTrue(category.isPresent());
        assertEquals(name, category.get().name());
    }

    @ParameterizedTest
    @CsvSource({"CustomerRelationshipType", "customerstatus", "''"})
    void unknownNamesAreNotFound(String name) {
        assertTrue(ConstantCategory.fromName(name).isEmpty());
    }

    @Test
    void namesAreUnique() {
        long distinct = ConstantCategory.ALL.stream().map(ConstantCategory::name).distinct().count();

        assertEquals(ConstantCategory.ALL.size(), distinct);
    }

    @Test
    void toStringIsTheMainframeName() {
        assertEquals("LoanRateType", ConstantCategory.RATE_TYPE.toString());
    }
}
