package app.dao;

import models.Organization;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrganizationDAO extends AbstractDao {

    private static final String BASE_SELECT = "SELECT org_id, name, region FROM organizations";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY org_id";
    private static final String INSERT = "INSERT INTO organizations (name, region) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE organizations SET name = ?, region = ? WHERE org_id = ?";
    private static final String DELETE = "DELETE FROM organizations WHERE org_id = ?";

    public List<Organization> findAll() throws SQLException {
        List<Organization> organizations = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_ALL); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                organizations.add(mapRow(resultSet));
            }
        }
        return organizations;
    }

    public Optional<Organization> findById(int orgId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE org_id = ?")) {
            statement.setInt(1, orgId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public int insert(Organization organization) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, organization.getName());
            statement.setString(2, organization.getRegion());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    organization.setOrgId(keys.getInt(1));
                }
            }
        }
        return organization.getOrgId();
    }

    public boolean update(Organization organization) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, organization.getName());
            statement.setString(2, organization.getRegion());
            statement.setInt(3, organization.getOrgId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int orgId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, orgId);
            return statement.executeUpdate() > 0;
        }
    }

    private Organization mapRow(ResultSet resultSet) throws SQLException {
        return new Organization(
                resultSet.getInt("org_id"),
                resultSet.getString("name"),
                resultSet.getString("region")
        );
    }
}
