package app.dao;

import app.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

abstract class AbstractDao {

    protected Connection openConnection() throws SQLException {
        Connection connection = DBConnection.getConnection();
        if (connection == null) {
            throw new SQLException("Unable to obtain a database connection.");
        }
        return connection;
    }
}
