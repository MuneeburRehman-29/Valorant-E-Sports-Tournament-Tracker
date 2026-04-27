package models;

public class PlayerMatchStat {
    private int statId;
    private int matchId;
    private int playerId;
    private int agentId;
    private int kills;
    private int deaths;
    private int assists;
    private int combatScore;

    public PlayerMatchStat() {}

    public PlayerMatchStat(int statId, int matchId, int playerId, int agentId, int kills, int deaths, int assists, int combatScore) {
        this.statId = statId;
        this.matchId = matchId;
        this.playerId = playerId;
        this.agentId = agentId;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.combatScore = combatScore;
    }

    public int getStatId() { return statId; }
    public void setStatId(int statId) { this.statId = statId; }
    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }
    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public int getAgentId() { return agentId; }
    public void setAgentId(int agentId) { this.agentId = agentId; }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }
    public int getCombatScore() { return combatScore; }
    public void setCombatScore(int combatScore) { this.combatScore = combatScore; }
}