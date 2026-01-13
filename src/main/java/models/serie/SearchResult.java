package models.serie;

public class SearchResult {
    private double score;   // Indica l'indice di rilevanza della ricerca
    private Serie show;

    // GET
    public double getScore() { return score; }
    public Serie getShow() { return show; }

    @Override
    public String toString() {
        if (show == null) return "_Serie non disponibile_";

        // Markdown
        return show.toString();
    }

}
