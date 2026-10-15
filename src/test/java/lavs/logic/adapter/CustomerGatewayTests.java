package lavs.logic.adapter;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lavs.logic.model.Page;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import mainframe.client.MessageRequest;
import mainframe.client.MessageResponse;
import mainframe.client.Status;
import mainframe.client.StatusMessage;
import mainframe.client.StubConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CustomerGatewayTests {

    private static final CustomerNumber NUMBER = new CustomerNumber("C123");
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 17);
    private static final Customer.Details DETAILS =
            new Customer.Details("Ms", "Ann", "Lee", DATE_OF_BIRTH, "New Zealand");
    private static final long SIZE = 7;

    private StubConnection stub;
    private CustomerGateway gateway;

    @BeforeEach
    void setUp() {
        stub = spy(new StubConnection());
        gateway = new CustomerGateway(new MainframeAdapter(stub));
    }

    private void respondWith(Map<String, String> values) {
        stub.setResponse(new MessageResponse(new Status(1), new HashMap<>(values)));
    }

    private MessageRequest sentRequest() {
        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(stub).send(captor.capture());
        return captor.getValue();
    }

    private static Map<String, String> customerResponse(String familyName, String dateOfBirth) {
        Map<String, String> values = new HashMap<>();
        values.put("number", "C123");
        values.put("title", "Ms");
        values.put("firstName", "Ann");
        values.put("familyName", familyName);
        values.put("status", "Active");
        values.put("dateOfBirth", dateOfBirth);
        values.put("nationality", "New Zealand");
        return values;
    }

    private static String[] row(String number, String firstName, String familyName, String dateOfBirth) {
        return new String[] {number, "Mr", firstName, familyName, dateOfBirth, "Active"};
    }

    private static Map<String, String> searchResponse(long total, String[]... rows) {
        Map<String, String> values = new HashMap<>();
        values.put("page", "1");
        values.put("total", Long.toString(total));
        values.put("items", Integer.toString(rows.length));
        for (int i = 0; i < rows.length; i++) {
            String prefix = (i + 1) + ":";
            values.put(prefix + "number", rows[i][0]);
            values.put(prefix + "title", rows[i][1]);
            values.put(prefix + "firstName", rows[i][2]);
            values.put(prefix + "familyName", rows[i][3]);
            values.put(prefix + "dateOfBirth", rows[i][4]);
            values.put(prefix + "status", rows[i][5]);
        }
        return values;
    }

    private static int unreadableCode() {
        return StatusMessage.UNABLE_TO_PARSE.getCode();
    }

    @Test
    void constructorRejectsNullAdapter() {
        Assertions.assertThrows(NullPointerException.class, () -> new CustomerGateway(null));
    }

    @Test
    void searchKeepsCustomersWithTheSameNameApart() {
        respondWith(searchResponse(2,
                row("C1", "John", "Smith", "1985-03-14"),
                row("C2", "John", "Smith", "1962-11-02")));

        Page<Customer.Summary> page = gateway.search(1, "Smith", SIZE);

        Assertions.assertEquals(2, page.items().size());
        Assertions.assertEquals(new CustomerNumber("C1"), page.items().get(0).number());
        Assertions.assertEquals(LocalDate.of(1985, 3, 14), page.items().get(0).dateOfBirth());
        Assertions.assertEquals(new CustomerNumber("C2"), page.items().get(1).number());
        Assertions.assertEquals(LocalDate.of(1962, 11, 2), page.items().get(1).dateOfBirth());
    }

    @Test
    void searchLabelsThePageWithTheRequestAndTheServerTotal() {
        respondWith(searchResponse(12, row("C1", "John", "Smith", "1985-03-14")));

        Page<Customer.Summary> page = gateway.search(2, "Smith", SIZE);

        Assertions.assertEquals(2, page.page());
        Assertions.assertEquals(SIZE, page.size());
        Assertions.assertEquals(12, page.total());
    }

    @Test
    void searchWithNoMatchesReturnsAnEmptyPage() {
        respondWith(searchResponse(0));

        Page<Customer.Summary> page = gateway.search(1, "Nobody", SIZE);

        Assertions.assertTrue(page.items().isEmpty());
        Assertions.assertEquals(0, page.total());
    }

    @Test
    void searchSendsTheTermPageAndSize() {
        respondWith(searchResponse(0));

        gateway.search(3, "Smith", SIZE);

        MessageRequest request = sentRequest();
        Assertions.assertEquals("Smith", request.getValue("search"));
        Assertions.assertEquals("3", request.getValue("page"));
        Assertions.assertEquals("7", request.getValue("size"));
    }

    @Test
    void searchSkipsRowsWithoutANumber() {
        respondWith(searchResponse(2,
                row("", "Ghost", "Row", "1980-01-01"),
                row("C2", "John", "Smith", "1962-11-02")));

        Page<Customer.Summary> page = gateway.search(1, "Smith", SIZE);

        Assertions.assertEquals(1, page.items().size());
        Assertions.assertEquals(new CustomerNumber("C2"), page.items().get(0).number());
    }

    @Test
    void searchSkipsRowsWithUnreadableDates() {
        respondWith(searchResponse(2,
                row("C1", "John", "Smith", "14/03/1985"),
                row("C2", "John", "Smith", "1962-11-02")));

        Page<Customer.Summary> page = gateway.search(1, "Smith", SIZE);

        Assertions.assertEquals(1, page.items().size());
        Assertions.assertEquals(new CustomerNumber("C2"), page.items().get(0).number());
    }

    @Test
    void searchKeepsRowsWithoutADateOfBirth() {
        respondWith(searchResponse(1, row("C1", "John", "Smith", "")));

        Page<Customer.Summary> page = gateway.search(1, "Smith", SIZE);

        Assertions.assertEquals(1, page.items().size());
        Assertions.assertNull(page.items().get(0).dateOfBirth());
    }

    @Test
    void retrieveMapsTheResponseToACustomer() {
        respondWith(customerResponse("Lee", "1990-05-17"));

        Customer customer = gateway.retrieve(NUMBER);

        Assertions.assertEquals(
                new Customer(NUMBER, "Ms", "Ann", "Lee", Customer.Status.ACTIVE, DATE_OF_BIRTH, "New Zealand"),
                customer);
        Assertions.assertEquals("C123", sentRequest().getValue("number"));
    }

    @Test
    void retrieveWithoutADateOfBirthGivesANullDate() {
        respondWith(customerResponse("Lee", ""));

        Assertions.assertNull(gateway.retrieve(NUMBER).dateOfBirth());
    }

    @Test
    void retrieveWithABlankFamilyNameIsReportedAsUnreadable() {
        respondWith(customerResponse("", "1990-05-17"));

        MainframeException e = Assertions.assertThrows(MainframeException.class, () -> gateway.retrieve(NUMBER));

        Assertions.assertEquals(unreadableCode(), e.errorCode());
    }

    @Test
    void retrieveWithAnUnreadableDateIsReportedAsUnreadable() {
        respondWith(customerResponse("Lee", "17/05/1990"));

        MainframeException e = Assertions.assertThrows(MainframeException.class, () -> gateway.retrieve(NUMBER));

        Assertions.assertEquals(unreadableCode(), e.errorCode());
    }

    @Test
    void createSendsEveryFieldAndTheDefaultOrganisation() {
        respondWith(Map.of("number", "C999"));

        CustomerNumber created = gateway.create(DETAILS);

        MessageRequest request = sentRequest();
        Assertions.assertEquals(new CustomerNumber("C999"), created);
        Assertions.assertEquals("0", request.getValue("organisation"));
        Assertions.assertEquals("Ms", request.getValue("title"));
        Assertions.assertEquals("Ann", request.getValue("firstName"));
        Assertions.assertEquals("Lee", request.getValue("familyName"));
        Assertions.assertEquals("1990-05-17", request.getValue("dateOfBirth"));
        Assertions.assertEquals("New Zealand", request.getValue("nationality"));
    }

    @Test
    void createWithoutADateOfBirthSendsNoDate() {
        respondWith(Map.of("number", "C999"));

        gateway.create(new Customer.Details(null, "Ann", "Lee", null, null));

        Assertions.assertNull(sentRequest().getValue("dateOfBirth"));
    }

    @Test
    void updateSendsEveryFieldAndReturnsWhatTheMainframeStored() {
        respondWith(customerResponse("Lee-Smith", "1990-05-17"));

        Customer updated = gateway.update(NUMBER, DETAILS);

        MessageRequest request = sentRequest();
        Assertions.assertEquals("C123", request.getValue("number"));
        Assertions.assertEquals("Ms", request.getValue("title"));
        Assertions.assertEquals("Ann", request.getValue("firstName"));
        Assertions.assertEquals("Lee", request.getValue("familyName"));
        Assertions.assertEquals("1990-05-17", request.getValue("dateOfBirth"));
        Assertions.assertEquals("New Zealand", request.getValue("nationality"));
        Assertions.assertEquals("Lee-Smith", updated.familyName());
        Assertions.assertEquals(Customer.Status.ACTIVE, updated.status());
    }

    @Test
    void mainframeErrorsPassThrough() {
        stub.setResponse(new MessageResponse(new Status(3, "Customer not found", 1), new HashMap<>()));

        MainframeException e = Assertions.assertThrows(MainframeException.class, () -> gateway.update(NUMBER, DETAILS));

        Assertions.assertEquals("Customer not found", e.getMessage());
    }
}