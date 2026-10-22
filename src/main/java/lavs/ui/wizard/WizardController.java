package lavs.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * The frame shared by every wizard (new customer, new loan): a header with a stepper, the current
 * step, and Back/Next along the bottom. A specific wizard gives it the steps and says what Cancel
 * and Finish do; the frame handles moving between steps.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class WizardController {

    private static final String DONE = "step-done";
    private static final String CURRENT = "step-current";

    @FXML private ScrollPane scrollPane;
    @FXML private StackPane iconHolder;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private HBox stepper;
    @FXML private StackPane stepArea;
    @FXML private Button backButton;
    @FXML private Button nextButton;

    private final List<Label> stepCircles = new ArrayList<>();
    private final List<Label> stepLabels = new ArrayList<>();
    private final List<Region> stepLines = new ArrayList<>();
    private List<WizardStep> steps = List.of();
    private int current;
    private String finishText = "Finish";
    private Runnable onFinish = () -> { };
    private Runnable onCancel = () -> { };

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    /** The picture beside the title, e.g. an icon for a new customer or the customer's initials for a loan. */
    public void setIcon(Node icon) {
        iconHolder.getChildren().setAll(icon);
    }

    /** Text on the last step's button, e.g. "Save customer". */
    public void setFinishText(String finishText) {
        this.finishText = finishText;
        update();
    }

    /** Runs when the last step passes its check and the user finishes. */
    public void setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    /** Runs when the user cancels from the first step; the wizard decides whether to confirm first. */
    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    /**
     * Locks the wizard while the finish is being saved, e.g. to the mainframe: the step and both
     * buttons can't be used, and the finish button says busyText, e.g. "Saving…".
     */
    public void setBusy(String busyText) {
        lock(true);
        nextButton.setText(busyText);
    }

    /** Unlocks the wizard after setBusy, e.g. when saving failed and the user can try again. */
    public void clearBusy() {
        lock(false);
        update();
    }

    private void lock(boolean locked) {
        stepArea.setDisable(locked);
        stepper.setDisable(locked);
        backButton.setDisable(locked);
        nextButton.setDisable(locked);
    }

    /** Sets the steps in order and shows the first. */
    public void setSteps(List<WizardStep> steps) {
        this.steps = List.copyOf(steps);
        buildStepper();
        showStep(0);
    }

    /**
     * Jumps back to an earlier step, e.g. from a "Change" link on a review. Going forward isn't
     * allowed this way, so each step's check still runs before moving past it.
     */
    public void goBackTo(int index) {
        if (index >= 0 && index < current) {
            showStep(index);
        }
    }

    /**
     * Moves on to the next step without checking the current one again, for a step whose check
     * finishes in the background, e.g. once the mainframe has accepted what was entered.
     */
    public void advance() {
        if (current < steps.size() - 1) {
            showStep(current + 1);
        }
    }

    @FXML
    private void handleBack() {
        if (current == 0) {
            onCancel.run();
        } else {
            showStep(current - 1);
        }
    }

    @FXML
    private void handleNext() {
        if (!steps.get(current).check().getAsBoolean()) {
            return;
        }
        if (current == steps.size() - 1) {
            onFinish.run();
        } else {
            showStep(current + 1);
        }
    }

    private void showStep(int index) {
        current = index;
        WizardStep step = steps.get(index);
        step.onShow().run();
        stepArea.getChildren().setAll(step.content());
        update();
        scrollPane.setVvalue(0);
    }

    /** One circle and label per step, with a line between each. */
    private void buildStepper() {
        stepper.getChildren().clear();
        stepCircles.clear();
        stepLabels.clear();
        stepLines.clear();
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                Region line = new Region();
                line.getStyleClass().add("step-line");
                HBox.setHgrow(line, Priority.ALWAYS);
                stepLines.add(line);
                stepper.getChildren().add(line);
            }
            Label circle = new Label();
            circle.getStyleClass().add("step-circle");
            Label label = new Label(steps.get(i).title());
            label.getStyleClass().add("step-label");
            label.setMinWidth(Region.USE_PREF_SIZE);
            stepCircles.add(circle);
            stepLabels.add(label);
            HBox item = new HBox(8, circle, label);
            item.getStyleClass().add("step-item");
            stepper.getChildren().add(item);
        }
    }

    /** Brings the stepper and the buttons in line with the current step. */
    private void update() {
        for (int i = 0; i < stepCircles.size(); i++) {
            boolean done = i < current;
            boolean isCurrent = i == current;
            Label circle = stepCircles.get(i);
            circle.setText(done ? "✓" : String.valueOf(i + 1));
            setStyle(circle, DONE, done);
            setStyle(circle, CURRENT, isCurrent);
            setStyle(stepLabels.get(i), CURRENT, isCurrent);
        }
        for (int i = 0; i < stepLines.size(); i++) {
            setStyle(stepLines.get(i), DONE, i < current);
        }
        if (steps.isEmpty()) {
            return;
        }
        boolean last = current == steps.size() - 1;
        backButton.setText(current == 0 ? "Cancel" : "←  Back");
        nextButton.setText(last ? "✓  " + finishText : "Next: " + steps.get(current + 1).title() + "  →");
    }

    private static void setStyle(Node node, String styleClass, boolean on) {
        node.getStyleClass().remove(styleClass);
        if (on) {
            node.getStyleClass().add(styleClass);
        }
    }
}
