package lavs;

import java.util.Objects;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.service.LoginService;
import lavs.logic.service.Session;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.Services;

public class App extends Application {

    // The mainframe to use; run with -Dlavs.mainframe.url=... to point somewhere else
    private static final String MAINFRAME_URL =
            System.getProperty("lavs.mainframe.url", "https://animating-eraser-arrogance.ngrok-free.dev/");

    // The window's usual size, and the smallest it can be made before screens stop fitting
    private static final double WIDTH = 1280;
    private static final double HEIGHT = 800;
    private static final double MIN_WIDTH = 1100;
    private static final double MIN_HEIGHT = 680;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Temporary root: the navigator replaces it straight away. Opens smaller on a small screen.
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(new StackPane(),
                Math.min(WIDTH, screen.getWidth()), Math.min(HEIGHT, screen.getHeight()));

        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/lavs/ui/app.css"),
                "Stylesheet /lavs/ui/app.css not found").toExternalForm());

        primaryStage.setTitle("LAVS");
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setScene(scene);   // must come before the navigator uses the scene

        // The services every screen shares, built once; the UI only ever sees services
        Session session = new Session();
        MainframeAdapter mainframe = MainframeAdapter.connect(MAINFRAME_URL);
        Services services = new Services(session, new LoginService(mainframe, session));

        Navigator navigator = new Navigator(primaryStage, services);
        navigator.showLogin();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}