package app.dao;

import models.MatchRound;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchRoundDAO extends AbstractDao {

    private static final String BASE_SELECT = "SELECT round_id, match_id, round_number, winning_team_id, winning_side, win_type FROM match_rounds";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY round_id";
    private static final String INSERT = "INSERT INTO match_rounds (match_id, round_number, winning_team_id, winning_side, win_type) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE match_rounds SET match_id = ?, round_number = ?, winning_team_id = ?, winning_side = ?, win_type = ? WHERE round_id = ?";
    private static final String DELETE = "DELETE FROM match_rounds WHERE round_id = ?";

    public List<MatchRound> findAll() throws SQLException {
        List<MatchRound> rounds = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_ALL); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rounds.add(mapRow(resultSet));
            }
        }
        return rounds;
    }

    public Optional<MatchRound> findById(int roundId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE round_id = ?")) {
            statement.setInt(1, roundId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<MatchRound> findByMatchId(int matchId) throws SQLException {
        List<MatchRound> rounds = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE match_id = ? ORDER BY round_number")) {
            statement.setInt(1, matchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rounds.add(mapRow(resultSet));
                }
            }
        }
        return rounds;
    }

    public int insert(MatchRound round) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, round.getMatchId());
            statement.setInt(2, round.getRoundNumber());
            if (round.getWinningTeamId() > 0) {
                statement.setInt(3, round.getWinningTeamId());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            statement.setString(4, round.getWinningSide());
            statement.setString(5, round.getWinType());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    round.setRoundId(keys.getInt(1));
                }
            }
        }
        return round.getRoundId();
    }

    public boolean update(MatchRound round) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setInt(1, round.getMatchId());
            statement.setInt(2, round.getRoundNumber());
            if (round.getWinningTeamId() > 0) {
                statement.setInt(3, round.getWinningTeamId());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            statement.setString(4, round.getWinningSide());
            statement.setString(5, round.getWinType());
            statement.setInt(6, round.getRoundId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int roundId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, roundId);
            return statement.executeUpdate() > 0;
        }
    }

    private MatchRound mapRow(ResultSet resultSet) throws SQLException {
        return new MatchRound(
                resultSet.getInt("round_id"),
                resultSet.getInt("match_id"),
                resultSet.getInt("round_number"),
                resultSet.getInt("winning_team_id"),
                resultSet.getString("winning_side"),
                resultSet.getString("win_type")
        );
    }
}
