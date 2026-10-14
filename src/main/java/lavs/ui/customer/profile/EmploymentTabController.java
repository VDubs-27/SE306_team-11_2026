package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Employer;
import lavs.ui.common.EmployerRows;
import lavs.ui.common.FormDialog;
import lavs.ui.common.PagerController;
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

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<Employer>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(employerList, items.stream()
                .map(employer -> EmployerRows.tile(employer.details(), () -> handleEditEmployer(employer)))
                .toList(), "No employers recorded."));
    }

    @FXML
    private void handleAddEmployer() {
        employerFormController.clear(false);
        if (FormDialog.show(employerList, "Add employer", employerForm, employerFormController::check, false)
                == FormDialog.Result.SAVED) {
            // Later: CustomerService.addEmployer(customerNumber, employerFormController.read()), then reload the tab
        }
    }

    private void handleEditEmployer(Employer employer) {
        employerFormController.fill(employer.details());
        switch (FormDialog.show(employerList, "Edit employer", employerForm, employerFormController::check, true)) {
            case SAVED -> {
                // Later: CustomerService.updateEmployer(employer.id(), employerFormController.read()), then reload the tab
            }
            case REMOVED -> {
                // Later: CustomerService.removeEmployer(employer.id()), then reload the tab
            }
            default -> {
                // Cancelled: nothing to do
            }
        }
    }
}
