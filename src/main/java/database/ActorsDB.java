package database;

import models.cast.Actor;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ActorsDB {
    private final DB db;
    private final String table = "actors";

    // CONSTRUCTOR //
    public ActorsDB() {
        db = DB.getInstance();
    }

    // EXISTS //
    public boolean actorExists(int id) {
        String sql = "SELECT 1 FROM " + table + " WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo d'esistenza dell'attore", e);
        }
    }

    // ADD OR UPDATE //
    public void addOrUpdateActor(Actor actor) {
        if (actorExists(actor.getId())) {
            updateActor(actor);
        } else {
            insertActor(actor);
        }
    }

    // GET ACTOR NAME //
    public String getActorName(int id) {
        String sql = "SELECT name FROM " + table + " WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura del nome dell'attore", e);
        }
    }

    // INSERT //
    public void insertActor(Actor actor) {
        String sql = "INSERT INTO " + table + " (id, name, birthday, deathday, gender) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, actor.getId());
            stmt.setString(2, actor.getName());
            stmt.setString(3, actor.getBirthday());
            stmt.setString(4, actor.getDeathday());
            stmt.setString(5, actor.getGender());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'inserimento dell'attore", e);
        }
    }

    // UPDATE //
    public void updateActor(Actor actor) {
        String sql = "UPDATE " + table + " SET name = ?, birthday = ?, deathday = ?, gender = ? WHERE id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, actor.getName());
            stmt.setString(2, actor.getBirthday());
            stmt.setString(3, actor.getDeathday());
            stmt.setString(4, actor.getGender());
            stmt.setInt(5, actor.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornamento dell'attore", e);
        }
    }
}