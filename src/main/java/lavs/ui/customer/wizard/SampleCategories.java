package lavs.ui.customer.wizard;

import java.util.List;

/**
 * Temporary choices for the drop-downs. Replaced by the mainframe's category lists
 * (constantCategory / constantItem) once there is a service to read them.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class SampleCategories {

    static final List<String> TITLES = List.of("Mr", "Mrs", "Ms", "Miss", "Mx", "Dr");

    static final List<String> NATIONALITIES = List.of(
            "New Zealand", "Australia", "China", "Cook Islands", "Fiji", "India", "Philippines",
            "Samoa", "South Africa", "Tonga", "United Kingdom", "United States");

    private SampleCategories() {
    }
}
