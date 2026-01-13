package database;

import models.enumerable.UserState;
import models.user.User;

import java.sql.*;
import java.util.Objects;

public class UserDB {
    private final DB db;
    private final String table = "users";

    // CONTRUCTOR //
    public UserDB() {
        db = DB.getInstance();
    }

    // EXIST //
    public boolean userExists(long chatId) {
        String sql = "SELECT 1 FROM users WHERE chat_id = ? LIMIT 1";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, chatId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo d'esistenza dell'utente", e);
        }
    }

    // ADD OR UPDATE //
    public void addOrUpdateUser(User user) {
        if (userExists(user.getChatId())) {
            updateUser(user);
        } else {
            insertUser(user);
        }
    }

    // CREATE/ADD //
    private void insertUser(User user) {
        String sql = """
            INSERT INTO users(chat_id, first_name, last_name, username, language, state, last_entry)
            VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP)
        """;
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, user.getChatId());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getUsername());
            stmt.setString(5, user.getLanguage());
            stmt.setString(6, user.getState().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore insertUser", e);
        }
    }

    // GET/READ //
    // USER
    public User getUser(long chatId) {
        String sql = "SELECT * FROM users WHERE chat_id = ? LIMIT 1";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, chatId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getLong("chat_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("language")
                );
                user.setState(UserState.valueOf(rs.getString("state")));
                return user;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore getUser", e);
        }
    }

    // STATE
    private String getCurrentState(long chatId) {
        String sql = "SELECT state FROM users WHERE chat_id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, chatId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("state") : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE //
    // USER
    private void updateUser(User user) {
        String sql = """
            UPDATE users
            SET first_name=?, last_name=?, username=?, language=?, state=?, last_entry=CURRENT_TIMESTAMP
            WHERE chat_id=?
        """;
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getUsername());
            stmt.setString(4, user.getLanguage());
            stmt.setString(5, user.getState().name());
            stmt.setLong(6, user.getChatId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore updateUser", e);
        }
    }

    // STATE
    public void updateUserState(long chatId, UserState state) {
        if (Objects.equals(getCurrentState(chatId), state.name())) return;

        String sql = "UPDATE users SET state=? WHERE chat_id=?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, state.name());
            stmt.setLong(2, chatId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore updateUserState", e);
        }
    }

    // LAST ENTRY
    public void updateLastEntry(long chatId) {
        String sql = "UPDATE users SET last_entry=CURRENT_TIMESTAMP WHERE chat_id=?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, chatId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore updateLastEntry", e);
        }
    }
}
