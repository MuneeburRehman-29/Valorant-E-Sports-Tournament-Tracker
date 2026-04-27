package models;

public class MatchRound {
    private int roundId;
    private int matchId;
    private int roundNumber;
    private int winningTeamId;
    private String winningSide;
    private String winType;

    public MatchRound() {}

    public MatchRound(int roundId, int matchId, int roundNumber, int winningTeamId, String winningSide, String winType) {
        this.roundId = roundId;
        this.matchId = matchId;
        this.roundNumber = roundNumber;
        this.winningTeamId = winningTeamId;
        this.winningSide = winningSide;
        this.winType = winType;
    }

    public int getRoundId() { return roundId; }
    public void setRoundId(int roundId) { this.roundId = roundId; }
    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public int getWinningTeamId() { return winningTeamId; }
    public void setWinningTeamId(int winningTeamId) { this.winningTeamId = winningTeamId; }
    public String getWinningSide() { return winningSide; }
    public void setWinningSide(String winningSide) { this.winningSide = winningSide; }
    public String getWinType() { return winType; }
    public void setWinType(String winType) { this.winType = winType; }
}