package models;

public class Team {
    private int teamId;
    private int orgId;
    private String name;

    public Team() {}

    public Team(int teamId, int orgId, String name) {
        this.teamId = teamId;
        this.orgId = orgId;
        this.name = name;
    }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}