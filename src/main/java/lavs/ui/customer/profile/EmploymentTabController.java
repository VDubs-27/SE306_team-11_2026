package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Employer;
import lavs.ui.common.EmployerRows;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

/**
 * The Employment tab: the customer's employers.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class EmploymentTabController {

    @FXML private VBox employerList;
    @FXML private PagerController pagerController;

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<Employer>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(employerList, items.stream()
                .map(employer -> EmployerRows.tile(employer.details(), () -> handleEditEmployer(employer)))
                .toList(), "No employers recorded."));
    }

    @FXML
    private void handleAddEmployer() {
        // Later: open the add employer form
    }

    private void handleEditEmployer(Employer employer) {
        // Later: open the edit employer form for this employer
    }
}
