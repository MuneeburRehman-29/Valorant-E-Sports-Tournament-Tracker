package app;

import java.sql.Connection;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // This links to the FXML file you just created!
        // Note: Change "/gui/PlayerSearch.fxml" to "/Gui/PlayerSearch.fxml"
        // if you kept the capital 'G' on your folder.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/PlayerSearch.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Valorant Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.out.println("Testing Database Connection...");

        // Call your connection method
        Connection testConnection = DBConnection.getConnection();

        // If it's not null, it worked!
        if (testConnection != null) {
            System.out.println("Ready to start building Model classes and DAOs!");
            try {
                testConnection.close(); // Close it cleanly after testing
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        launch(args); // This is the standard JavaFX launch command

    }
}