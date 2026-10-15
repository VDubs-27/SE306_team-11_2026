package lavs;

import java.util.Objects;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Temporary root: the navigator replaces it straight away
        Scene scene = new Scene(new StackPane(), 1280, 800);

        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/lavs/ui/app.css"),
                "Stylesheet /lavs/ui/app.css not found").toExternalForm());

        primaryStage.setTitle("LAVS");
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