package models;

public class Map {
    private int mapId;
    private String name;

    public Map() {}

    public Map(int mapId, String name) {
        this.mapId = mapId;
        this.name = name;
    }

    public int getMapId() { return mapId; }
    public void setMapId(int mapId) { this.mapId = mapId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}