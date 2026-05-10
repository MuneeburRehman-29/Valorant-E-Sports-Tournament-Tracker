package app.dao;

import models.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerDAO extends AbstractDao {

    private static final String BASE_SELECT = "SELECT player_id, team_id, riot_id, is_active FROM players";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY player_id";
    private static final String INSERT = "INSERT INTO players (team_id, riot_id, is_active) VALUES (?, ?, ?)";
    private static final String UPDATE = "UPDATE players SET team_id = ?, riot_id = ?, is_active = ? WHERE player_id = ?";
    private static final String DELETE = "DELETE FROM players WHERE player_id = ?";

    public List<Player> findAll() throws SQLException {
        List<Player> players = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_ALL); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                players.add(mapRow(resultSet));
            }
        }
        return players;
    }

    public Optional<Player> findById(int playerId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE player_id = ?")) {
            statement.setInt(1, playerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Player> findByTeamId(int teamId) throws SQLException {
        List<Player> players = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE team_id = ? ORDER BY player_id")) {
            statement.setInt(1, teamId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    players.add(mapRow(resultSet));
                }
            }
        }
        return players;
    }

    public List<Player> searchByRiotId(String fragment) throws SQLException {
        List<Player> players = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE riot_id LIKE ? ORDER BY player_id")) {
            statement.setString(1, "%" + fragment + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    players.add(mapRow(resultSet));
                }
            }
        }
        return players;
    }

    public Optional<Player> findByRiotId(String riotId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE riot_id = ?")) {
            statement.setString(1, riotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public int insert(Player player) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            if (player.getTeamId() > 0) {
                statement.setInt(1, player.getTeamId());
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            statement.setString(2, player.getRiotId());
            statement.setBoolean(3, player.isActive());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    player.setPlayerId(keys.getInt(1));
                }
            }
        }
        return player.getPlayerId();
    }

    public boolean update(Player player) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            if (player.getTeamId() > 0) {
                statement.setInt(1, player.getTeamId());
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            statement.setString(2, player.getRiotId());
            statement.setBoolean(3, player.isActive());
            statement.setInt(4, player.getPlayerId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int playerId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, playerId);
            return statement.executeUpdate() > 0;
        }
    }

    private Player mapRow(ResultSet resultSet) throws SQLException {
        return new Player(
                resultSet.getInt("player_id"),
                resultSet.getInt("team_id"),
                resultSet.getString("riot_id"),
                resultSet.getBoolean("is_active")
        );
    }
}
