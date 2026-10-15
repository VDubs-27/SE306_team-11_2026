package lavs.ui.customer.wizard;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Employer;
import lavs.ui.common.EditableList;
import lavs.ui.common.EmployerRows;
import lavs.ui.common.RecordEditor;
import lavs.ui.customer.forms.EmployerFormController;

/**
 * Step 4 of the new customer wizard: whether the customer is employed and, if so, who by.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class EmploymentStepController {

    @FXML private Node employerForm;
    @FXML private EmployerFormController employerFormController;
    @FXML private ToggleGroup employmentChoice;
    @FXML private ToggleButton employedButton;
    @FXML private VBox employedSection;
    @FXML private VBox employerList;
    @FXML private HBox errorBanner;

    private NewCustomerDraft draft;
    private EditableList<Employer.Details> employers;

    @FXML
    private void initialize() {
        show(errorBanner, false);
        // One primary employer: saving another as primary makes the old one secondary
        employers = new EditableList<>(
                new RecordEditor<>(employerList, "employer", employerForm, employerFormController),
                employerList, EmployerRows::tile, "No employers added yet.")
                .keepOnlyOne(employer -> employer.type() == Employer.Type.PRIMARY,
                        employer -> new Employer.Details(Employer.Type.SECONDARY, employer.name(),
                                employer.position(), employer.salary()))
                .onChanged(this::refresh);
        employedButton.setSelected(true);
        // One of the two is always chosen; clicking the chosen one again leaves it chosen
        employmentChoice.selectedToggleProperty().addListener((obs, oldChoice, choice) -> {
            if (choice == null) {
                oldChoice.setSelected(true);
                return;
            }
            draft.setEmployed(choice == employedButton);
            refresh();
        });
    }

    /** Gives the step the draft whose employment it edits. */
    void use(NewCustomerDraft draft) {
        this.draft = draft;
        employers.use(draft.employers());
        refresh();
    }

    /** Not employed, or employed with at least one employer; otherwise shows what's missing. */
    boolean check() {
        boolean ok = !draft.isEmployed() || !draft.employers().isEmpty();
        show(errorBanner, !ok);
        return ok;
    }

    @FXML
    private void handleAddEmployer() {
        employers.add();
    }

    private void refresh() {
        show(employedSection, draft.isEmployed());
        if (!draft.isEmployed() || !draft.employers().isEmpty()) {
            show(errorBanner, false);
        }
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
