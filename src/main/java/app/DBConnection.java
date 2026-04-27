package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // The URL points to your local MySQL server and the specific database Umais designed
    private static final String URL = "jdbc:mysql://localhost:3306/valorant_esports";

    // Default MySQL username is usually 'root'
    private static final String USER = "root";

    // ⚠️ IMPORTANT: Change this to the actual password you set when installing MySQL!
    private static final String PASSWORD = "211319131";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // This line actually attempts to log into your database
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connection Successful: Linked to valorant_esports!");
        } catch (SQLException e) {
            System.err.println("❌ Connection Failed. Check your password or ensure MySQL is running.");
            e.printStackTrace();
        }
        return connection;
    }
}