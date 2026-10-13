package lavs;

import java.util.Objects;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lavs.ui.shell.Navigator;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Temporary root: the navigator replaces it straight away
        Scene scene = new Scene(new StackPane(), 1280, 800);

        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/lavs/ui/app.css"),
                "Stylesheet /lavs/ui/app.css not found").toExternalForm());

        primaryStage.setTitle("LAVS");
        primaryStage.setScene(scene);   // must come before the navigator uses the scene

        Navigator navigator = new Navigator(primaryStage);
        navigator.showShell();           // later: navigator.showLogin() once login exists
        navigator.showCustomerSearch();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}