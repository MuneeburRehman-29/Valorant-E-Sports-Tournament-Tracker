package models;

import java.time.LocalDateTime;

public class Match {
    private int matchId;
    private int tournamentId;
    private int mapId;
    private String stage;
    private LocalDateTime matchDate;
    private int teamAId;
    private int teamBId;
    private int winnerTeamId;

    public Match() {}

    public Match(int matchId, int tournamentId, int mapId, String stage, LocalDateTime matchDate, int teamAId, int teamBId, int winnerTeamId) {
        this.matchId = matchId;
        this.tournamentId = tournamentId;
        this.mapId = mapId;
        this.stage = stage;
        this.matchDate = matchDate;
        this.teamAId = teamAId;
        this.teamBId = teamBId;
        this.winnerTeamId = winnerTeamId;
    }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }
    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }
    public int getMapId() { return mapId; }
    public void setMapId(int mapId) { this.mapId = mapId; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }
    public int getTeamAId() { return teamAId; }
    public void setTeamAId(int teamAId) { this.teamAId = teamAId; }
    public int getTeamBId() { return teamBId; }
    public void setTeamBId(int teamBId) { this.teamBId = teamBId; }
    public int getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(int winnerTeamId) { this.winnerTeamId = winnerTeamId; }
}