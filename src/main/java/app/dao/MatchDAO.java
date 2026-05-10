package app.dao;

import models.Match;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchDAO extends AbstractDao {

    private static final String BASE_SELECT = "SELECT match_id, tournament_id, map_id, stage, match_date, team_a_id, team_b_id, winner_team_id FROM matches";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY match_id";
    private static final String INSERT = "INSERT INTO matches (tournament_id, map_id, stage, match_date, team_a_id, team_b_id, winner_team_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE matches SET tournament_id = ?, map_id = ?, stage = ?, match_date = ?, team_a_id = ?, team_b_id = ?, winner_team_id = ? WHERE match_id = ?";
    private static final String DELETE = "DELETE FROM matches WHERE match_id = ?";

    public List<Match> findAll() throws SQLException {
        List<Match> matches = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_ALL); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                matches.add(mapRow(resultSet));
            }
        }
        return matches;
    }

    public Optional<Match> findById(int matchId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE match_id = ?")) {
            statement.setInt(1, matchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Match> findByTournamentId(int tournamentId) throws SQLException {
        List<Match> matches = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE tournament_id = ? ORDER BY match_id")) {
            statement.setInt(1, tournamentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    matches.add(mapRow(resultSet));
                }
            }
        }
        return matches;
    }

    public int insert(Match match) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            bindNullableInt(statement, 1, match.getTournamentId());
            bindNullableInt(statement, 2, match.getMapId());
            statement.setString(3, match.getStage());
            if (match.getMatchDate() != null) {
                statement.setTimestamp(4, Timestamp.valueOf(match.getMatchDate()));
            } else {
                statement.setNull(4, java.sql.Types.TIMESTAMP);
            }
            bindNullableInt(statement, 5, match.getTeamAId());
            bindNullableInt(statement, 6, match.getTeamBId());
            bindNullableInt(statement, 7, match.getWinnerTeamId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    match.setMatchId(keys.getInt(1));
                }
            }
        }
        return match.getMatchId();
    }

    public boolean update(Match match) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            bindNullableInt(statement, 1, match.getTournamentId());
            bindNullableInt(statement, 2, match.getMapId());
            statement.setString(3, match.getStage());
            if (match.getMatchDate() != null) {
                statement.setTimestamp(4, Timestamp.valueOf(match.getMatchDate()));
            } else {
                statement.setNull(4, java.sql.Types.TIMESTAMP);
            }
            bindNullableInt(statement, 5, match.getTeamAId());
            bindNullableInt(statement, 6, match.getTeamBId());
            bindNullableInt(statement, 7, match.getWinnerTeamId());
            statement.setInt(8, match.getMatchId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int matchId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, matchId);
            return statement.executeUpdate() > 0;
        }
    }

    private Match mapRow(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("match_date");
        return new Match(
                resultSet.getInt("match_id"),
                resultSet.getInt("tournament_id"),
                resultSet.getInt("map_id"),
                resultSet.getString("stage"),
                timestamp == null ? null : timestamp.toLocalDateTime(),
                resultSet.getInt("team_a_id"),
                resultSet.getInt("team_b_id"),
                resultSet.getInt("winner_team_id")
        );
    }

    private void bindNullableInt(PreparedStatement statement, int parameterIndex, int value) throws SQLException {
        if (value > 0) {
            statement.setInt(parameterIndex, value);
        } else {
            statement.setNull(parameterIndex, java.sql.Types.INTEGER);
        }
    }
}
