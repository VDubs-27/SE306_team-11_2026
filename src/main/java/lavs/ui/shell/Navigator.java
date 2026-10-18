package lavs.ui.shell;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import lavs.logic.model.loan.Loan;
import lavs.logic.service.Result;
import lavs.ui.common.Background;
import lavs.ui.common.Choices;
import lavs.ui.common.ErrorPanel;
import lavs.ui.common.Toasts;
import lavs.ui.customer.profile.CustomerProfileController;
import lavs.ui.customer.profile.ProfileLoader;
import lavs.ui.customer.wizard.NewCustomer;
import lavs.ui.customer.wizard.NewCustomerWizardController;
import lavs.ui.loan.people.LoanPerson;
import lavs.ui.loan.view.LoanLoader;
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
    /** True while a screen is loading before it opens, so a second click doesn't open it twice. */
    private boolean opening;

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
        // Drop-downs offer the mainframe's lists, loaded when the user signed in
        Choices.use(services.session().constants());
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
        Choices.use(null);
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
     * Reads one customer's profile from the mainframe, then shows it below the header. Until it's
     * read, the current screen stays, with the header's bar sweeping; if it can't be read, says why.
     * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
     */
    public void showCustomerProfile(String customerNumber) {
        openWhenLoaded(() -> ProfileLoader.load(services, customerNumber), "Couldn't open this customer", profile -> {
                    FXMLLoader loader = load("/lavs/ui/customer/profile/customer-profile.fxml");
                    CustomerProfileController controller = loader.getController();
                    controller.showCustomer(profile);
                    show(loader.getRoot());
                });
    }

    /**
     * Shows the new customer wizard below the header.
     * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
     */
    public void showNewCustomerWizard() {
        showNewCustomerWizard(null);
    }

    /** Shows the new customer wizard; once the customer is saved, comes back here and runs onSaved. */
    public void showNewCustomerWizard(Consumer<NewCustomer> onSaved) {
        FXMLLoader loader = load("/lavs/ui/customer/wizard/new-customer-wizard.fxml");
        NewCustomerWizardController controller = loader.getController();
        controller.setOnSaved(onSaved);
        show(loader.getRoot());
    }

    /**
     * Shows the new loan wizard for a customer below the header.
     * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
     */
    public void showNewLoanWizard(LoanPerson applicant) {
        FXMLLoader loader = load("/lavs/ui/loan/wizard/new-loan-wizard.fxml");
        NewLoanWizardController controller = loader.getController();
        controller.start(applicant);
        show(loader.getRoot());
    }

    /**
     * Shows one of a customer's loans below the header.
     * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
     */
    public void showLoan(Loan loan, LoanPerson applicant) {
        openWhenLoaded(() -> LoanLoader.load(services, loan, applicant), "Couldn't open this loan", screen -> {
            FXMLLoader loader = load("/lavs/ui/loan/view/loan-view.fxml");
            LoanViewController controller = loader.getController();
            controller.showLoan(screen);
            show(loader.getRoot());
        });
    }

    /**
     * Shows that a screen couldn't load, in its place below the header: what failed and why, with
     * Try again (retry, e.g. load it again) and Back. For loads that fail, e.g. the mainframe is down.
     */
    public void showLoadFailed(String title, String message, Runnable retry) {
        show(ErrorPanel.create(title, message, retry, this::back));
    }

    /**
     * Loads what a screen needs in the background, then shows it with show. While loading, another
     * open is ignored; if loading fails, a message titled failTitle says why and nothing opens.
     */
    private <T> void openWhenLoaded(Supplier<Result<T>> loadData, String failTitle, Consumer<T> show) {
        if (opening) {
            return;
        }
        opening = true;
        Background.run(loadData, loaded -> {
            opening = false;
            if (loaded.successful()) {
                show.accept(loaded.value());
            } else {
                Toasts.show(stage.getScene(), Toasts.Kind.ERROR, failTitle, loaded.errorMessage());
            }
        });
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