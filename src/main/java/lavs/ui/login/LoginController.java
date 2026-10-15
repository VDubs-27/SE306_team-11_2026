package lavs.ui.login;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import lavs.logic.service.LoginResult;
import lavs.ui.common.FieldErrors;
import lavs.ui.common.LoadingBar;
import lavs.ui.common.Nodes;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;

/**
 * The login screen: a username and password, checked by the mainframe. Signing in happens off the
 * screen's thread, so the window stays responsive; on success the app opens on customer search.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class LoginController implements NavigatorAware {

    /** An open eye, and the same eye struck through once the password is showing. */
    private static final String EYE = "M1 6C3 2.5 5.5 1 8 1s5 1.5 7 5c-2 3.5-4.5 5-7 5S3 9.5 1 6z"
            + "M8 3.6a2.4 2.4 0 1 0 0 4.8a2.4 2.4 0 1 0 0-4.8z";
    private static final String EYE_STRUCK = EYE + "M2 0.5L14 11.5";
    private static final String UNEXPECTED = "Something went wrong while signing in. Try again.";

    @FXML private TextField userNameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordShownField;
    @FXML private ToggleButton showPasswordButton;
    @FXML private SVGPath eyeIcon;
    @FXML private Label userNameError;
    @FXML private Label passwordError;
    @FXML private HBox errorBanner;
    @FXML private Label errorText;
    @FXML private Button signInButton;
    @FXML private Pane topBar;

    private Navigator navigator;
    private FieldErrors errors;
    private LoadingBar loadingBar;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        // Two boxes share the password: dots normally, plain text while the eye is on
        passwordShownField.textProperty().bindBidirectional(passwordField.textProperty());
        Nodes.show(passwordShownField, false);
        eyeIcon.setContent(EYE);
        errors = new FieldErrors()
                .add("userName", userNameField, userNameError)
                .add("password", passwordField, passwordError);
        Nodes.show(errorBanner, false);
        loadingBar = new LoadingBar(topBar);
        Platform.runLater(userNameField::requestFocus);
    }

    @FXML
    private void handleShowPassword() {
        boolean showing = showPasswordButton.isSelected();
        Nodes.show(passwordShownField, showing);
        Nodes.show(passwordField, !showing);
        eyeIcon.setContent(showing ? EYE_STRUCK : EYE);
        TextField visible = showing ? passwordShownField : passwordField;
        visible.requestFocus();
        // Focusing selects from the start, so the cursor is moved to the end once that's done
        Platform.runLater(() -> visible.positionCaret(visible.getText().length()));
    }

    @FXML
    private void handleSignIn() {
        Nodes.show(errorBanner, false);
        String userName = userNameField.getText().trim();
        String password = passwordField.getText();
        setBusy(true);
        Task<LoginResult> signIn = new Task<>() {
            @Override
            protected LoginResult call() {
                return navigator.services().login().login(userName, password);
            }
        };
        signIn.setOnSucceeded(event -> finish(signIn.getValue()));
        signIn.setOnFailed(event -> {
            setBusy(false);
            showError(UNEXPECTED);
        });
        Thread thread = new Thread(signIn, "sign-in");
        thread.setDaemon(true);
        thread.start();
    }

    /** Opens the app, or shows what's wrong: missing fields in place, a refusal above the fields. */
    private void finish(LoginResult result) {
        setBusy(false);
        if (result.successful()) {
            navigator.showShell();
            navigator.showCustomerSearch();
            return;
        }
        errors.show(result.fieldErrors());
        if (result.errorMessage() != null) {
            showError(result.errorMessage());
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        Nodes.show(errorBanner, true);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            loadingBar.start();
        } else {
            loadingBar.stop();
        }
        signInButton.setDisable(busy);
        signInButton.setText(busy ? "Signing in…" : "Sign in");
        userNameField.setDisable(busy);
        passwordField.setDisable(busy);
        passwordShownField.setDisable(busy);
    }
}
