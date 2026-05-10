package app.dao;

import models.Tournament;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TournamentDAO extends AbstractDao {

    private static final String BASE_SELECT = "SELECT tournament_id, name, start_date, end_date, prize_pool FROM tournaments";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY tournament_id";
    private static final String INSERT = "INSERT INTO tournaments (name, start_date, end_date, prize_pool) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE tournaments SET name = ?, start_date = ?, end_date = ?, prize_pool = ? WHERE tournament_id = ?";
    private static final String DELETE = "DELETE FROM tournaments WHERE tournament_id = ?";

    public List<Tournament> findAll() throws SQLException {
        List<Tournament> tournaments = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_ALL); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tournaments.add(mapRow(resultSet));
            }
        }
        return tournaments;
    }

    public Optional<Tournament> findById(int tournamentId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE tournament_id = ?")) {
            statement.setInt(1, tournamentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public int insert(Tournament tournament) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            if (tournament.getName() != null) {
                statement.setString(1, tournament.getName());
            } else {
                statement.setNull(1, java.sql.Types.VARCHAR);
            }
            if (tournament.getStartDate() != null) {
                statement.setDate(2, Date.valueOf(tournament.getStartDate()));
            } else {
                statement.setNull(2, java.sql.Types.DATE);
            }
            if (tournament.getEndDate() != null) {
                statement.setDate(3, Date.valueOf(tournament.getEndDate()));
            } else {
                statement.setNull(3, java.sql.Types.DATE);
            }
            statement.setDouble(4, tournament.getPrizePool());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    tournament.setTournamentId(keys.getInt(1));
                }
            }
        }
        return tournament.getTournamentId();
    }

    public boolean update(Tournament tournament) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, tournament.getName());
            if (tournament.getStartDate() != null) {
                statement.setDate(2, Date.valueOf(tournament.getStartDate()));
            } else {
                statement.setNull(2, java.sql.Types.DATE);
            }
            if (tournament.getEndDate() != null) {
                statement.setDate(3, Date.valueOf(tournament.getEndDate()));
            } else {
                statement.setNull(3, java.sql.Types.DATE);
            }
            statement.setDouble(4, tournament.getPrizePool());
            statement.setInt(5, tournament.getTournamentId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int tournamentId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, tournamentId);
            return statement.executeUpdate() > 0;
        }
    }

    private Tournament mapRow(ResultSet resultSet) throws SQLException {
        Date startDate = resultSet.getDate("start_date");
        Date endDate = resultSet.getDate("end_date");
        return new Tournament(
                resultSet.getInt("tournament_id"),
                resultSet.getString("name"),
                startDate == null ? null : startDate.toLocalDate(),
                endDate == null ? null : endDate.toLocalDate(),
                resultSet.getDouble("prize_pool")
        );
    }
}
