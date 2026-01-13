package models.episode;

import bot.oldBot.Bot;
import com.google.gson.annotations.SerializedName;
import models.serie.Image;
import models.serie.Rating;
import models.serie.Serie;


public class Episode {

    private int id;
    private String name;
    private int season;
    private int number;

    private String airdate;
    private String airtime;
    private int runtime;

    private Image image;
    private Rating rating;
    private String summary;
    private String url;

    @SerializedName("_embedded")
    private EmbeddedShow embedded;

    @SerializedName("_links")
    private Links links;

    public Links getLinks() {
        return links;
    }

    public static class Links {
        public Show show;
        public Self self;

        public static class Show {
            public String href;
            public String name;
        }

        public static class Self {
            public String href;
        }
    }

    // GET
    public int getId() { return id; }
    public String getName() { return name; }
    public int getSeason() { return season; }
    public int getNumber() { return number; }

    public String getAirdate() { return airdate; }
    public String getAirtime() { return airtime; }
    public int getRuntime() { return runtime; }

    public Image getImage() { return image; }
    public Rating getRating() { return rating; }
    public String getSummary() { return summary; }
    public String getUrl() { return url; }

    public Serie getShow() {
        return embedded != null ? embedded.show : null;
    }

    public static class EmbeddedShow {
        public Serie show;
    }

    public int getSerieIdLinks() {
        if (links != null && links.show != null && links.show.href != null) {
            return Integer.parseInt(links.show.href.substring(links.show.href.lastIndexOf('/') + 1));
        }
        return -1;
    }

    public String getSerieNameLinks() {
        return links != null && links.show != null ? links.show.name : null;
    }

    public String SEN(){
        return "S" + season + "E" + number + " - " + name;
    }

    @Override
    public String toString() {
        String text = "";

        if(getShow() != null){
            String link = Bot.bot_url + "?start=serie_" + getShow().getId();
            text = "[" + getShow().getName() + "](" + link + ")\n";
        }

        text += "*S" + season + "E" + number + "* - _" + name + "_\n";

        if (airdate != null)
            text += "_Data:_ " + airdate + "\n";

        if (runtime > 0)
            text += "_Durata:_ " + runtime + " min\n";

        if (rating != null && rating.getAverage() != null)
            text += "_Rating:_ " + rating.getAverage() + "\n";

        if (summary != null)
            text += "\n" + summary.replaceAll("<[^>]*>", "");

        return text;
    }
}