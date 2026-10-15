package lavs.logic.adapter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Phone;
import lavs.logic.model.loan.Loan;
import mainframe.client.MessageRequest;
import org.junit.jupiter.api.Test;

class ConstantsReaderTests {

    @Test
    void loadsEveryCategoryWithItsItems() {
        ConstantsConnection connection = new ConstantsConnection()
                .category("CustomerPhoneNumberType", "Home", "Mobile", "Work", "Other")
                .category("LoanRateType", "Fixed", "Floating", "InterestOnly");

        Constants constants = new MainframeAdapter(connection).loadConstants();

        assertAll(
                () -> assertEquals(List.of(Phone.Type.HOME, Phone.Type.MOBILE, Phone.Type.WORK, Phone.Type.OTHER),
                        constants.options(ConstantCategory.PHONE_TYPE)),
                () -> assertEquals(List.of(Loan.RateType.FIXED, Loan.RateType.FLOATING, Loan.RateType.INTEREST_ONLY),
                        constants.options(ConstantCategory.RATE_TYPE))
        );
    }

    @Test
    void sortsItemsByTheirMainframeOrder() {
        ConstantsConnection connection = new ConstantsConnection()
                .item("CustomerAddressType", "Postal", 2)
                .item("CustomerAddressType", "Other", 5)
                .item("CustomerAddressType", "Residential", 1);

        Constants constants = new MainframeAdapter(connection).loadConstants();

        assertEquals(List.of(Address.Type.RESIDENTIAL, Address.Type.POSTAL, Address.Type.OTHER),
                constants.options(ConstantCategory.ADDRESS_TYPE));
    }

    @Test
    void readsEveryPageOfCategoriesAndItems() {
        ConstantsConnection connection = new ConstantsConnection()
                .category("CustomerStatus", "Active", "Inactive")
                .category("CustomerPhoneNumberType", "Home", "Mobile", "Work", "Other")
                .category("LoanRateType", "Fixed", "Floating", "InterestOnly")
                .pageSize(2);

        Constants constants = new MainframeAdapter(connection).loadConstants();

        assertAll(
                () -> assertEquals(4, constants.options(ConstantCategory.PHONE_TYPE).size()),
                () -> assertEquals(3, constants.options(ConstantCategory.RATE_TYPE).size()),
                () -> assertEquals(2, constants.options(ConstantCategory.CUSTOMER_STATUS).size())
        );
    }

    @Test
    void asksForPagesFromOneWithTheReaderPageSize() {
        ConstantsConnection connection = new ConstantsConnection().category("LoanRateType", "Fixed");

        new MainframeAdapter(connection).loadConstants();

        MessageRequest first = connection.constantRequests().get(0);
        assertAll(
                () -> assertEquals("1", first.getValue("page")),
                () -> assertEquals(String.valueOf(ConstantsReader.PAGE_SIZE), first.getValue("size"))
        );
    }

    @Test
    void skipsItemsOfCategoriesLavsDoesNotUse() {
        ConstantsConnection connection = new ConstantsConnection()
                .category("CustomerRelationshipType", "Parent", "Spouse")
                .category("LoanRateType", "Fixed");

        new MainframeAdapter(connection).loadConstants();

        List<String> itemRequests = connection.constantRequests().stream()
                .filter(request -> request.getRequestType().equals("constantItem.list"))
                .map(request -> request.getValue("category"))
                .toList();
        assertEquals(List.of("2"), itemRequests);
    }

    @Test
    void stopsAtAnEmptyPageEvenIfTheTotalSaysThereAreMore() {
        ConstantsConnection connection = new ConstantsConnection()
                .category("LoanRateType", "Fixed")
                .reportCategoryTotal(50);

        Constants constants = new MainframeAdapter(connection).loadConstants();

        assertEquals(List.of(Loan.RateType.FIXED), constants.options(ConstantCategory.RATE_TYPE));
    }

    @Test
    void categoryListFailureIsThrown() {
        ConstantsConnection connection = new ConstantsConnection().failCategoryList(3, "Invalid transaction number");

        MainframeException e = assertThrows(MainframeException.class,
                () -> new MainframeAdapter(connection).loadConstants());

        assertEquals("Invalid transaction number", e.getMessage());
    }

    @Test
    void itemListFailureIsThrown() {
        ConstantsConnection connection = new ConstantsConnection()
                .category("LoanRateType", "Fixed")
                .failItemList(10, "Unavailable");

        MainframeException e = assertThrows(MainframeException.class,
                () -> new MainframeAdapter(connection).loadConstants());

        assertTrue(e.isNetworkFailure());
    }
}
