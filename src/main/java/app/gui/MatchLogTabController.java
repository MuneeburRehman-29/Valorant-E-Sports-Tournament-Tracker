package app.gui;

import app.dao.MatchDAO;
import app.dao.MatchRoundDAO;
import app.dao.PlayerDAO;
import app.dao.TeamDAO;
import app.dao.TournamentDAO;
import app.dao.MapDAO;
import models.Match;
import models.MatchRound;
import models.Team;
import models.Tournament;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MatchLogTabController {

    @FXML private Label totalMatchesLabel;
    @FXML private Label totalTournamentsLabel;
    @FXML private Label totalTeamsLabel;
    @FXML private Label totalPlayersLabel;
    @FXML private Button refreshMatchesBtn;

    @FXML private TableView<MatchRow> matchTable;
    @FXML private TableColumn<MatchRow, String> colMatchId;
    @FXML private TableColumn<MatchRow, String> colTournament;
    @FXML private TableColumn<MatchRow, String> colStage;
    @FXML private TableColumn<MatchRow, String> colMatchMap;
    @FXML private TableColumn<MatchRow, String> colTeamA;
    @FXML private TableColumn<MatchRow, String> colTeamB;
    @FXML private TableColumn<MatchRow, String> colWinner;
    @FXML private TableColumn<MatchRow, String> colMatchDate;

    @FXML private javafx.scene.layout.VBox roundDetailPane;
    @FXML private Label roundDetailTitle;
    @FXML private TableView<RoundRow> roundTable;
    @FXML private TableColumn<RoundRow, String> colRoundNum;
    @FXML private TableColumn<RoundRow, String> colRoundWinner;
    @FXML private TableColumn<RoundRow, String> colRoundSide;
    @FXML private TableColumn<RoundRow, String> colRoundType;

    private final MatchDAO matchDAO           = new MatchDAO();
    private final MatchRoundDAO roundDAO      = new MatchRoundDAO();
    private final TeamDAO teamDAO             = new TeamDAO();
    private final TournamentDAO tournamentDAO = new TournamentDAO();
    private final MapDAO mapDAO               = new MapDAO();
    private final PlayerDAO playerDAO         = new PlayerDAO();

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Cache maps for lookups
    private java.util.Map<Integer, String> teamNames        = new HashMap<>();
    private java.util.Map<Integer, String> tournamentNames  = new HashMap<>();
    private java.util.Map<Integer, String> mapNames         = new HashMap<>();

    @FXML
    public void initialize() {
        // Wire match table columns
        colMatchId.setCellValueFactory(new PropertyValueFactory<>("matchId"));
        colTournament.setCellValueFactory(new PropertyValueFactory<>("tournament"));
        colStage.setCellValueFactory(new PropertyValueFactory<>("stage"));
        colMatchMap.setCellValueFactory(new PropertyValueFactory<>("map"));
        colTeamA.setCellValueFactory(new PropertyValueFactory<>("teamA"));
        colTeamB.setCellValueFactory(new PropertyValueFactory<>("teamB"));
        colWinner.setCellValueFactory(new PropertyValueFactory<>("winner"));
        colMatchDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Winner column: highlight in red
        colWinner.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (!item.equals("TBD"))
                    setStyle("-fx-text-fill: #ff4655; -fx-font-weight: bold;");
                else
                    setStyle("-fx-text-fill: #7f8c8d;");
            }
        });

        // Wire round table columns
        colRoundNum.setCellValueFactory(new PropertyValueFactory<>("roundNum"));
        colRoundWinner.setCellValueFactory(new PropertyValueFactory<>("winner"));
        colRoundSide.setCellValueFactory(new PropertyValueFactory<>("side"));
        colRoundType.setCellValueFactory(new PropertyValueFactory<>("winType"));

        // Click a match row → show rounds
        matchTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) loadRoundsFor(selected);
        });

        // Load data on init
        loadAllData();
    }

    @FXML
    public void handleRefresh() {
        loadAllData();
    }

    private void loadAllData() {
        totalMatchesLabel.setText("…");
        totalTournamentsLabel.setText("…");
        totalTeamsLabel.setText("…");
        totalPlayersLabel.setText("…");

        Task<Void> task = new Task<>() {
            private List<MatchRow> rows;
            private int matchCount, tournCount, teamCount, playerCount;

            @Override
            protected Void call() throws Exception {
                // Build lookup caches
                teamDAO.findAll().forEach(t -> teamNames.put(t.getTeamId(), t.getName()));
                tournamentDAO.findAll().forEach(t -> tournamentNames.put(t.getTournamentId(), t.getName()));
                mapDAO.findAll().forEach(m -> mapNames.put(m.getMapId(), m.getName()));

                List<Match> matches = matchDAO.findAll();
                matchCount   = matches.size();
                tournCount   = tournamentDAO.findAll().size();
                teamCount    = teamDAO.findAll().size();
                playerCount  = playerDAO.findAll().size();

                rows = new ArrayList<>();
                for (Match m : matches) {
                    rows.add(new MatchRow(
                            String.valueOf(m.getMatchId()),
                            tournamentNames.getOrDefault(m.getTournamentId(), "—"),
                            m.getStage() != null ? m.getStage() : "—",
                            mapNames.getOrDefault(m.getMapId(), "—"),
                            teamNames.getOrDefault(m.getTeamAId(), "—"),
                            teamNames.getOrDefault(m.getTeamBId(), "—"),
                            m.getWinnerTeamId() > 0
                                ? teamNames.getOrDefault(m.getWinnerTeamId(), "TBD")
                                : "TBD",
                            m.getMatchDate() != null ? m.getMatchDate().format(fmt) : "—",
                            m.getMatchId()
                    ));
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    totalMatchesLabel.setText(String.valueOf(matchCount));
                    totalTournamentsLabel.setText(String.valueOf(tournCount));
                    totalTeamsLabel.setText(String.valueOf(teamCount));
                    totalPlayersLabel.setText(String.valueOf(playerCount));
                    matchTable.setItems(FXCollections.observableArrayList(rows));
                    roundDetailPane.setVisible(false);
                    roundDetailPane.setManaged(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    totalMatchesLabel.setText("✘");
                });
            }
        };
        new Thread(task).start();
    }

    private void loadRoundsFor(MatchRow selected) {
        int matchId = selected.getRawMatchId();
        roundDetailTitle.setText("ROUND BREAKDOWN — Match #" + matchId
                + "  (" + selected.getTeamA() + "  vs  " + selected.getTeamB() + ")");

        Task<List<RoundRow>> task = new Task<>() {
            @Override
            protected List<RoundRow> call() throws Exception {
                List<MatchRound> rounds = roundDAO.findByMatchId(matchId);
                List<RoundRow> rows = new ArrayList<>();
                for (MatchRound r : rounds) {
                    rows.add(new RoundRow(
                            String.valueOf(r.getRoundNumber()),
                            teamNames.getOrDefault(r.getWinningTeamId(), "—"),
                            r.getWinningSide() != null ? r.getWinningSide() : "—",
                            r.getWinType() != null ? r.getWinType() : "—"
                    ));
                }
                return rows;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    roundTable.setItems(FXCollections.observableArrayList(getValue()));
                    roundDetailPane.setVisible(true);
                    roundDetailPane.setManaged(true);
                });
            }
        };
        new Thread(task).start();
    }

    // ══ Row model classes ══

    public static class MatchRow {
        private final String matchId, tournament, stage, map,
                             teamA, teamB, winner, date;
        private final int rawMatchId;

        public MatchRow(String matchId, String tournament, String stage, String map,
                        String teamA, String teamB, String winner, String date, int rawMatchId) {
            this.matchId = matchId; this.tournament = tournament;
            this.stage = stage; this.map = map;
            this.teamA = teamA; this.teamB = teamB;
            this.winner = winner; this.date = date;
            this.rawMatchId = rawMatchId;
        }
        public String getMatchId()     { return matchId; }
        public String getTournament()  { return tournament; }
        public String getStage()       { return stage; }
        public String getMap()         { return map; }
        public String getTeamA()       { return teamA; }
        public String getTeamB()       { return teamB; }
        public String getWinner()      { return winner; }
        public String getDate()        { return date; }
        public int    getRawMatchId()  { return rawMatchId; }
    }

    public static class RoundRow {
        private final String roundNum, winner, side, winType;
        public RoundRow(String roundNum, String winner, String side, String winType) {
            this.roundNum = roundNum; this.winner = winner;
            this.side = side; this.winType = winType;
        }
        public String getRoundNum() { return roundNum; }
        public String getWinner()   { return winner; }
        public String getSide()     { return side; }
        public String getWinType()  { return winType; }
    }
}
