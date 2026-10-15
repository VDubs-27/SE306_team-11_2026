package lavs.logic.model.constants;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lavs.logic.model.customer.Address;
import lavs.logic.model.loan.Loan;
import org.junit.jupiter.api.Test;

class ConstantsTests {

    @Test
    void optionsFollowTheMainframeOrder() {
        Constants constants = new Constants(Map.of(
                ConstantCategory.RATE_TYPE, List.of("InterestOnly", "Fixed", "Floating")));

        assertEquals(List.of(Loan.RateType.INTEREST_ONLY, Loan.RateType.FIXED, Loan.RateType.FLOATING),
                constants.options(ConstantCategory.RATE_TYPE));
    }

    @Test
    void optionsLeaveOutValuesLavsDoesNotModel() {
        Constants constants = new Constants(Map.of(
                ConstantCategory.ADDRESS_TYPE, List.of("Residential", "debug_item_1791673040692", "Postal", "")));

        assertEquals(List.of(Address.Type.RESIDENTIAL, Address.Type.POSTAL),
                constants.options(ConstantCategory.ADDRESS_TYPE));
    }

    @Test
    void optionsMatchCodesIgnoringCaseAndDropDuplicates() {
        Constants constants = new Constants(Map.of(
                ConstantCategory.RATE_TYPE, List.of("fixed", "FIXED", "Floating")));

        assertEquals(List.of(Loan.RateType.FIXED, Loan.RateType.FLOATING),
                constants.options(ConstantCategory.RATE_TYPE));
    }

    @Test
    void categoryNotLoadedHasNoOptions() {
        Constants constants = new Constants(Map.of(ConstantCategory.RATE_TYPE, List.of("Fixed")));

        assertTrue(constants.options(ConstantCategory.LOAN_STATUS).isEmpty());
    }

    @Test
    void emptyHasNoOptions() {
        assertTrue(ConstantCategory.ALL.stream().allMatch(category -> Constants.EMPTY.options(category).isEmpty()));
    }

    @Test
    void allowsOnlyLoadedValues() {
        Constants constants = new Constants(Map.of(ConstantCategory.RATE_TYPE, List.of("Fixed", "Floating")));

        assertAll(
                () -> assertTrue(constants.allows(ConstantCategory.RATE_TYPE, Loan.RateType.FIXED)),
                () -> assertFalse(constants.allows(ConstantCategory.RATE_TYPE, Loan.RateType.INTEREST_ONLY)),
                () -> assertFalse(constants.allows(ConstantCategory.RATE_TYPE, Loan.RateType.UNKNOWN)),
                () -> assertFalse(constants.allows(ConstantCategory.LOAN_STATUS, Loan.Status.NEW))
        );
    }

    @Test
    void laterChangesToTheSourceDoNotAffectIt() {
        List<String> codes = new ArrayList<>(List.of("Fixed"));
        Constants constants = new Constants(Map.of(ConstantCategory.RATE_TYPE, codes));

        codes.add("Floating");

        assertEquals(List.of(Loan.RateType.FIXED), constants.options(ConstantCategory.RATE_TYPE));
    }
}
