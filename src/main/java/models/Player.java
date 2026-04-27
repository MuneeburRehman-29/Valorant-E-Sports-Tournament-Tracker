package models;

public class Player {
    private int playerId;
    private int teamId;
    private String riotId;
    private boolean isActive;

    public Player() {}

    public Player(int playerId, int teamId, String riotId, boolean isActive) {
        this.playerId = playerId;
        this.teamId = teamId;
        this.riotId = riotId;
        this.isActive = isActive;
    }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public String getRiotId() { return riotId; }
    public void setRiotId(String riotId) { this.riotId = riotId; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}