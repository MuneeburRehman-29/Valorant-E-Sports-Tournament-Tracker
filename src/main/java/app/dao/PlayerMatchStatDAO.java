package app.dao;

import models.PlayerMatchStat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerMatchStatDAO extends AbstractDao {

    private static final String BASE_SELECT = "SELECT stat_id, match_id, player_id, agent_id, kills, deaths, assists, combat_score FROM player_match_stats";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY stat_id";
    private static final String INSERT = "INSERT INTO player_match_stats (match_id, player_id, agent_id, kills, deaths, assists, combat_score) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE player_match_stats SET match_id = ?, player_id = ?, agent_id = ?, kills = ?, deaths = ?, assists = ?, combat_score = ? WHERE stat_id = ?";
    private static final String DELETE = "DELETE FROM player_match_stats WHERE stat_id = ?";

    public List<PlayerMatchStat> findAll() throws SQLException {
        List<PlayerMatchStat> stats = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_ALL); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                stats.add(mapRow(resultSet));
            }
        }
        return stats;
    }

    public Optional<PlayerMatchStat> findById(int statId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE stat_id = ?")) {
            statement.setInt(1, statId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<PlayerMatchStat> findByMatchId(int matchId) throws SQLException {
        List<PlayerMatchStat> stats = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE match_id = ? ORDER BY stat_id")) {
            statement.setInt(1, matchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    stats.add(mapRow(resultSet));
                }
            }
        }
        return stats;
    }

    public List<PlayerMatchStat> findByPlayerId(int playerId) throws SQLException {
        List<PlayerMatchStat> stats = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE player_id = ? ORDER BY stat_id")) {
            statement.setInt(1, playerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    stats.add(mapRow(resultSet));
                }
            }
        }
        return stats;
    }

    public int insert(PlayerMatchStat stat) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, stat.getMatchId());
            statement.setInt(2, stat.getPlayerId());
            if (stat.getAgentId() > 0) {
                statement.setInt(3, stat.getAgentId());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            statement.setInt(4, stat.getKills());
            statement.setInt(5, stat.getDeaths());
            statement.setInt(6, stat.getAssists());
            statement.setInt(7, stat.getCombatScore());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    stat.setStatId(keys.getInt(1));
                }
            }
        }
        return stat.getStatId();
    }

    public boolean update(PlayerMatchStat stat) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setInt(1, stat.getMatchId());
            statement.setInt(2, stat.getPlayerId());
            if (stat.getAgentId() > 0) {
                statement.setInt(3, stat.getAgentId());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            statement.setInt(4, stat.getKills());
            statement.setInt(5, stat.getDeaths());
            statement.setInt(6, stat.getAssists());
            statement.setInt(7, stat.getCombatScore());
            statement.setInt(8, stat.getStatId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int statId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, statId);
            return statement.executeUpdate() > 0;
        }
    }

    private PlayerMatchStat mapRow(ResultSet resultSet) throws SQLException {
        return new PlayerMatchStat(
                resultSet.getInt("stat_id"),
                resultSet.getInt("match_id"),
                resultSet.getInt("player_id"),
                resultSet.getInt("agent_id"),
                resultSet.getInt("kills"),
                resultSet.getInt("deaths"),
                resultSet.getInt("assists"),
                resultSet.getInt("combat_score")
        );
    }
}
