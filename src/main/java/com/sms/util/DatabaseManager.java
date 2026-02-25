package com.sms.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and schema creation.
 * Uses a single shared connection (singleton-style).
 */
public class DatabaseManager {

    private static final String DATA_DIR  = "data";
    private static final String DB_FILE   = DATA_DIR + File.separator + "students.db";
    private static Connection connection;

    // Private constructor – no instances needed
    private DatabaseManager() {}

    /**
     * Returns the shared SQLite connection, creating it if needed.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Make sure the data folder exists
            new File(DATA_DIR).mkdirs();

            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            connection.setAutoCommit(true);

            AppLogger.info("Database connection opened: " + DB_FILE);
            createSchema(connection);
        }
        return connection;
    }

    /**
     * Creates the students table if it does not already exist.
     * All constraints are enforced at the database level.
     */
    private static void createSchema(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS students (
                    student_id   TEXT    PRIMARY KEY NOT NULL,
                    full_name    TEXT    NOT NULL,
                    programme    TEXT    NOT NULL,
                    level        INTEGER NOT NULL CHECK(level IN (100,200,300,400,500,600,700)),
                    gpa          REAL    NOT NULL CHECK(gpa >= 0.0 AND gpa <= 4.0),
                    email        TEXT    NOT NULL,
                    phone_number TEXT    NOT NULL,
                    date_added   TEXT    NOT NULL,
                    status       TEXT    NOT NULL DEFAULT 'Active'
                );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            AppLogger.info("Database schema verified/created.");
        }
    }

    /**
     * Closes the connection cleanly on app shutdown.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                AppLogger.info("Database connection closed.");
            } catch (SQLException e) {
                AppLogger.error("Error closing database connection: " + e.getMessage());
            }
        }
    }
}
