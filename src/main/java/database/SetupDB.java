package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SetupDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:databases/bot.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();

                // Creazione tabella actors
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS actors (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        gender TEXT,
                        birthday TEXT,
                        deathday TEXT
                    );
                """);

                // Creazione tabella users
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        chat_id LONG PRIMARY KEY,
                        first_name VARCHAR(255) NOT NULL,
                        last_name VARCHAR(255),
                        username VARCHAR(255),
                        last_entry DATETIME,
                        language VARCHAR(255),
                        state TEXT
                    );
                """);

                // Creazione tabella series
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS series (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        rating DOUBLE
                    );
                """);

                // Creazione tabella episodes
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS episodes (
                        id INTEGER PRIMARY KEY,
                        serie_id INTEGER,
                        season INTEGER,
                        number INTEGER,
                        name TEXT,
                        FOREIGN KEY(serie_id) REFERENCES series(id)
                    );
                """);

                // Creazione tabella searches
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS searches (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        chat_id INTEGER NOT NULL,
                        entity_type TEXT,
                        entity_id INTEGER,
                        entity_name TEXT,
                        searched_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                // Creazione tabella user_favourites
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS user_favourites (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        chat_id INTEGER NOT NULL,
                        entity_type TEXT,
                        entity_id INTEGER,
                        add_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        entity_name TEXT
                    );
                """);

                System.out.println("Database e tabelle create con successo!");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}