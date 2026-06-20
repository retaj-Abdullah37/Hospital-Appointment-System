package projectcode1.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection class - Manages Oracle database connections
 * Demonstrates: Singleton Pattern, Exception Handling
 *
 * This class provides a single connection instance to the Oracle database
 * Connection details: localhost:1521/XE, SYSTEM/SYSTEM2005
 *
 * @author CS313 Term Project
 */
public class DatabaseConnection {

    // Singleton instance
    private static DatabaseConnection instance;
    private Connection connection;

    // Oracle database connection details
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DB_USER = "SYSTEM";
    private static final String DB_PASSWORD = "SYSTEM2005";

    /**
     * Private constructor - Singleton pattern
     * Prevents direct instantiation
     */
    private DatabaseConnection() {
        try {
            // Load Oracle JDBC driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            // Establish connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection established successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found!");
            System.err.println("Please add ojdbc8.jar to the project library.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get singleton instance of DatabaseConnection
     * Synchronized to ensure thread-safety
     * @return DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Get the database connection
     * @return Connection object
     * @throws SQLException if connection is closed or unavailable
     */
    public Connection getConnection() throws SQLException {
        // Check if connection is still valid
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (SQLException e) {
                System.err.println("Failed to re-establish database connection!");
                throw e;
            }
        }
        return connection;
    }

    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection!");
            e.printStackTrace();
        }
    }

    /**
     * Test database connection
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed!");
            e.printStackTrace();
            return false;
        }
    }
}
