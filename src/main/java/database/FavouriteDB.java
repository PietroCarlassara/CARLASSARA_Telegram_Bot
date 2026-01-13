package database;

import models.enumerable.Entity;

import javax.xml.stream.events.EntityReference;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FavouriteDB {
    private final DB db;
    private final String table = "user_favourites";

    // DBs
    ActorsDB actorsDB =  new ActorsDB();
    EpisodesDB episodesDB = new EpisodesDB();
    SeriesDB seriesDB = new SeriesDB();

    // CONSTRUCTOR //
    public FavouriteDB() {
        db = DB.getInstance();
    }

    // EXISTS (per chat_id + entity_type + entity_id)
    public boolean favouriteExists(long chatId, Entity entityType, int entityId) {
        String sql = "SELECT 1 FROM " + table + " WHERE chat_id = ? AND entity_type = ? AND entity_id = ? LIMIT 1";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, chatId);
            stmt.setString(2, entityType.name());
            stmt.setInt(3, entityId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo dell'esistenza del preferito", e);
        }
    }

    // ADD //
    public void addFavourite(long chatId, Entity entityType, int entityId) {
        StringBuilder entityName = new StringBuilder();
        switch (entityType) {
            case ACTOR:
                entityName.append(actorsDB.getActorName(entityId));
                break;
            case EPISODE:
                entityName.append(episodesDB.getEpisodeName(entityId));
                break;
            case SERIE:
                entityName.append(seriesDB.getSerieName(entityId));
                break;
        }

        if (!favouriteExists(chatId, entityType, entityId)) {
            String sql = "INSERT INTO " + table + " (chat_id, entity_type, entity_id, entity_name, add_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
                stmt.setLong(1, chatId);
                stmt.setString(2, entityType.name());
                stmt.setInt(3, entityId);
                stmt.setString(4, entityName.toString());
                stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Errore nell'inserimento del preferito", e);
            }
        }
    }

    // REMOVE //
    public void removeFavourite(long chatId, Entity entityType, int entityId) {
        String sql = "DELETE FROM " + table + " WHERE chat_id = ? AND entity_type = ? AND entity_id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, chatId);
            stmt.setString(2, entityType.name());
            stmt.setInt(3, entityId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella rimozione del preferito", e);
        }
    }

    // GET FAVOURITES BY ENTITY TYPE //
    public List<String> getFavouritesByType(long chatId, Entity type) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT entity_name, entity_id FROM " + table + " WHERE chat_id = ? AND entity_type = ? ORDER BY add_at DESC";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, chatId);
            stmt.setString(2, type.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("entity_id") + " - " + rs.getString("entity_name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dei preferiti", e);
        }
        return list;
    }

}