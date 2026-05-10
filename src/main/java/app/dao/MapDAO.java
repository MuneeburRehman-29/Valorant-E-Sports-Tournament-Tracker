package app.dao;

import models.Map;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MapDAO extends AbstractDao {

    private static final String BASE_SELECT = "SELECT map_id, name FROM maps";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY map_id";
    private static final String INSERT = "INSERT INTO maps (name) VALUES (?)";
    private static final String UPDATE = "UPDATE maps SET name = ? WHERE map_id = ?";
    private static final String DELETE = "DELETE FROM maps WHERE map_id = ?";

    public List<Map> findAll() throws SQLException {
        List<Map> maps = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_ALL); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                maps.add(mapRow(resultSet));
            }
        }
        return maps;
    }

    public Optional<Map> findById(int mapId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE map_id = ?")) {
            statement.setInt(1, mapId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public int insert(Map map) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, map.getName());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    map.setMapId(keys.getInt(1));
                }
            }
        }
        return map.getMapId();
    }

    public boolean update(Map map) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, map.getName());
            statement.setInt(2, map.getMapId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int mapId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, mapId);
            return statement.executeUpdate() > 0;
        }
    }

    private Map mapRow(ResultSet resultSet) throws SQLException {
        return new Map(
                resultSet.getInt("map_id"),
                resultSet.getString("name")
        );
    }
}
