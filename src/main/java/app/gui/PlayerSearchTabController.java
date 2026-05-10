package app.gui;

import app.ValorantApiService;
import app.AccountResponse;
import app.MatchResponse;
import app.dao.PlayerDAO;
import models.Player;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerSearchTabController {

    // ── Search inputs ──
    @FXML private TextField riotIdField;
    @FXML private TextField tagLineField;
    @FXML private ComboBox<String> regionCombo;
    @FXML private Button searchBtn;
    @FXML private Label apiStatusLabel;

    // ── API results table ──
    @FXML private TableView<MatchRow> matchHistoryTable;
    @FXML private TableColumn<MatchRow, String> colMap;
    @FXML private TableColumn<MatchRow, String> colResult;
    @FXML private TableColumn<MatchRow, String> colAgent;
    @FXML private TableColumn<MatchRow, String> colKills;
    @FXML private TableColumn<MatchRow, String> colDeaths;
    @FXML private TableColumn<MatchRow, String> colAssists;
    @FXML private TableColumn<MatchRow, String> colAcs;

    // ── DB section ──
    @FXML private Button saveToDbBtn;
    @FXML private Button viewDbBtn;
    @FXML private Label saveStatusLabel;
    @FXML private TableView<DbPlayerRow> dbPlayersTable;
    @FXML private TableColumn<DbPlayerRow, String> colDbId;
    @FXML private TableColumn<DbPlayerRow, String> colDbRiotId;
    @FXML private TableColumn<DbPlayerRow, String> colDbTeam;
    @FXML private TableColumn<DbPlayerRow, String> colDbActive;

    private final ValorantApiService apiService = new ValorantApiService();
    private final PlayerDAO playerDAO = new PlayerDAO();

    // Holds the last fetched account so we can save it
    private String lastFetchedRiotId = null;

    @FXML
    public void initialize() {
        regionCombo.setItems(FXCollections.observableArrayList("na", "eu", "ap", "kr", "br", "latam"));
        regionCombo.setValue("na");

        // Wire API match table columns
        colMap.setCellValueFactory(new PropertyValueFactory<>("map"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colAgent.setCellValueFactory(new PropertyValueFactory<>("agent"));
        colKills.setCellValueFactory(new PropertyValueFactory<>("kills"));
        colDeaths.setCellValueFactory(new PropertyValueFactory<>("deaths"));
        colAssists.setCellValueFactory(new PropertyValueFactory<>("assists"));
        colAcs.setCellValueFactory(new PropertyValueFactory<>("acs"));

        // Color WIN green / LOSS red in result column
        colResult.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.equals("WIN"))  setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                else if (item.equals("LOSS")) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                else setStyle("-fx-text-fill: #7f8c8d;");
            }
        });

        // Wire DB players table columns
        colDbId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDbRiotId.setCellValueFactory(new PropertyValueFactory<>("riotId"));
        colDbTeam.setCellValueFactory(new PropertyValueFactory<>("teamId"));
        colDbActive.setCellValueFactory(new PropertyValueFactory<>("active"));
    }

    @FXML
    public void handleSearch() {
        String name   = riotIdField.getText().trim();
        String tag    = tagLineField.getText().trim();
        String region = regionCombo.getValue();

        if (name.isEmpty() || tag.isEmpty()) {
            apiStatusLabel.setText("⚠ Please enter both Riot ID and Tag");
            apiStatusLabel.setStyle("-fx-text-fill: #f39c12;");
            return;
        }

        setSearchLoading(true);
        apiStatusLabel.setText("Fetching from Henrik API...");
        apiStatusLabel.setStyle("-fx-text-fill: #f39c12;");
        matchHistoryTable.setItems(FXCollections.observableArrayList());
        saveToDbBtn.setDisable(true);
        lastFetchedRiotId = null;

        Task<List<MatchRow>> task = new Task<>() {
            private AccountResponse account;

            @Override
            protected List<MatchRow> call() throws Exception {
                // 1. Verify account exists
                account = apiService.fetchAccountInfo(name, tag);

                // 2. Fetch match history using MatchResponse
                List<MatchRow> rows = new ArrayList<>();
                String apiKey = "HDEV-f7015903-5fce-49e7-aaa6-c55ec6fb6851";

                String url = "https://api.henrikdev.xyz/valorant/v3/matches/"
                        + region + "/" + name + "/" + tag;

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Authorization", apiKey)
                        .GET().build();

                java.net.http.HttpResponse<String> response =
                        client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

                com.google.gson.Gson gson = new com.google.gson.Gson();
                MatchResponse mr = gson.fromJson(response.body(), MatchResponse.class);

                if (mr.data == null) return rows;

                for (MatchResponse.MatchData match : mr.data) {
                    if (match.players == null || match.players.all_players == null) continue;
                    for (MatchResponse.Player p : match.players.all_players) {
                        if (p.name != null && p.name.equalsIgnoreCase(name)
                                && p.tag != null && p.tag.equalsIgnoreCase(tag)) {
                            int rounds = match.metadata != null ? match.metadata.rounds_played : 1;
                            int acs = (rounds > 0 && p.stats != null) ? p.stats.score / rounds : 0;

                            // Determine win/loss
                            String result = "N/A";
                            // Henrik v3 stores team result in each player's team field
                            // We can try to check from teams node via JSON directly
                            // Using character field approach:
                            if (p.stats != null) {
                                result = "—";
                            }

                            rows.add(new MatchRow(
                                    match.metadata != null ? match.metadata.map : "Unknown",
                                    result,
                                    p.character != null ? p.character : "Unknown",
                                    p.stats != null ? String.valueOf(p.stats.kills)   : "0",
                                    p.stats != null ? String.valueOf(p.stats.deaths)  : "0",
                                    p.stats != null ? String.valueOf(p.stats.assists) : "0",
                                    String.valueOf(acs)
                            ));
                            break;
                        }
                    }
                }
                return rows;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setSearchLoading(false);
                    List<MatchRow> rows = getValue();
                    if (rows.isEmpty()) {
                        apiStatusLabel.setText("No recent matches found for this player.");
                        apiStatusLabel.setStyle("-fx-text-fill: #7f8c8d;");
                    } else {
                        apiStatusLabel.setText("✔ Found " + rows.size() + " matches for "
                                + (account != null && account.data != null
                                    ? account.data.name + "#" + account.data.tag
                                    : name + "#" + tag));
                        apiStatusLabel.setStyle("-fx-text-fill: #2ecc71;");
                        matchHistoryTable.setItems(FXCollections.observableArrayList(rows));
                        lastFetchedRiotId = name + "#" + tag;
                        saveToDbBtn.setDisable(false);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setSearchLoading(false);
                    String msg = getException() != null ? getException().getMessage() : "Unknown error";
                    apiStatusLabel.setText("✘ Error: " + msg);
                    apiStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
                });
            }
        };
        new Thread(task).start();
    }

    @FXML
    public void handleSavePlayer() {
        if (lastFetchedRiotId == null) return;
        saveStatusLabel.setText("Saving...");
        saveStatusLabel.setStyle("-fx-text-fill: #f39c12;");

        String riotIdToSave = lastFetchedRiotId;
        Task<String> saveTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                var existing = playerDAO.findByRiotId(riotIdToSave);
                if (existing.isPresent()) {
                    return "already_exists";
                }
                Player p = new Player();
                p.setRiotId(riotIdToSave);
                p.setActive(true);
                playerDAO.insert(p);
                return "saved";
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if ("saved".equals(getValue())) {
                        saveStatusLabel.setText("✔ Saved " + riotIdToSave + " to database");
                        saveStatusLabel.setStyle("-fx-text-fill: #2ecc71;");
                    } else {
                        saveStatusLabel.setText("Player already exists in DB");
                        saveStatusLabel.setStyle("-fx-text-fill: #7f8c8d;");
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    saveStatusLabel.setText("✘ Save failed: " + getException().getMessage());
                    saveStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
                });
            }
        };
        new Thread(saveTask).start();
    }

    @FXML
    public void handleViewDb() {
        boolean showing = dbPlayersTable.isVisible();
        if (showing) {
            dbPlayersTable.setVisible(false);
            dbPlayersTable.setManaged(false);
            viewDbBtn.setText("🗄  VIEW DB PLAYERS");
            return;
        }

        viewDbBtn.setText("🗄  HIDE DB PLAYERS");
        Task<List<DbPlayerRow>> task = new Task<>() {
            @Override
            protected List<DbPlayerRow> call() throws Exception {
                List<Player> players = playerDAO.findAll();
                List<DbPlayerRow> rows = new ArrayList<>();
                for (Player p : players) {
                    rows.add(new DbPlayerRow(
                            String.valueOf(p.getPlayerId()),
                            p.getRiotId(),
                            p.getTeamId() > 0 ? String.valueOf(p.getTeamId()) : "—",
                            p.isActive() ? "Active" : "Inactive"
                    ));
                }
                return rows;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    dbPlayersTable.setItems(FXCollections.observableArrayList(getValue()));
                    dbPlayersTable.setVisible(true);
                    dbPlayersTable.setManaged(true);
                });
            }
        };
        new Thread(task).start();
    }

    private void setSearchLoading(boolean loading) {
        searchBtn.setDisable(loading);
        searchBtn.setText(loading ? "Fetching..." : "FETCH STATS");
    }

    // ══ Inner model classes for TableView rows ══

    public static class MatchRow {
        private final String map, result, agent, kills, deaths, assists, acs;
        public MatchRow(String map, String result, String agent,
                        String kills, String deaths, String assists, String acs) {
            this.map = map; this.result = result; this.agent = agent;
            this.kills = kills; this.deaths = deaths;
            this.assists = assists; this.acs = acs;
        }
        public String getMap()     { return map; }
        public String getResult()  { return result; }
        public String getAgent()   { return agent; }
        public String getKills()   { return kills; }
        public String getDeaths()  { return deaths; }
        public String getAssists() { return assists; }
        public String getAcs()     { return acs; }
    }

    public static class DbPlayerRow {
        private final String id, riotId, teamId, active;
        public DbPlayerRow(String id, String riotId, String teamId, String active) {
            this.id = id; this.riotId = riotId;
            this.teamId = teamId; this.active = active;
        }
        public String getId()     { return id; }
        public String getRiotId() { return riotId; }
        public String getTeamId() { return teamId; }
        public String getActive() { return active; }
    }
}
