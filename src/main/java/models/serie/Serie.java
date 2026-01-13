package models.serie;

import com.google.gson.annotations.SerializedName;
import models.episode.Episode;

public class Serie {
    private int id;
    private String name;
    private String url;
    private String type;
    private String language;
    private String[] genres;
    private String premiered;
    private String ended;
    private WebChannel network;   // Network
    private WebChannel webChannel;

    public static class EmbeddedEpisodes {
        private Episode previousepisode;
        private Episode nextepisode;

        public Episode getPreviousepisode() { return previousepisode; }
        public Episode getNextepisode() { return nextepisode; }
    }

    @SerializedName("_embedded")
    private EmbeddedEpisodes embedded;

    private Image image;
    private Rating rating;
    private String summary;

    // GET
    public int getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getType() { return type; }
    public String getLanguage() { return language; }
    public String[] getGenres() { return genres; }
    public String getPremiered() { return premiered; }
    public String getEnded() { return ended; }
    public WebChannel getWebChannel() { return webChannel; }
    public Episode getPreviousEpisode() { return embedded != null ? embedded.getPreviousepisode() : null; }
    public Episode getNextEpisode() { return embedded != null ? embedded.getNextepisode() : null; }
    public Image getImage() { return image; }
    public Rating getRating() { return rating; }
    public String getSummary() { return summary; }

    @Override
    public String toString() {
        String text = "*" + (name != null ? name : "N/A") + "*\n";

        if (genres != null && genres.length > 0)
            text += "_Generi:_ " + String.join(", ", genres) + "\n";

        text += "_Anteprima:_ " + (premiered != null ? premiered : "N/A") + "\n";
        text += "_Conclusa:_ " + (ended != null ? ended : "N/A") + "\n";

        if (webChannel != null)
            text += "_Disponibile su:_ [" + webChannel.getName() + "](" + (webChannel.getOfficialSite() != null ? webChannel.getOfficialSite() : "") + ")\n";

        if(embedded != null){
            if (embedded.getPreviousepisode() != null)
                text += "_Ultimo episodio:_ [" + embedded.getPreviousepisode().SEN() + "]\n";

            if (embedded.getNextepisode() != null)
                text += "_Prossimo episodio:_ [" + embedded.getNextepisode().SEN() + "]\n";
        }

        text += "_Rating:_ " + (rating != null && rating.getAverage() != null ? rating.getAverage().toString() : "N/A") + "\n";

        if (summary != null)
            text += "\n" + summary.replaceAll("<[^>]*>", "");   // Rimuove i tag HTML

        return text + "\n";
    }

}