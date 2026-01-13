package models.credits;

import bot.Bot;
import com.google.gson.annotations.SerializedName;
import models.episode.Episode;

public class GuestCredits {

    @SerializedName("_links")
    private Links links;

    @SerializedName("_embedded")
    private Embedded embedded;

    // GET
    public Episode getEpisode() {
        return embedded != null ? embedded.episode : null;
    }
    public Links getLinks() {
        return links;
    }

    // ID
    private int extractId(String href) {
        if (href == null || href.isEmpty()) return -1;
        return Integer.parseInt(href.substring(href.lastIndexOf('/') + 1));
    }

    public int getEpisodeId() {
        return getEpisode() != null ? getEpisode().getId() : -1;
    }

    public int getCharacterId() {
        return links != null && links.character != null ? extractId(links.character.href) : -1;
    }

    public int getShowId() {
        return getEpisode() != null && getEpisode().getLinks() != null && getEpisode().getLinks().show != null ? extractId(getEpisode().getLinks().show.href) : -1;
    }

    // NAME
    public String getCharacterName() {
        return links != null && links.character != null ? links.character.name : null;
    }

    public String getShowName() {
        return getEpisode() != null && getEpisode().getLinks() != null && getEpisode().getLinks().show != null ? getEpisode().getLinks().show.name : null;
    }

    public String getEpisodeName() {
        return getEpisode() != null ? getEpisode().getName() : null;
    }

    //
    public static class Embedded {
        public Episode episode;
    }

    public static class Links {
        public Link character;
        public Link episode; // opzionale

        public static class Link {
            public String href;
            public String name;
        }
    }

    //
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Serie
        if (getShowName() != null && !getShowName().isEmpty()) {
            if (getShowId() != -1) {
                String serielink = Bot.bot_url + "?start=serie_" + getShowId();
                sb.append("*Serie TV*: [").append(getShowName()).append("](").append(serielink).append(")\n");
            } else {
                sb.append("*Serie TV*: ").append(getShowName()).append("\n");
            }
        }

        // Episodio
        if (getEpisode() != null && getEpisodeName() != null && !getEpisodeName().isEmpty()) {
            sb.append("\t*Episode*: ");
            if (getEpisodeId() != -1) {
                String episodelink = Bot.bot_url + "?start=episode_" + getEpisodeId();
                sb.append("[").append(getEpisode().SEN()).append("](").append(episodelink).append(")\n");
            } else {
                sb.append(getEpisode().SEN()).append("\n");
            }
        }

        // Character
        if (getCharacterName() != null && !getCharacterName().isEmpty()) {
            sb.append("\t*Character*: ");
            if (getCharacterId() != -1) {
                String characterlink = Bot.bot_url + "?start=character_" + getCharacterId();
                sb.append("[").append(getCharacterName()).append("](").append(characterlink).append(")\n");
            } else {
                sb.append(getCharacterName()).append("\n");
            }
        }

        return !sb.isEmpty() ? sb.append("\n").toString() : "";
    }
}