package database;

import models.serie.Serie;
import models.user.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class SeriesDB {
    private final DB db;
    private final String table = "series";

    // CONTRUCTOR //
    public SeriesDB() {
        db = DB.getInstance();
    }

    // EXISTS //
    public boolean serieExists(int id) {
        String sql = "SELECT 1 FROM " + table + " WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo d'esistenza dell'utente", e);
        }
    }

    // ADD OR UPDATE //
    public void addOrUpdateSerie(Serie serie) {
        if (serieExists(serie.getId())) {
            updateSerie(serie);
        } else {
            insertSerie(serie);
        }
    }

    // GET //
    public String getSerieName(int id) {
        String sql = "SELECT name FROM " + table + " WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            } else {
                return null; // serie non trovata
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura del nome della serie", e);
        }
    }

    // INSERT //
    public void insertSerie(Serie serie) {
        String insertSql = "INSERT INTO " + table + " (id, name, rating) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(insertSql)) {
            stmt.setInt(1, serie.getId());
            stmt.setString(2, serie.getName());

            if (serie.getRating() != null) {
                stmt.setDouble(3, serie.getRating().getAverage());
            } else {
                stmt.setNull(3, Types.DOUBLE);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inserimento della serie", e);
        }
    }

    // UPDATE //
    public void updateSerie(Serie serie) {
        String updateSql = "UPDATE " + table + " SET name = ?, rating = ? WHERE id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(updateSql)) {
            stmt.setString(1, serie.getName());
            if (serie.getRating() != null) {
                stmt.setDouble(2, serie.getRating().getAverage());
            } else {
                stmt.setNull(2, Types.DOUBLE);
            }
            stmt.setInt(3, serie.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della serie", e);
        }
    }
}
