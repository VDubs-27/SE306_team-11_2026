package lavs.ui.shell;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import lavs.logic.model.customer.Customer;
import lavs.logic.model.loan.Loan;
import lavs.ui.common.Toasts;
import lavs.ui.customer.profile.CustomerProfileController;
import lavs.ui.loan.view.LoanViewController;
import lavs.ui.loan.wizard.NewLoanWizardController;

/**
 * Owns all screen switching. Controllers ask the navigator to move;
 * they never load FXML themselves.
 */
public class Navigator {

    private final Stage stage;
    private final Services services;
    /** The window's root for good: the current screen first, then the layer that short messages appear in. */
    private final StackPane windowRoot = new StackPane();
    private final Deque<Parent> history = new ArrayDeque<>();
    private ShellController shell;
    private Parent currentScreen;

    public Navigator(Stage stage, Services services) {
        this.stage = stage;
        this.services = services;
        windowRoot.getChildren().add(new Region());
        stage.getScene().setRoot(windowRoot);
        Toasts.install(stage.getScene(), windowRoot);
    }

    /** The services built at start-up, for screens that need them. */
    public Services services() {
        return services;
    }

    /** Replaces the whole window with the shell (header + empty content area). */
    public void showShell() {
        FXMLLoader loader = load("/lavs/ui/shell/shell.fxml");
        shell = loader.getController();
        if (services.session().isLoggedIn()) {
            shell.showUser(services.session().currentUser());
        }
        showWindow(loader.getRoot());
        history.clear();
        currentScreen = null;
    }

    /**
     * Replaces the whole window with the login screen (no header), forgetting the screens before it.
     * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
     */
    public void showLogin() {
        FXMLLoader loader = load("/lavs/ui/login/login.fxml");
        showWindow(loader.getRoot());
        shell = null;
        history.clear();
        currentScreen = null;
    }

    /** Signs out of the mainframe and goes back to the login screen, saying it worked. */
    public void logOut() {
        services.login().logout();
        showLogin();
        Toasts.show(stage.getScene(), Toasts.Kind.SUCCESS, "Signed out",
                "You've signed out of LAVS. Sign in again to carry on.");
    }

    /** Shows customer search below the header. */
    public void showCustomerSearch() {
        FXMLLoader loader = load("/lavs/ui/customer/customer-search.fxml");
        show(loader.getRoot());
    }

    /**
     * Shows one customer's profile below the header.
     * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
     */
    public void showCustomerProfile(String customerNumber) {
        FXMLLoader loader = load("/lavs/ui/customer/profile/customer-profile.fxml");
        CustomerProfileController controller = loader.getController();
        controller.showCustomer(customerNumber);
        show(loader.getRoot());
    }

    /**
     * Shows the new customer wizard below the header.
     * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
     */
    public void showNewCustomerWizard() {
        FXMLLoader loader = load("/lavs/ui/customer/wizard/new-customer-wizard.fxml");
        show(loader.getRoot());
    }

    /**
     * Shows the new loan wizard for a customer below the header.
     * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
     */
    public void showNewLoanWizard(Customer customer) {
        FXMLLoader loader = load("/lavs/ui/loan/wizard/new-loan-wizard.fxml");
        NewLoanWizardController controller = loader.getController();
        controller.start(customer);
        show(loader.getRoot());
    }

    /**
     * Shows one of a customer's loans below the header.
     * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
     */
    public void showLoan(Loan loan, Customer customer) {
        FXMLLoader loader = load("/lavs/ui/loan/view/loan-view.fxml");
        LoanViewController controller = loader.getController();
        controller.showLoan(loan, customer);
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

    /** Fills the whole window with a screen (the shell, or the login screen), under any messages. */
    private void showWindow(Parent screen) {
        windowRoot.getChildren().set(0, screen);
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