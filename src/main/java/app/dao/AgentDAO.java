package app.dao;

import models.Agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AgentDAO extends AbstractDao {

    private static final String BASE_SELECT = "SELECT agent_id, name, role FROM agents";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY agent_id";
    private static final String INSERT = "INSERT INTO agents (name, role) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE agents SET name = ?, role = ? WHERE agent_id = ?";
    private static final String DELETE = "DELETE FROM agents WHERE agent_id = ?";

    public List<Agent> findAll() throws SQLException {
        List<Agent> agents = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_ALL); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                agents.add(mapRow(resultSet));
            }
        }
        return agents;
    }

    public Optional<Agent> findById(int agentId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE agent_id = ?")) {
            statement.setInt(1, agentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public int insert(Agent agent) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, agent.getName());
            statement.setString(2, agent.getRole());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    agent.setAgentId(keys.getInt(1));
                }
            }
        }
        return agent.getAgentId();
    }

    public boolean update(Agent agent) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, agent.getName());
            statement.setString(2, agent.getRole());
            statement.setInt(3, agent.getAgentId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int agentId) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, agentId);
            return statement.executeUpdate() > 0;
        }
    }

    private Agent mapRow(ResultSet resultSet) throws SQLException {
        return new Agent(
                resultSet.getInt("agent_id"),
                resultSet.getString("name"),
                resultSet.getString("role")
        );
    }
}
