package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import java.util.List;
import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Employer;

/** The Employment tab: the customer's employers. */
public class EmploymentTabController {

    @FXML private VBox employerList;
    @FXML private PagerController pagerController;

    private LongFunction<Page<Employer>> loadPage = page -> new Page<>(List.of(), 1, 1, 0);

    @FXML
    private void initialize() {
        pagerController.setOnPageRequested(this::showPage);
    }

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<Employer>> loadPage) {
        this.loadPage = loadPage;
        return showPage(1).total();
    }

    private Page<Employer> showPage(long pageNumber) {
        Page<Employer> page = loadPage.apply(pageNumber);
        RecordTiles.fill(employerList, page.items().stream()
                .map(employer -> RecordTiles.tile(employer.type().code(),
                        ProfileFormat.orNotRecorded(employer.name()), role(employer),
                        List.of(), () -> handleEditEmployer(employer)))
                .toList(), "No employers recorded.");
        pagerController.show(page);
        return page;
    }

    @FXML
    private void handleAddEmployer() {
        // Later: open the add employer form
    }

    private void handleEditEmployer(Employer employer) {
        // Later: open the edit employer form for this employer
    }

    /** e.g. "Project Manager · $98,000 a year", leaving out whichever part is missing. */
    private static String role(Employer employer) {
        String salary = employer.salary() == null ? null : ProfileFormat.money(employer.salary()) + " a year";
        if (isBlank(employer.position())) {
            return salary;
        }
        return salary == null ? employer.position() : employer.position() + " · " + salary;
    }
}
