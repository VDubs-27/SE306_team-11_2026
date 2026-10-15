package lavs.ui.customer.wizard;

import java.util.List;
import java.util.stream.IntStream;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Employer;
import lavs.ui.common.EmployerRows;
import lavs.ui.common.FormDialog;
import lavs.ui.common.RecordTiles;
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

    @FXML
    private void initialize() {
        show(errorBanner, false);
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
        List<Employer.Details> employers = draft.employers();
        employerFormController.clear(employers.isEmpty());
        if (openForm("Add employer", false) == FormDialog.Result.SAVED) {
            employers.add(employerFormController.read());
            keepOnePrimary(employers.size() - 1);
        }
        refresh();
    }

    private void handleEditEmployer(int index) {
        List<Employer.Details> employers = draft.employers();
        employerFormController.fill(employers.get(index));
        FormDialog.Result result = openForm("Edit employer", true);
        if (result == FormDialog.Result.SAVED) {
            employers.set(index, employerFormController.read());
            keepOnePrimary(index);
        } else if (result == FormDialog.Result.REMOVED) {
            employers.remove(index);
        }
        refresh();
    }

    private FormDialog.Result openForm(String title, boolean canRemove) {
        return FormDialog.show(employerList, title, employerForm, employerFormController::check, canRemove);
    }

    /** One primary employer: when the one just saved is primary, any other becomes secondary. */
    private void keepOnePrimary(int savedIndex) {
        List<Employer.Details> employers = draft.employers();
        if (employers.get(savedIndex).type() != Employer.Type.PRIMARY) {
            return;
        }
        for (int i = 0; i < employers.size(); i++) {
            Employer.Details employer = employers.get(i);
            if (i != savedIndex && employer.type() == Employer.Type.PRIMARY) {
                employers.set(i, new Employer.Details(Employer.Type.SECONDARY, employer.name(),
                        employer.position(), employer.salary()));
            }
        }
    }

    private void refresh() {
        List<Employer.Details> employers = draft.employers();
        RecordTiles.fill(employerList, IntStream.range(0, employers.size())
                .mapToObj(i -> EmployerRows.tile(employers.get(i), () -> handleEditEmployer(i)))
                .toList(), "No employers added yet.");
        show(employedSection, draft.isEmployed());
        if (!draft.isEmployed() || !employers.isEmpty()) {
            show(errorBanner, false);
        }
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
