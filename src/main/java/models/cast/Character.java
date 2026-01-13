package models.cast;
import models.serie.Image;

public class Character {
    private int id;
    private String name;
    public Image image;

    // GET
    public int getId() { return id; }
    public String getName() { return name; }
    public Image getImage() { return image; }

    @Override
    public String toString() {
        return name;
    }
}