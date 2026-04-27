package models;

public class Organization {
    private int orgId;
    private String name;
    private String region;

    public Organization() {}

    public Organization(int orgId, String name, String region) {
        this.orgId = orgId;
        this.name = name;
        this.region = region;
    }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}