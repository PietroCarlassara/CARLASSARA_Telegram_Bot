package models.credits;

import bot.Bot;
import com.google.gson.annotations.SerializedName;
import models.serie.Serie;

public class CastCredits {
    private boolean self;
    private boolean voice;

    @SerializedName("_embedded")
    private EmbeddedShow embedded;

    @SerializedName("_links")
    private Links links;

    public boolean isSelf() { return self; }
    public boolean isVoice() { return voice; }
    public Links getLinks() { return links; }

    public Serie getSerie() { return embedded != null ? embedded.show : null; }

    public static class EmbeddedShow {
        public Serie show;
    }

    // Classe Links interna
    public static class Links {
        private Link show;
        private Link character;

        public Link getShow() { return show; }
        public Link getCharacter() { return character; }

        // Classe Link interna
        public static class Link {
            private String href;
            private String name;

            public String getHref() { return href; }
            public String getName() { return name; }
        }
    }

    // ID
    private int extractId(String href) {
        if (href == null || href.isEmpty()) return -1;
        return Integer.parseInt(href.substring(href.lastIndexOf('/') + 1));
    }

    public int getCharacterId() {
        return links != null && links.character != null ? extractId(links.character.href) : -1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Serie TV
        if (getSerie() != null) {
            String serieName = getSerie().getName() != null ? getSerie().getName().trim() : "N/A";
            if (getSerie().getId() != -1) {
                String serieLink = Bot.bot_url + "?start=serie_" + getSerie().getId();
                sb.append("Serie TV: [").append(serieName).append("](").append(serieLink).append(")\n");
            } else {
                sb.append("Serie TV: ").append(serieName).append("\n");
            }
        } else {
            sb.append("Serie TV: N/A\n");
        }

        // Character
        if(links != null && links.getCharacter() != null && links.getCharacter().getName() != null) {
            if(getCharacterId() != -1){
                String characterLink = Bot.bot_url + "?start=character_" + getCharacterId();
                sb.append("\tCharacter: [").append(links.getCharacter().getName()).append("](").append(characterLink).append(")").append("\n");
            }
            else
                sb.append("\tCharacter: ").append(links.getCharacter().getName()).append("\n");
        }
        else {
            sb.append("\tCharacter: N/A\n");
        }

        return sb.toString();
    }
}


