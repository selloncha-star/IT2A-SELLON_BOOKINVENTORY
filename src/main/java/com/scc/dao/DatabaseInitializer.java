package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initializeDatabase() {
        String usersSql = """
                CREATE TABLE IF NOT EXISTS users (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles    TEXT NOT NULL,
                    status   TEXT NOT NULL DEFAULT 'PENDING'
                )""";

        String booksSql = """
                CREATE TABLE IF NOT EXISTS books (
                    book_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    title        TEXT NOT NULL,
                    author       TEXT NOT NULL,
                    category     TEXT NOT NULL,
                    quantity     INTEGER NOT NULL,
                    availability TEXT NOT NULL
                )""";

        String logsSql = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id     INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp   DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )""";

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(usersSql);
            stmt.execute(booksSql);
            stmt.execute(logsSql);
        } catch (SQLException e) {
            System.err.println("Database setup failed: " + e.getMessage());
        }
    }
}