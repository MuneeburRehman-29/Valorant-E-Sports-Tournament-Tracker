package app.gui;

import app.DBConnection;
import app.dao.PlayerDAO;
import app.dao.TeamDAO;
import app.dao.OrganizationDAO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardTabController {

    @FXML private Button refreshLbBtn;
    @FXML private HBox podiumBox;
    @FXML private Label lbStatusLabel;

    @FXML private TableView<LbRow> leaderboardTable;
    @FXML private TableColumn<LbRow, String> colRank;
    @FXML private TableColumn<LbRow, String> colLbPlayer;
    @FXML private TableColumn<LbRow, String> colLbTeam;
    @FXML private TableColumn<LbRow, String> colAvgAcs;
    @FXML private TableColumn<LbRow, String> colTotalK;
    @FXML private TableColumn<LbRow, String> colTotalD;
    @FXML private TableColumn<LbRow, String> colTotalA;
    @FXML private TableColumn<LbRow, String> colKdRatio;

    @FXML private TableView<RosterRow> rosterTable;
    @FXML private TableColumn<RosterRow, String> colRosterRiotId;
    @FXML private TableColumn<RosterRow, String> colRosterTeam;
    @FXML private TableColumn<RosterRow, String> colRosterOrg;
    @FXML private TableColumn<RosterRow, String> colRosterRegion;
    @FXML private TableColumn<RosterRow, String> colRosterStatus;

    @FXML
    public void initialize() {
        // Wire leaderboard columns
        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colLbPlayer.setCellValueFactory(new PropertyValueFactory<>("riotId"));
        colLbTeam.setCellValueFactory(new PropertyValueFactory<>("team"));
        colAvgAcs.setCellValueFactory(new PropertyValueFactory<>("avgAcs"));
        colTotalK.setCellValueFactory(new PropertyValueFactory<>("kills"));
        colTotalD.setCellValueFactory(new PropertyValueFactory<>("deaths"));
        colTotalA.setCellValueFactory(new PropertyValueFactory<>("assists"));
        colKdRatio.setCellValueFactory(new PropertyValueFactory<>("kd"));

        // Rank column: gold/silver/bronze styling for top 3
        colRank.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "1"  -> setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 15px;");
                    case "2"  -> setStyle("-fx-text-fill: #C0C0C0; -fx-font-weight: bold; -fx-font-size: 14px;");
                    case "3"  -> setStyle("-fx-text-fill: #CD7F32; -fx-font-weight: bold; -fx-font-size: 13px;");
                    default   -> setStyle("-fx-text-fill: #7f8c8d;");
                }
            }
        });

        // ACS column: highlight high values
        colAvgAcs.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                try {
                    double acs = Double.parseDouble(item);
                    if (acs >= 250)     setStyle("-fx-text-fill: #ff4655; -fx-font-weight: bold;");
                    else if (acs >= 200) setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    else                setStyle("-fx-text-fill: #ecf0f1;");
                } catch (NumberFormatException ignored) {}
            }
        });

        // Wire roster columns
        colRosterRiotId.setCellValueFactory(new PropertyValueFactory<>("riotId"));
        colRosterTeam.setCellValueFactory(new PropertyValueFactory<>("team"));
        colRosterOrg.setCellValueFactory(new PropertyValueFactory<>("org"));
        colRosterRegion.setCellValueFactory(new PropertyValueFactory<>("region"));
        colRosterStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status column coloring
        colRosterStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.equals("Active"))
                    setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                else
                    setStyle("-fx-text-fill: #e74c3c;");
            }
        });

        loadData();
    }

    @FXML
    public void handleRefresh() {
        loadData();
    }

    private void loadData() {
        lbStatusLabel.setText("Loading...");
        lbStatusLabel.setStyle("-fx-text-fill: #f39c12;");
        podiumBox.getChildren().clear();

        Task<Result> task = new Task<>() {
            @Override
            protected Result call() throws Exception {
                return new Result(fetchLeaderboard(), fetchRoster());
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    Result r = getValue();
                    leaderboardTable.setItems(FXCollections.observableArrayList(r.lbRows));
                    rosterTable.setItems(FXCollections.observableArrayList(r.rosterRows));
                    buildPodium(r.lbRows);
                    lbStatusLabel.setText(r.lbRows.isEmpty()
                        ? "No stats yet — record matches to build rankings"
                        : "✔ " + r.lbRows.size() + " players ranked");
                    lbStatusLabel.setStyle(r.lbRows.isEmpty()
                        ? "-fx-text-fill: #7f8c8d;"
                        : "-fx-text-fill: #2ecc71;");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    lbStatusLabel.setText("✘ Failed to load: " + getException().getMessage());
                    lbStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
                });
            }
        };
        new Thread(task).start();
    }

    /**
     * COMPLEX QUERY 1 — Leaderboard
     * Uses GROUP BY + AVG + SUM across player_match_stats joined to players and teams.
     */
    private List<LbRow> fetchLeaderboard() throws SQLException {
        String sql =
            "SELECT p.riot_id, " +
            "       t.name AS team, " +
            "       ROUND(AVG(pms.combat_score), 1) AS avg_acs, " +
            "       SUM(pms.kills)   AS total_kills, " +
            "       SUM(pms.deaths)  AS total_deaths, " +
            "       SUM(pms.assists) AS total_assists " +
            "FROM player_match_stats pms " +
            "JOIN players p ON pms.player_id = p.player_id " +
            "JOIN teams   t ON p.team_id     = t.team_id " +
            "GROUP BY p.player_id, p.riot_id, t.name " +
            "ORDER BY avg_acs DESC";

        List<LbRow> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int rank = 1;
            while (rs.next()) {
                int kills  = rs.getInt("total_kills");
                int deaths = rs.getInt("total_deaths");
                int assists = rs.getInt("total_assists");
                String kd  = deaths > 0
                        ? String.format("%.2f", (double) kills / deaths)
                        : "Perfect";
                rows.add(new LbRow(
                        String.valueOf(rank++),
                        rs.getString("riot_id"),
                        rs.getString("team"),
                        String.valueOf(rs.getDouble("avg_acs")),
                        String.valueOf(kills),
                        String.valueOf(deaths),
                        String.valueOf(assists),
                        kd
                ));
            }
        }
        return rows;
    }

    /**
     * COMPLEX QUERY 2 — Full Roster
     * 3-table JOIN: players → teams → organizations
     */
    private List<RosterRow> fetchRoster() throws SQLException {
        String sql =
            "SELECT p.riot_id, " +
            "       t.name  AS team, " +
            "       o.name  AS org, " +
            "       o.region, " +
            "       p.is_active " +
            "FROM players p " +
            "JOIN teams         t ON p.team_id = t.team_id " +
            "JOIN organizations o ON t.org_id  = o.org_id " +
            "ORDER BY o.region, t.name, p.riot_id";

        List<RosterRow> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new RosterRow(
                        rs.getString("riot_id"),
                        rs.getString("team"),
                        rs.getString("org"),
                        rs.getString("region") != null ? rs.getString("region") : "—",
                        rs.getBoolean("is_active") ? "Active" : "Inactive"
                ));
            }
        }
        return rows;
    }

    private void buildPodium(List<LbRow> rows) {
        podiumBox.getChildren().clear();
        if (rows.isEmpty()) return;

        // Order: 2nd, 1st, 3rd for classic podium look
        int[] order = rows.size() >= 3 ? new int[]{1, 0, 2}
                    : rows.size() == 2 ? new int[]{1, 0}
                    : new int[]{0};

        String[] medals   = {"🥇", "🥈", "🥉"};
        String[] colors   = {"#FFD700", "#C0C0C0", "#CD7F32"};
        String[] heights  = {"90px", "70px", "55px"};

        for (int idx : order) {
            if (idx >= rows.size()) continue;
            LbRow row = rows.get(idx);
            int displayRank = Integer.parseInt(row.getRank());

            VBox card = new VBox(6);
            card.setAlignment(Pos.CENTER);
            card.setStyle(String.format(
                "-fx-background-color: #14232e; " +
                "-fx-border-color: %s; " +
                "-fx-border-width: 0 0 3 0; " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8; " +
                "-fx-padding: 16 24; " +
                "-fx-min-width: 180; " +
                "-fx-effect: dropshadow(gaussian, %s44, 16, 0, 0, 0);",
                colors[displayRank - 1], colors[displayRank - 1]
            ));

            Label medal = new Label(medals[displayRank - 1]);
            medal.setStyle("-fx-font-size: 28px;");

            Label name = new Label(row.getRiotId());
            name.setStyle("-fx-text-fill: #ecf0f1; -fx-font-weight: bold; -fx-font-size: 14px;");

            Label team = new Label(row.getTeam());
            team.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

            Label acs = new Label(row.getAvgAcs() + " ACS");
            acs.setStyle(String.format(
                "-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 16px;",
                colors[displayRank - 1]
            ));

            Label kd = new Label("K/D  " + row.getKd());
            kd.setStyle("-fx-text-fill: #4a6075; -fx-font-size: 11px;");

            card.getChildren().addAll(medal, name, team, acs, kd);
            podiumBox.getChildren().add(card);
        }
    }

    // ══ Helper result wrapper ══
    private static class Result {
        final List<LbRow> lbRows;
        final List<RosterRow> rosterRows;
        Result(List<LbRow> lb, List<RosterRow> r) { lbRows = lb; rosterRows = r; }
    }

    // ══ Row model classes ══

    public static class LbRow {
        private final String rank, riotId, team, avgAcs, kills, deaths, assists, kd;
        public LbRow(String rank, String riotId, String team,
                     String avgAcs, String kills, String deaths,
                     String assists, String kd) {
            this.rank = rank; this.riotId = riotId; this.team = team;
            this.avgAcs = avgAcs; this.kills = kills; this.deaths = deaths;
            this.assists = assists; this.kd = kd;
        }
        public String getRank()    { return rank; }
        public String getRiotId()  { return riotId; }
        public String getTeam()    { return team; }
        public String getAvgAcs()  { return avgAcs; }
        public String getKills()   { return kills; }
        public String getDeaths()  { return deaths; }
        public String getAssists() { return assists; }
        public String getKd()      { return kd; }
    }

    public static class RosterRow {
        private final String riotId, team, org, region, status;
        public RosterRow(String riotId, String team, String org, String region, String status) {
            this.riotId = riotId; this.team = team; this.org = org;
            this.region = region; this.status = status;
        }
        public String getRiotId() { return riotId; }
        public String getTeam()   { return team; }
        public String getOrg()    { return org; }
        public String getRegion() { return region; }
        public String getStatus() { return status; }
    }
}
