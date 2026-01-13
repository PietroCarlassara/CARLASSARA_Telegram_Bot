package database;

import java.sql.*;

public class DB {
    private static final String DB_URL = "jdbc:sqlite:databases/bot.db";
    private static DB instance;
    private Connection connection;

    private DB() {
        openConnection();
    }

    public static DB getInstance() {
        if (instance == null) {
            instance = new DB();
        }
        return instance;
    }

    // Apre la connessione
    private void openConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            throw new RuntimeException("Errore apertura DB", e);
        }
    }

    // Controllo e in caso riapertura della connessione
    private void checkConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Riapertura della connessione");
                openConnection();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo della connessione", e);
        }
    }

    public Connection getConnection() {
        checkConnection();
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la chiusura della connessione", e);
        }
    }
}
