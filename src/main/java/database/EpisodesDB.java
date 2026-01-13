package database;

import models.episode.Episode;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class EpisodesDB {
    private final DB db;
    private final String table = "episodes";

    // CONSTRUCTOR
    public EpisodesDB() {
        db = DB.getInstance();
    }

    // CHECK IF EPISODE EXISTS
    public boolean episodeExists(Integer id) {
        if (id == null) return false;
        String sql = "SELECT 1 FROM " + table + " WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo d'esistenza dell'episodio", e);
        }
    }

    // ADD OR UPDATE
    public void addOrUpdateEpisode(Episode episode) {
        if (episode == null || episode.getId() == -1) return;

        if (episodeExists(episode.getId())) {
            updateEpisode(episode);
        } else {
            insertEpisode(episode);
        }
    }

    // GET //
    public String getEpisodeName(int id) {
        String sql = "SELECT name FROM " + table + " WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            } else {
                return null; // episodio non trovato
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura del nome dell'episodio", e);
        }
    }

    // INSERT EPISODE
    public void insertEpisode(Episode episode) {
        String insertSql = "INSERT INTO " + table + " (id, serie_id, season, number, name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(insertSql)) {
            stmt.setInt(1, episode.getId());
            stmt.setObject(2, episode.getSerieIdLinks());
            stmt.setObject(3, episode.getSeason());
            stmt.setObject(4, episode.getNumber());
            stmt.setObject(5, episode.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inserimento dell'episodio", e);
        }
    }

    // UPDATE EPISODE
    public void updateEpisode(Episode episode) {
        String updateSql = "UPDATE " + table + " SET serie_id = ?, season = ?, number = ?, name = ? WHERE id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(updateSql)) {
            stmt.setInt(1, episode.getId());
            stmt.setObject(2, episode.getSeason());
            stmt.setObject(3, episode.getNumber());
            stmt.setObject(4, episode.getName());
            stmt.setObject(5, episode.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento dell'episodio", e);
        }
    }
}