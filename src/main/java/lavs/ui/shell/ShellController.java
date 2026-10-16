package lavs.ui.shell;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import lavs.ui.common.DisplayFormat;

public class ShellController implements NavigatorAware {

    @FXML private StackPane contentArea;
    @FXML private Button accountButton;
    @FXML private Label accountInitials;

    private final ContextMenu accountMenu = new ContextMenu();
    private Navigator navigator;

    @FXML
    private void initialize() {
        MenuItem logout = new MenuItem("Log out");
        logout.setOnAction(event -> handleLogout());
        accountMenu.getItems().add(logout);
        accountMenu.setOnShown(event -> {
            Region content = (Region) accountMenu.getSkin().getNode();
            content.setMinWidth(accountButton.getWidth());
        });
    }

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    /** Shows who is signed in on the account button, e.g. "bob.ross" as Bob Ross with BR. */
    public void showUser(String userName) {
        accountButton.setText(DisplayFormat.userName(userName));
        accountInitials.setText(DisplayFormat.userInitials(userName));
    }

    /** Replaces whatever screen is showing below the header. */
    public void setContent(Parent screen) {
        contentArea.getChildren().setAll(screen);
    }

    /** Opens the account menu directly below the button, at the same width. */
    @FXML
    private void handleAccountMenu() {
        if (accountMenu.isShowing()) {
            accountMenu.hide();
            return;
        }
        accountMenu.show(accountButton, Side.BOTTOM, 0, 4);
    }

    private void handleLogout() {
        navigator.logOut();
    }
}