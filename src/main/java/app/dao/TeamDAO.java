package app.dao;

import models.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamDAO extends AbstractDao {

    private static final String BASE_SELECT = "SELECT team_id, org_id, name FROM teams";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY team_id";
    private static final String INSERT = "INSERT INTO teams (org_id, name) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE teams SET org_id = ?, name = ? WHERE team_id = ?";
    private static final String DELETE = "DELETE FROM teams WHERE team_id = ?";

    public List<Team> findAll() throws SQLException {
        List<Team> teams = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_ALL); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                teams.add(mapRow(resultSet));
            }
        }
        return teams;
    }

    public Optional<Team> findById(int teamId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE team_id = ?")) {
            statement.setInt(1, teamId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Team> findByOrganizationId(int orgId) throws SQLException {
        List<Team> teams = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE org_id = ? ORDER BY team_id")) {
            statement.setInt(1, orgId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    teams.add(mapRow(resultSet));
                }
            }
        }
        return teams;
    }

    public int insert(Team team) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            if (team.getOrgId() > 0) {
                statement.setInt(1, team.getOrgId());
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            statement.setString(2, team.getName());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    team.setTeamId(keys.getInt(1));
                }
            }
        }
        return team.getTeamId();
    }

    public boolean update(Team team) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            if (team.getOrgId() > 0) {
                statement.setInt(1, team.getOrgId());
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            statement.setString(2, team.getName());
            statement.setInt(3, team.getTeamId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int teamId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, teamId);
            return statement.executeUpdate() > 0;
        }
    }

    private Team mapRow(ResultSet resultSet) throws SQLException {
        return new Team(
                resultSet.getInt("team_id"),
                resultSet.getInt("org_id"),
                resultSet.getString("name")
        );
    }
}
