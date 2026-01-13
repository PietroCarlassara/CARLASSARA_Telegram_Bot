package api;
import models.cast.Actor;
import models.cast.Cast;
import models.cast.SearchActorResult;
import models.credits.CastCredits;
import models.credits.GuestCredits;
import models.episode.Episode;
import models.serie.SearchResult;

// Gson
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.serie.Serie;
import models.cast.Character;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class API {
    private static final String BASE_URL = "https://api.tvmaze.com";
    private final HttpClient client;
    private final Gson gson;

    public API() {
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder().create();
    }

    // Classe template (generics)
    // <T> il metodo utilizza un tipo generico
    // T tipo generico, ritorna un tipo generico, deciso in runtime
    private <T> T get(String endpoint, Class<T> classOfT, int StatusCode) {
        HttpResponse<String> response = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != StatusCode) {
                System.err.println("Errore: " + response.statusCode());
                return null;
            }

            return gson.fromJson(response.body(), classOfT);

        } catch (IOException | InterruptedException e) {
            System.err.println("Errore" + e.getMessage());
            return null;
        }
    }

    // https://api.tvmaze.com/search/shows?q=Game+of+Thrones
    public SearchResult[] getSeries(String serie) {
        // Codifica, gli spazi vengono rimpiazzati con il +
        serie = URLEncoder.encode(serie, StandardCharsets.UTF_8);
        String endpoint = "/search/shows?q=" + serie;
        return this.get(endpoint,  SearchResult[].class, 200);
    }

    // https://api.tvmaze.com/shows/82?embed[]=previousepisode&embed[]=nextepisode
    public Serie getSerieById(int id) {
        String endpoint = "/shows/" + id + "?embed[]=previousepisode&embed[]=nextepisode";
        return this.get(endpoint, Serie.class, 200);
    }

    // https://api.tvmaze.com/shows/82/cast
    public Cast[] getMainCastBySerieId(int id){
        String endpoint = "/shows/" + id + "/cast";
        return this.get(endpoint, Cast[].class, 200);
    }

    // Guest Cast dell'episodio
    // /episodes/{EPISODE_ID}/guestcast
    // https://api.tvmaze.com/episodes/1/guestcast
    public Cast[] getEpisodeCastByEpisodeId(int id) {
        String endpoint = "/episodes/" + id + "/guestcast";
        return this.get(endpoint, Cast[].class, 200);
    }

    // https://api.tvmaze.com/search/people?q=Bryan+Cranston
    public SearchActorResult[] getActors(String name) {
        name = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String endpoint = "/search/people?q=" + name;
        return this.get(endpoint, SearchActorResult[].class, 200);
    }

    // https://api.tvmaze.com/people/1
    public Actor getActorById(int id) {
        String endpoint = "/people/" + id;
        return this.get(endpoint, Actor.class, 200);
    }

    // Cast credits (serie + personaggi)
    // /people/{PERSON_ID}/castcredits?embed=show
    // https://api.tvmaze.com/people/1/castcredits?embed=show
    public CastCredits[] getActorCastCredits(int id) {
        String endpoint = "/people/" + id + "/castcredits?embed=show";
        return this.get(endpoint, CastCredits[].class, 200);
    }

    // Guest cast credits (episodi + personaggi)
    // /people/{PERSON_ID}/guestcastcredits?embed=episode
    // https://api.tvmaze.com/people/1/guestcastcredits?embed=episode
    public GuestCredits[] getActorGuestCredits(int id) {
        String endpoint = "/people/" + id + "/guestcastcredits?embed=episode";
        return this.get(endpoint, GuestCredits[].class, 200);
    }

    // Episodio da serie + stagione + numero
    // /shows/{SHOW_ID}/episodebynumber?season={SEASON}&number={EPISODE}
    // https://api.tvmaze.com/shows/82/episodebynumber?season=1&number=1
    public Episode getEpisodeByNumber(int serie_id, int season, int number) {
        String endpoint = "/shows/" + serie_id + "/episodebynumber?season=" + season + "&number=" + number;
        return this.get(endpoint, Episode.class, 200);
    }

    // Episodio completo + info serie
    // /episodes/{EPISODE_ID}?embed=show
    // https://api.tvmaze.com/episodes/4952?embed=show
    public Episode getEpisodeById(int id) {
        String endpoint = "/episodes/" + id + "?embed=show";
        return this.get(endpoint, Episode.class, 200);
    }

    // Character
    // /characters/{CHARACTER_ID}
    // https://api.tvmaze.com/characters/1
    public Character getCharacterById(int id) {
        String endpoint = "/characters/" + id;
        return this.get(endpoint, Character.class, 200);
    }
}