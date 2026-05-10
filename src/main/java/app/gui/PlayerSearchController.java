package app.gui;

import app.ValorantApiService;
import app.AccountResponse;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import app.dao.PlayerDAO;
import models.Player;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.sql.SQLException;
import java.util.List;

public class PlayerSearchController {

    @FXML
    private TextField riotIdField;

    @FXML
    private TextField tagLineField;

    @FXML
    private Button searchButton;

    @FXML
    private TextArea logOutput;

    @FXML
    private Button loadDbButton;

    private ValorantApiService apiService;
    private PlayerDAO playerDAO;

    @FXML
    public void initialize() {
        apiService = new ValorantApiService();
        playerDAO = new PlayerDAO();
        logOutput.setText("System ready. \nWaiting for input... ");
    }

    @FXML
    void handleSearch(ActionEvent event) {
        String riotId = riotIdField.getText().trim();
        String tagLine = tagLineField.getText().trim();

        if (riotId.isEmpty() || tagLine.isEmpty()) {
            logOutput.setText("Error: Please provide both a Riot ID and a Tagline.\n");
            return;
        }

        logOutput.setText("Fetching data for " + riotId + "#" + tagLine + "...\n\n");

        Task<Void> fetchTask = new Task<>() {
            private AccountResponse account;
            private String errorMsg = null;

            @Override
            protected Void call() {
                try {
                    account = apiService.fetchAccountInfo(riotId, tagLine);
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (errorMsg != null) {
                        logOutput.appendText("Error fetching account: " + errorMsg + "\n");
                        return;
                    }
                    if (account == null || account.data == null) {
                        logOutput.appendText("No account data returned.\n");
                        return;
                    }

                    String fullRiotId = account.data.name + "#" + account.data.tag;
                    logOutput.appendText("Found: " + fullRiotId + "\n");
                    logOutput.appendText("Account Level: " + account.data.account_level + "\n");
                    logOutput.appendText("Region: " + account.data.region + "\n\n");

                    javafx.scene.control.TextInputDialog editDialog = new javafx.scene.control.TextInputDialog(fullRiotId);
                    editDialog.setTitle("Confirm Save");
                    editDialog.setHeaderText("Edit Riot ID before saving");
                    editDialog.setContentText("Riot ID (name#tag):");

                    editDialog.showAndWait().ifPresent(edited -> {
                        String toSave = edited.trim();
                        if (toSave.isEmpty()) {
                            logOutput.appendText("Save cancelled: empty Riot ID provided.\n");
                            return;
                        }

                        Task<Void> saveTask = new Task<>() {
                            private String saveMsg = null;

                            @Override
                            protected Void call() {
                                try {
                                    Player p = new Player();
                                    p.setRiotId(toSave);
                                    p.setActive(true);
                                    playerDAO.insert(p);
                                    saveMsg = "Player saved to database successfully.\n";
                                } catch (SQLException e) {
                                    saveMsg = "Failed to save player: " + e.getMessage() + "\n";
                                }
                                return null;
                            }

                            @Override
                            protected void succeeded() {
                                Platform.runLater(() -> logOutput.appendText(saveMsg));
                            }
                        };
                        new Thread(saveTask).start();
                    });
                });
            }
        };
        new Thread(fetchTask).start();
    }

    @FXML
    void handleLoadFromDb(ActionEvent event) {
        logOutput.setText("Fetching all players currently saved in MySQL Database...\n\n");
        try {
            List<Player> players = playerDAO.findAll();
            if (players.isEmpty()) {
                logOutput.appendText("Database is currently empty.\n");
                return;
            }
            for (Player p : players) {
                logOutput.appendText(String.format("DB ID: %d | Riot ID: %s | Active: %b | Team ID: %d%n",
                        p.getPlayerId(), p.getRiotId(), p.isActive(), p.getTeamId()));
            }
        } catch (SQLException e) {
            logOutput.appendText("Database error: " + e.getMessage() + "\n");
        }
    }
}
