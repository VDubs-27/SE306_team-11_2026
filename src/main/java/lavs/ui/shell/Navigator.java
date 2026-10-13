package lavs.ui.shell;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Owns all screen switching. Controllers ask the navigator to move;
 * they never load FXML themselves.
 */
public class Navigator {

    private final Stage stage;
    private final Deque<Parent> history = new ArrayDeque<>();
    private ShellController shell;
    private Parent currentScreen;

    public Navigator(Stage stage) {
        this.stage = stage;
    }

    /** Replaces the whole window with the shell (header + empty content area). */
    public void showShell() {
        FXMLLoader loader = load("/lavs/ui/shell/shell.fxml");
        shell = loader.getController();
        stage.getScene().setRoot(loader.getRoot());
        history.clear();
        currentScreen = null;
    }

    /** Replaces the whole window with the login screen (no header). */
    public void showLogin() {
        // Your teammate's screen. Until it exists, just print a message.
        // Later: load the login FXML and setRoot, like showShell().
        System.out.println("will load login");
    }

    /** Shows customer search below the header. */
    public void showCustomerSearch() {
        FXMLLoader loader = load("/lavs/ui/customer/placeholder.fxml");
        show(loader.getRoot());
    }

    /** Returns to the previous screen, if there is one. */
    public void back() {
        if (history.isEmpty()) {
            return;
        }
        currentScreen = history.pop();
        shell.setContent(currentScreen);
    }

    /** Shows a screen below the header, remembering the current one for back(). */
    private void show(Parent screen) {
        if (currentScreen != null) {
            history.push(currentScreen);
        }
        currentScreen = screen;
        shell.setContent(screen);
    }

    /** Loads an FXML file and gives its controller this navigator, if it wants one. */
    private FXMLLoader load(String path) {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                getClass().getResource(path), "FXML not found: " + path));
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load " + path, e);
        }
        if (loader.getController() instanceof NavigatorAware aware) {
            aware.setNavigator(this);
        }
        return loader;
    }
}