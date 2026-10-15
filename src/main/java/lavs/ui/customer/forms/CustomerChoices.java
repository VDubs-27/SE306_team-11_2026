package lavs.ui.customer.forms;

import java.util.List;

/**
 * The choices for a customer's title and nationality. The mainframe keeps no category list for
 * either (its lists cover address, phone, email and employer types, statuses and loan values),
 * so these are kept here.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class CustomerChoices {

    static final List<String> TITLES = List.of("Mr", "Mrs", "Ms", "Miss", "Mx", "Dr");

    static final List<String> NATIONALITIES = List.of(
            "New Zealand", "Australia", "China", "Cook Islands", "Fiji", "India", "Philippines",
            "Samoa", "South Africa", "Tonga", "United Kingdom", "United States");

    private CustomerChoices() {
    }
}
