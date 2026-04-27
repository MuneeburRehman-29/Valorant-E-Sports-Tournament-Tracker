package models;

public class Agent {
    private int agentId;
    private String name;
    private String role;

    public Agent() {}

    public Agent(int agentId, String name, String role) {
        this.agentId = agentId;
        this.name = name;
        this.role = role;
    }

    public int getAgentId() { return agentId; }
    public void setAgentId(int agentId) { this.agentId = agentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}