package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = System.getenv().getOrDefault("VALORANT_DB_URL", "jdbc:mysql://localhost:3306/valorant_esports");
    private static final String USER = System.getenv().getOrDefault("VALORANT_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("VALORANT_DB_PASSWORD", "");

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connection Successful: Linked to valorant_esports!");
        } catch (SQLException e) {
            System.err.println("❌ Connection Failed. Check your password or ensure MySQL is running.");
            e.printStackTrace();
        }
        return connection;
    }
}