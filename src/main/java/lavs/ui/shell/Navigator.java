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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import lavs.logic.model.loan.LoanNumber;
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
    /** Which screen the user is on, so a load that finishes late can be told from one they still want. */
    private final ScreenTracker tracker = new ScreenTracker();
    private ShellController shell;
    private Parent currentScreen;

    public Navigator(Stage stage, Services services) {
        this.stage = stage;
        this.services = services;
        windowRoot.getChildren().add(new StackPane());
        stage.getScene().setRoot(windowRoot);
        Toasts.install(stage.getScene(), windowRoot);
    }

    /** The services built at start-up, for screens that need them. */
    public Services services() {
        return services;
    }

    /**
     * Where the user is now. A background call can keep this and ask isAt when it finishes, to see
     * whether they have moved to another screen since.
     */
    public long position() {
        return tracker.position();
    }

    /** True if the user has not moved to another screen since position() gave this. */
    public boolean isAt(long position) {
        return tracker.isAt(position);
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
        tracker.forgetAll();
        tracker.moved();
    }

    /**
     * Replaces the whole window with the login screen (no header), forgetting the screens before it.
     * AI-assisted: written by Jack, refined with Claude.
     */
    public void showLogin() {
        FXMLLoader loader = load("/lavs/ui/login/login.fxml");
        showWindow(loader.getRoot());
        shell = null;
        history.clear();
        currentScreen = null;
        tracker.forgetAll();
        tracker.moved();
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
     * AI-assisted: written by Jack, refined with Claude.
     */
    public void showCustomerProfile(String customerNumber) {
        openCustomerProfile(customerNumber, this::show, () -> { });
    }

    /**
     * Opens a customer's profile in place of the current screen, such as the wizard that just saved them,
     * so Back from the profile skips the wizard. The current screen stays until the profile is read; if
     * it can't be, goes back instead.
     */
    public void showCustomerProfileInstead(String customerNumber) {
        openCustomerProfile(customerNumber, this::replace, this::back);
    }

    private void openCustomerProfile(String customerNumber, Consumer<Parent> place, Runnable onFailed) {
        openWhenLoaded(() -> ProfileLoader.load(services, customerNumber), "Couldn't open this customer", onFailed,
                profile -> {
                    FXMLLoader loader = load("/lavs/ui/customer/profile/customer-profile.fxml");
                    CustomerProfileController controller = loader.getController();
                    controller.showCustomer(profile);
                    place.accept(loader.getRoot());
                });
    }

    /**
     * Shows the new customer wizard below the header.
     * AI-assisted: written by Jack, refined with Claude.
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
     * AI-assisted: written by Jack, refined with Claude.
     */
    public void showNewLoanWizard(LoanPerson applicant) {
        FXMLLoader loader = load("/lavs/ui/loan/wizard/new-loan-wizard.fxml");
        NewLoanWizardController controller = loader.getController();
        controller.start(applicant);
        show(loader.getRoot());
    }

    /**
     * Shows one of a customer's loans below the header. The loan is read again first, so the screen
     * shows its current status.
     * AI-assisted: written by Jack, refined with Claude.
     */
    public void showLoan(LoanNumber loanNumber, LoanPerson applicant) {
        openLoan(loanNumber, applicant, this::show, () -> { });
    }

    /**
     * Opens a loan in place of the current screen, such as the wizard that just made it, so Back from
     * the loan skips the wizard. The current screen stays until the loan is read; if it can't be, goes
     * back instead, where the screen before reads its loans again.
     */
    public void showLoanInstead(LoanNumber loanNumber, LoanPerson applicant) {
        openLoan(loanNumber, applicant, this::replace, this::back);
    }

    private void openLoan(LoanNumber loanNumber, LoanPerson applicant, Consumer<Parent> place, Runnable onFailed) {
        openWhenLoaded(() -> LoanLoader.load(services, loanNumber, applicant), "Couldn't open this loan", onFailed,
                screen -> {
                    FXMLLoader loader = load("/lavs/ui/loan/view/loan-view.fxml");
                    LoanViewController controller = loader.getController();
                    controller.showLoan(screen);
                    place.accept(loader.getRoot());
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
     * open from the same screen is ignored; if loading fails, a message titled failTitle says why,
     * nothing opens, and onFailed runs. If the user moves to another screen (or signs out) while it
     * loads, the result is dropped, so a screen never opens over somewhere the user has since gone.
     */
    private <T> void openWhenLoaded(Supplier<Result<T>> loadData, String failTitle, Runnable onFailed,
                                    Consumer<T> show) {
        if (shell == null) {
            return; // signed out: there is no header to open a screen under
        }
        long ticket = tracker.startLoad();
        if (ticket == ScreenTracker.NO_LOAD) {
            return;
        }
        Background.run(loadData, loaded -> {
            if (!tracker.finishLoad(ticket)) {
                return;
            }
            if (loaded.successful()) {
                show.accept(loaded.value());
            } else {
                Toasts.show(stage.getScene(), Toasts.Kind.ERROR, failTitle, loaded.errorMessage());
                onFailed.run();
            }
        });
    }

    /**
     * Returns to the previous screen, if there is one. A screen that can go out of date (see
     * Refreshable) reads again what may have changed while another was open on top of it.
     */
    public void back() {
        if (history.isEmpty()) {
            return;
        }
        tracker.forget(currentScreen);
        currentScreen = history.pop();
        tracker.moved();
        shell.setContent(currentScreen);
        tracker.refresh(currentScreen);
    }

    /** Fills the whole window with a screen (the shell, or the login screen), under any messages. */
    private void showWindow(Parent screen) {
        windowRoot.getChildren().set(0, screen);
    }

    /** Shows a screen below the header, remembering the current one for back(). */
    private void show(Parent screen) {
        if (shell == null) {
            return; // signed out, e.g. a save that finished after logging out: nowhere to show it
        }
        if (currentScreen != null) {
            history.push(currentScreen);
        }
        currentScreen = screen;
        tracker.moved();
        shell.setContent(screen);
    }

    /** Shows a screen below the header in place of the current one, which isn't returned to. */
    private void replace(Parent screen) {
        tracker.forget(currentScreen);
        currentScreen = screen;
        tracker.moved();
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
        Object controller = loader.getController();
        if (controller instanceof NavigatorAware aware) {
            aware.setNavigator(this);
        }
        tracker.remember(loader.getRoot(), controller);
        return loader;
    }
}