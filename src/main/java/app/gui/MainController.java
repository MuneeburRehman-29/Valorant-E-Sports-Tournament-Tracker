package app.gui;

import app.DBConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.concurrent.Task;

import java.sql.Connection;

public class MainController {

    @FXML private Label dbStatusLabel;

    @FXML
    public void initialize() {
        dbStatusLabel.setText("● Connecting...");
        dbStatusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");

        Task<Boolean> dbCheck = new Task<>() {
            @Override
            protected Boolean call() {
                try (Connection c = DBConnection.getConnection()) {
                    return c != null;
                } catch (Exception e) {
                    return false;
                }
            }
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (getValue()) {
                        dbStatusLabel.setText("● DB Connected");
                        dbStatusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 12px;");
                    } else {
                        dbStatusLabel.setText("● DB Offline");
                        dbStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12px;");
                    }
                });
            }
        };
        new Thread(dbCheck).start();
    }
}
