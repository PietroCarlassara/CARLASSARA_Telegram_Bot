package models.cast;

import models.serie.Image;

import java.time.LocalDate;
import java.time.Period;

public class Actor {
    private int id;
    private String name;
    private String birthday;
    private String deathday;
    private Country country;
    private Image image;
    private String gender;

    // GET
    public int getId() { return id; }
    public String getName() { return name; }
    public String getBirthday() { return birthday; }
    public String getDeathday() { return deathday; }
    public Country getCountry() { return country; }
    public Image getImage() { return image; }
    public String getGender() { return gender; }

    // Classe Country
    public static class Country {
        private String name;

        public String getName() { return name; }
    }

    @Override
    public String toString() {
        String text = "";

        // Nome attore
        text += "*" + (name != null ? name : "N/A") + "*\n";
        if(getGender() != null)
            text += "*" + getGender() + "*" + "\n";

        if(birthday != null){
            if(deathday != null)
                text += "_Età_: " + Period.between(LocalDate.parse(birthday), LocalDate.parse(deathday)).getYears() + "\n";
            else
                text += "_Età_: " + Period.between(LocalDate.parse(birthday), LocalDate.now()).getYears() + "\n";
        }

        if (birthday != null)
            text += "_Data di nascita:_ " + birthday + "\n";

        if (deathday != null)
            text += "_Data di morte:_ " + deathday + "\n";

        if (country != null && country.getName() != null)
            text += "_Paese:_ " + country.getName() + "\n";

        return text + "\n";
    }


}
