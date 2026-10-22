package lavs.ui.customer.profile;

import java.util.function.LongConsumer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Employer;
import lavs.ui.common.EmployerRows;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordEditor;
import lavs.ui.common.ServiceList;
import lavs.ui.customer.forms.EmployerFormController;

/**
 * The Employment tab: one tile per employer, a page at a time from the mainframe. Add and Edit use
 * the same employer pop-up as the new customer wizard.
 * AI-assisted: written by Jack, refined with Claude.
 */
public class EmploymentTabController {

    private static final long PAGE_SIZE = 5;

    @FXML private VBox employerList;
    @FXML private PagerController pagerController;
    @FXML private Node employerForm;
    @FXML private EmployerFormController employerFormController;

    private ServiceList<Employer, Employer.Details> employers;

    @FXML
    private void initialize() {
        employers = new ServiceList<>(
                new RecordEditor<>(employerList, "employer", employerForm, employerFormController),
                employerList, pagerController, PAGE_SIZE,
                (employer, onEdit) -> EmployerRows.tile(employer.details(), onEdit), Employer::details,
                "No employers recorded.");
    }

    /** Shows the customer's employers from this source; onTotal hears how many there are, for the tab's count. */
    void show(ServiceList.Source<Employer, Employer.Details> source, LongConsumer onTotal) {
        employers.onTotal(onTotal).show(source);
    }

    @FXML
    private void handleAddEmployer() {
        employers.add();
    }
}
