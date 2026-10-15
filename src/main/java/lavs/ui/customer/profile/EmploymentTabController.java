package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Employer;
import lavs.ui.common.EmployerRows;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordEditor;
import lavs.ui.common.RecordTiles;
import lavs.ui.customer.forms.EmployerFormController;

/**
 * The Employment tab: the customer's employers. Add and Edit use the same employer pop-up as the
 * new customer wizard.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class EmploymentTabController {

    @FXML private VBox employerList;
    @FXML private PagerController pagerController;
    @FXML private Node employerForm;
    @FXML private EmployerFormController employerFormController;

    private RecordEditor<Employer.Details> editor;

    @FXML
    private void initialize() {
        editor = new RecordEditor<>(employerList, "employer", employerForm, employerFormController);
    }

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<Employer>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(employerList, items.stream()
                .map(employer -> EmployerRows.tile(employer.details(), () -> handleEditEmployer(employer)))
                .toList(), "No employers recorded."));
    }

    @FXML
    private void handleAddEmployer() {
        editor.add(false, employer -> {
            // Later: CustomerService.addEmployer(customerNumber, employer), then reload the tab
        });
    }

    private void handleEditEmployer(Employer employer) {
        editor.edit(employer.details(), changed -> {
            // Later: CustomerService.updateEmployer(employer.id(), changed), then reload the tab
        }, () -> {
            // Later: CustomerService.removeEmployer(employer.id()), then reload the tab
        });
    }
}
