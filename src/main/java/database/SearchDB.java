package database;

import models.enumerable.Entity;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SearchDB {
    private final DB db;
    private final String table = "searches";

    // CONSTRUCTOR //
    public SearchDB() {
        db = DB.getInstance();
    }

    // INSERT //
    public void addSearch(long chatId, Entity entityType, int entityId, String entityName) {
        String sql = "INSERT INTO " + table + " (chat_id, entity_type, entity_id, entity_name, searched_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, chatId);
            stmt.setString(2, entityType.name());
            stmt.setInt(3, entityId);
            stmt.setString(4, entityName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'inserimento della ricerca", e);
        }
    }

    // RECUPERO ENTITÀ PIÙ RICERCATA //
    private String getMostSearchedEntityByType(String entityType) {
        String sql = "SELECT entity_name, COUNT(*) AS total FROM " + table + " WHERE entity_type = ? GROUP BY entity_id ORDER BY total DESC LIMIT 1";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, entityType);
            var rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("entity_name");
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dell'entità più cercata", e);
        }
    }

    public String getMostSearchedActor() {
        return getMostSearchedEntityByType(Entity.ACTOR.name());
    }

    public String getMostSearchedSerie() {
        return getMostSearchedEntityByType(Entity.SERIE.name());
    }

    public String getMostSearchedEpisode() {
        return getMostSearchedEntityByType(Entity.EPISODE.name());
    }
}
