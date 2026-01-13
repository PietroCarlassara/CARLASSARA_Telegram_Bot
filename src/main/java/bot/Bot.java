// Package
package bot;
import config.Config;
import api.API;
import database.*;
import models.cast.Character;
import models.credits.CastCredits;
import models.credits.GuestCredits;
import models.enumerable.Entity;
import models.episode.Episode;

// Per connettersi a Telegram usando HTTP
import models.cast.Actor;
import models.cast.Cast;
import models.cast.SearchActorResult;
import models.enumerable.Prefix;
import models.enumerable.UserState;
import models.serie.SearchResult;
import models.serie.Serie;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
// Interfaccia che riceve i messaggi in long polling
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
//
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
// Classe per costruire i messaggi da inviare
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
// Aggiornamento
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
// BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
// Eccezione
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
// Interfaccia generica del client Telegram
import org.telegram.telegrambots.meta.generics.TelegramClient;
// Buttons
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static models.enumerable.Entity.*;

// Polling(Sondaggio): Verifica ciclica, il bot chiede al server ogni x secondi se ci sono nuovi messaggi
// Long Polling: Connesione persistente con il server
// Classe Bot che implementa l'Interfaccia LongPollingSingleThreadUpdateConsumer
// Il bot mantiene una connessione apera con i server Telegram
public class Bot implements LongPollingSingleThreadUpdateConsumer {
    // Creazione del client Telegram, serve per inviare messaggi a Telegram
    private final TelegramClient telegramClient;

    // DBs
    private final UserDB userDB;
    private final SeriesDB seriesDB;
    private final EpisodesDB episodesDB;
    private final ActorsDB actorsDB;
    private final SearchDB searchDB;
    private final FavouriteDB favouriteDB;

    private final API api;
    private static final String bot_name = Config.get("telegram.bot.username");
    public static final String bot_url = "https://t.me/" + bot_name;

    // Episode
    private final Map<Long, Integer> episodeSerieCache = new HashMap<>();

    // Guest Cast
    private final Map<Long, Integer> guestSerieChache = new HashMap<>();

    // private static final int TELEGRAM_LIMIT = 4000;
    private static final int MAX_CAST_ITEMS = 15;
    private static final int MAX_FAVOURITES = 15;

    // Costruttore
    public Bot() {
        this.telegramClient = new OkHttpTelegramClient(Config.get("telegram.bot.token"));
        api = new API();

        // DBs
        this.userDB = new UserDB();
        this.seriesDB = new SeriesDB();
        this.episodesDB = new EpisodesDB();
        this.actorsDB = new ActorsDB();
        this.searchDB = new SearchDB();
        this.favouriteDB = new FavouriteDB();
    }

    // Metodo per avviare il bot
    public static void startBot() {
        try {
            // Crea un istanza della classe TelegramBotsLongPollingApplication responsabile di gestire il bot in long polling
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            // Registra il bot su Telegram usando il token
            botsApplication.registerBot(Config.get("telegram.bot.token"), new Bot());
        } catch (TelegramApiException e) {
            throw new RuntimeException("Errore durante l'avvio del bot", e);
        }
    }

    // Metodo dell'Interfaccia LongPollingSingleThreadUpdateConsumer
    // Metodo chiamato ogni volta che arriva un messaggio al bot, messaggio inviato dal client
    // Telegram manda aggiornamenti(Update) uno alla volta a questo metodo
    @Override
    public void consume(Update update) {
        // Messaggio normale
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleCommand(update.getMessage().getText(), update, update.getMessage().getChatId());
            // updateLastEntry(update.getMessage().getChatId());
        }
        // Callback dei bottoni
        else if (update.hasCallbackQuery()) {
            handleCallback(update);
        }
    }

    private void setInitialCommands(long chat_id) {
        List<BotCommand> commands = List.of(
                new BotCommand("start", "Avvia il bot")
        );

        this.setCommands(chat_id, commands);
    }

    private void setMainCommands(long chat_id) {
        List<BotCommand> commands = List.of(
                new BotCommand("help", "Mostra aiuto"),
                new BotCommand("actor", "Mostra informazioni su un attore"),
                new BotCommand("cast", "Mostra il cast/(guest cast) di una serie TV(o di un episodio)"),
                new BotCommand("episode", "Mostra i dettagli di un episodio"),
                new BotCommand("favourites", "Mostra serie/attori/episodi che hai aggiunto ai preferiti"),
                new BotCommand("serie", "Mostra i dettagli di una serie TV")
        );

        this.setCommands(chat_id, commands);
    }

    private void setCommands(long chat_id, List<BotCommand> commands) {
        SetMyCommands setCommands = new SetMyCommands(
                commands,
                new BotCommandScopeChat(String.valueOf(chat_id)),
                null
        );

        try {
            telegramClient.execute(setCommands);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public boolean userExists(long chat_id) {
        return this.userDB.userExists(chat_id);
    }

    private void addOrUpdateUser(Update update) {
        long chat_id = update.getMessage().getChatId();
        // Chi ha scritto
        var from = update.getMessage().getFrom();

        models.user.User user = new models.user.User(
                chat_id,
                from.getFirstName(),
                from.getLastName(),
                from.getUserName(),
                from.getLanguageCode()
        );

        this.userDB.addOrUpdateUser(user);
    }

    private void resetUserState(long chat_id) {
        this.userDB.updateUserState(chat_id, UserState.NONE);
    }

    private void changeUserState(long chat_id, UserState state) {
        this.userDB.updateUserState(chat_id, state);
    }

    private void updateLastEntry(long chat_id){
        this.userDB.updateLastEntry(chat_id);
    }

    // Quando un utente clicca un bottone inline Telegram invia una CallbackQuery
    private void handleCallback(Update update) {
        // Quale bottone l'utente ha cliccato, il valore
        String callbackData = update.getCallbackQuery().getData();
        long chat_id = update.getCallbackQuery().getMessage().getChatId();
        // ID della callback
        String callback_id = update.getCallbackQuery().getId();

        // Telegram, dopo che il bottone é stato premuto, si aspetta una risposta dal Bot
        // E se il Bot non risponde Telegram tine il bottone bloccato per un certo timeout
        AnswerCallbackQuery answer = AnswerCallbackQuery
                .builder()
                .callbackQueryId(callback_id)
                .text("")
                .showAlert(false)               // false: sbloccare il bottone
                .build();
        try {
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        this.handlePrefix(chat_id, callbackData, update.getCallbackQuery().getMessage().getMessageId());
    }

    private void handlePrefix(long chat_id, String callbackData, Integer message_id) {
        String[] parts = callbackData.split(":");
        if (parts.length != 2) return;

        Prefix prefix;
        try {
            prefix = Prefix.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            return;
        }

        int id;

        try {
            id = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return;
        }

        switch (prefix) {
            // SERIE
            case SERIE:
                this.handleSingleSerie(chat_id, id);
                break;

            // CAST
            case CAST_TYPE:
                switch (id){
                    case 1:
                        sendText(chat_id, """
                        Di quale _Serie TV_ ti interessa il _Main Cast_?
                        Scrivi il nome della _Serie TV_, *preferibilmente in inglese*.
                        """);
                        this.changeUserState(chat_id, UserState.WAITING_MAIN_CAST_SERIE_NAME);
                        break;
                    case 2:
                        sendText(chat_id, """
                        Di quale _Serie TV_ ti interessa l' _Episode Guest Cast_?
                        Scrivi il nome della _Serie TV_, *preferibilmente in inglese*.
                        """);
                        this.changeUserState(chat_id, UserState.WAITING_EPISODE_CAST_SERIE_NAME);
                        break;
                }
                break;

            case MAIN_CAST:
                this.handleMainCast(chat_id, id);
                break;

            case EPISODE_CAST:
                // Salva serie_id
                guestSerieChache.put(chat_id, id);
                sendText(chat_id, """
                    Scrivi stagione ed episodio nel formato: 
                    *S*<numero> *E*<numero>
                    _Esempio: S1 E1_
                    """);
                changeUserState(chat_id, UserState.WAITING_EPISODE_CAST_NUMBER);
                break;

            // ACTOR
            case ACTOR:
                this.handleSingleActor(chat_id, id);
                break;

            // EPISODE
            case EPISODE:
                // Salva serie_id
                episodeSerieCache.put(chat_id, id);
                sendText(chat_id, """
                        Scrivi stagione ed episodio nel formato: 
                        *S*<numero> *E*<numero>
                        _Esempio: S1 E1_
                        """);
                changeUserState(chat_id, UserState.WAITING_EPISODE_NUMBER);
                break;

            case CAST_CREDITS:
                handleCastCredits(chat_id, id);
                break;
            case GUEST_CREDITS:
                handleGuestCredits(chat_id, id);
                break;
            case FAVOURITE_ACTOR:
                if(favouriteDB.favouriteExists(chat_id, ACTOR, id))
                    deleteFromFavouritesDB(chat_id, ACTOR, id, message_id);
                else
                    addToFavouritesDB(chat_id, Entity.ACTOR, id, message_id);
                break;
            case FAVOURITE_EPISODE:
                if(favouriteDB.favouriteExists(chat_id, EPISODE, id))
                    deleteFromFavouritesDB(chat_id, EPISODE, id, message_id);
                else
                    addToFavouritesDB(chat_id, Entity.EPISODE, id, message_id);
                break;
            case FAVOURITE_SERIE:
                if(favouriteDB.favouriteExists(chat_id, SERIE, id))
                    deleteFromFavouritesDB(chat_id, SERIE, id, message_id);
                else
                    addToFavouritesDB(chat_id, Entity.SERIE, id, message_id);
                break;
        }
    }

    private void handleCommand(String text, Update update, long chat_id) {
        boolean exists = userExists(chat_id);
        if(!exists){
            setInitialCommands(chat_id);
            if (!text.startsWith("/")) {
                this.sendText(chat_id, "Usa */start* per avviare il bot");
                return;
            }

            if (text.startsWith("/start")) handleStart(update);
        }
        else {
            if (!text.startsWith("/")) {
                this.handleTextMessage(text, chat_id);
                return;
            }

            // Es. /serie
            switch(text.split(" ", 2)[0]) {
                case "/start":
                    handleStart(update);
                    break;
                case "/help":
                    this.handleHelp(chat_id);
                    break;
                case "/serie":
                    sendText(chat_id, """
                    Quale _Serie TV_ ti interessa?
                    Scrivi il nome della _Serie TV_ da cercare, *preferibilmente in inglese*.
                    """);
                    this.changeUserState(chat_id, UserState.WAITING_SERIE_NAME);
                    break;
                case "/cast":
                    // Invia bottoni, l'utente deve scegliere se vuole cast o episode cast
                    List<String> options = new ArrayList<>();

                    options.add("Main Cast");
                    options.add("Episode Guest Cast");

                    List<Integer> ids = new ArrayList<>();
                    ids.add(1);
                    ids.add(2);

                    List<Prefix> prefixes = new ArrayList<>();
                    prefixes.add(Prefix.CAST_TYPE);
                    prefixes.add(Prefix.CAST_TYPE);

                    this.sendButtons(chat_id, "Quale cast ti interessa?", options, ids, prefixes);
                    break;
                case "/actor":
                        sendText(chat_id, "Di quale attore vuoi vedere i dettagli?");
                        changeUserState(chat_id, UserState.WAITING_ACTOR_NAME);
                    break;
                case "/episode":
                    sendText(chat_id, """
                    Di quale _Serie TV_ ti interessa il _Cast_?
                    Scrivi il nome della _Serie TV_ da cercare, *preferibilmente in inglese*.
                    """);
                    changeUserState(chat_id, UserState.WAITING_EPISODE_SERIE_NAME);
                    break;
                case "/favourites":
                    handleFavourites(chat_id);
                    break;
                case "/searchStats":
                    // handleStats(chat_id);
                    break;
                default:
                    this.resetUserState(chat_id);
                    this.sendText(chat_id, "Comando non riconosciuto. Usa */help*");
                    break;
            }
        }
    }

    private void handleTextMessage(String text, long chat_id){
        models.user.User user = userDB.getUser(chat_id);
        if(user != null){
            switch (user.getState()) {
                case NONE:
                    sendText(chat_id, "Usa /help per vedere i comandi");
                    break;

                // SERIE
                case WAITING_SERIE_NAME:
                    handleSerie(chat_id, text, "Serie trovate per: " + text, Prefix.SERIE);
                    // resetUserState(chat_id);
                    break;

                case WAITING_MAIN_CAST_SERIE_NAME:
                    handleSerie(chat_id, text, "Serie trovate per: " + text, Prefix.MAIN_CAST);
                    // resetUserState(chat_id);
                    break;

                case WAITING_EPISODE_CAST_SERIE_NAME:
                    handleSerie(chat_id, text, "Serie trovate per: " + text, Prefix.EPISODE_CAST);
                    break;

                // ACTOR
                case WAITING_ACTOR_NAME:
                    handleActor(chat_id, text);
                    // resetUserState(chat_id);
                    break;

                // EPISODE
                case WAITING_EPISODE_SERIE_NAME:
                    // Cerco la serie come in handleSerie
                    handleSerie(chat_id, text, "Serie trovate per: " + text, Prefix.EPISODE);
                    // resetUserState(chat_id);
                    break;

                case WAITING_EPISODE_NUMBER:
                    handleEpisode(chat_id, text);
                    // resetUserState(chat_id);
                    break;

                case WAITING_EPISODE_CAST_NUMBER:
                    handleEpisodeCast(chat_id, text);
                    break;
            }
        }
    }

    private void handleStart(Update update) {
        long chat_id = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        // Registra utente se non esiste
        if (!userExists(chat_id)) {
            this.addOrUpdateUser(update);
            setMainCommands(chat_id);
            sendText(chat_id, "Benvenuto! Usa /help per cominciare");
        }

        // Gestione link
        if (text.startsWith("/start ")) {
            String textWithoutStart = text.substring(7);

            if (textWithoutStart.startsWith("serie_")) {
                handleSingleSerie(chat_id, Integer.parseInt(textWithoutStart.replace("serie_", "")));
                return;
            }
            // Main Cast
            else if (textWithoutStart.startsWith("cast_")) {
                handleMainCast(chat_id, Integer.parseInt(textWithoutStart.replace("cast_", "")));
                return;
            }
            else if (textWithoutStart.startsWith("guestCast_")) {
                handleEpisodeCastByID(chat_id, Integer.parseInt(textWithoutStart.replace("guestCast_", "")));
                return;
            }
            else if (textWithoutStart.startsWith("actor_")) {
                handleSingleActor(chat_id, Integer.parseInt(textWithoutStart.replace("actor_", "")));
                return;
            }
            else if (textWithoutStart.startsWith("castCredits_")) {
                handleCastCredits(chat_id, Integer.parseInt(textWithoutStart.replace("castCredits_", "")));
                return;
            }
            else if (textWithoutStart.startsWith("guestCredits_")) {
                handleGuestCredits(chat_id, Integer.parseInt(textWithoutStart.replace("guestCredits_", "")));
                return;
            }
            else if (textWithoutStart.startsWith("episode_")) {
                handleEpisodeWithID(chat_id, Integer.parseInt(textWithoutStart.replace("episode_", "")));
                return;
            }
            else if (textWithoutStart.startsWith("character_")) {
                handleCharacter(chat_id, Integer.parseInt(textWithoutStart.replace("character_", "")));
                return;
            }
            else {
                sendText(chat_id, "Comando non riconosciuto. Usa */help*");
            }
        }

        // Reset stato utente
        resetUserState(chat_id);
    }

    private void handleHelp(long chat_id) {
        this.resetUserState(chat_id);

        String text = """
        Comandi disponibili:
        \t_-_ */help* - _Mostra questa guida_
        \t_-_ */actor* - _Mostra informazioni su un attore_
        \t_-_ */cast* - _Mostra il cast(guest cast) di una serie TV (o di un episodio)_
        \t_-_ */episode* - _Mostra i dettagli di un episodio_
        \t_-_ */favourites* - _Mostra serie/attori/episodi che hai aggiunto ai preferiti_
        \t_-_ */serie* - _Mostra i dettagli di una serie TV_
        """;

        this.sendText(chat_id, text);
    }

    // Utilizza un API per più ricerche
    private void handleSerie(long chat_id, String serie_name, String buttonText, Prefix prefix) {
        // API
        SearchResult[] series = api.getSeries(serie_name);

        if (series == null || series.length == 0) {
            sendText(chat_id, "*Nessuna* _Serie TV_ trovata per: " + "_" + serie_name + "_" + "\n*Riprova*");
            return;
        }

        // Se ci sono più opzioni
        if(series.length > 1) {
            List<String> options = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            List<Prefix> prefixs = new ArrayList<>();

            for (SearchResult s : series) {
                if (s.getShow() != null && s.getShow().getName() != null){
                    options.add(s.getShow().getName());
                    ids.add(s.getShow().getId());
                    prefixs.add(prefix);
                }
                else
                    System.out.println("Questa serie " + serie_name + " non ha show o name!");
            }
            sendButtons(chat_id, buttonText, options, ids, prefixs);
        }
        else if(series.length == 1) {
            int serie_id = series[0].getShow().getId();
            switch(prefix){
                case SERIE:
                    handleSingleSerie(chat_id, serie_id);
                    break;
                case MAIN_CAST:
                    handleMainCast(chat_id, serie_id);
                    break;
                case EPISODE_CAST:
                    // Salva serie_id
                    guestSerieChache.put(chat_id, serie_id);
                    sendText(chat_id, """
                    Scrivi stagione ed episodio nel formato: 
                    *S*<numero> *E*<numero>
                    _Esempio: S1 E1_
                    """);
                    changeUserState(chat_id, UserState.WAITING_EPISODE_CAST_NUMBER);
                    break;
                case EPISODE:
                    // Salva serie_id
                    episodeSerieCache.put(chat_id, serie_id);
                    sendText(chat_id, """
                    Scrivi stagione ed episodio nel formato: 
                    *S*<numero> *E*<numero>
                    _Esempio: S1 E1_
                    """);
                    changeUserState(chat_id, UserState.WAITING_EPISODE_NUMBER);
                    break;
            }
        }
        else {
            sendText(chat_id, "*Nessuna* _Serie TV_ trovata per: " + "_" + serie_name + "_" + "\n*Riprova*");
        }
    }

    private void handleSingleSerie(long chat_id, int id) {
        Serie serie = api.getSerieById(id);

        if (serie == null) {
            sendText(chat_id, "Errore nel recupero della serie.");
            return;
        }

        // Salvataggio DB
        seriesDB.addOrUpdateSerie(serie);
        searchDB.addSearch(chat_id, SERIE, serie.getId(),  serie.getName());

        if (serie.getImage() != null && serie.getImage().getOriginal() != null) {
            sendPhoto(chat_id, serie.getImage().getOriginal());
        }

        sendText(chat_id, serie.toString());

        String cast_id = String.valueOf(serie.getId());
        String link = Bot.bot_url + "?start=cast_" + cast_id;

        sendLink(chat_id, "Main Cast",  link);
        // Chiedo all'utente se vuole salvare la sua preferenza
        addOrRemoveFavourite(chat_id, SERIE, serie.getId(), serie.getName());
    }

    private void handleCast(long chat_id, Cast[] casts, String text) {
        if (casts == null || casts.length == 0) {
            sendText(chat_id, "Nessun cast trovato per questa serie.");
            return;
        }

        if(casts.length < 1) {
            sendText(chat_id, "Nessun cast trovato per questa serie.");
            return;
        }

        int limit = Math.min(casts.length, MAX_CAST_ITEMS);

        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < limit; i++) {
            sb.append(casts[i].toString());
        }

        if (casts.length > MAX_CAST_ITEMS) {
            sb.append("\n(+ altri ")
                    .append(casts.length - MAX_CAST_ITEMS)
                    .append(" membri del cast)");
        }

        sendText(chat_id, (sb.toString()), "Markdown");
    }

    private void handleMainCast(long chat_id, int id) {
        Cast[] casts = api.getMainCastBySerieId(id);
        handleCast(chat_id, casts, "Main Cast\n");
    }

    private void handleEpisodeCast(long chat_id, String text) {
        Integer serie_id = guestSerieChache.get(chat_id);

        if (serie_id == null) {
            sendText(chat_id, "Nessuna serie selezionata. Usa /cast.");
            return;
        }

        try {
            text = text.toUpperCase().replace(",", " ");
            String[] parts = text.split(" ");

            if (parts.length < 2) {
                sendText(chat_id, "Formato non valido. Usa: S1 E1");
                return;
            }

            int season = Integer.parseInt(parts[0].replace("S", ""));
            int number = Integer.parseInt(parts[1].replace("E", ""));

            Episode episode = api.getEpisodeByNumber(serie_id, season, number);

            if(episode != null) {
                Cast[] casts = api.getEpisodeCastByEpisodeId(episode.getId());
                handleCast(chat_id, casts, "Episode Guest Cast\n");
                // episodeSerieCache.remove(chat_id);

                return;
            }

            sendText(chat_id, "Episodio non trovato, riprova");

        } catch (NumberFormatException e) {
            sendText(chat_id, "Stagione ed episodio devono essere numeri.");
        }
    }

    private void handleEpisodeCastByID(long chat_id, int id){
        Cast[] casts = api.getEpisodeCastByEpisodeId(id);
        handleCast(chat_id, casts, "Episode Guest Cast\n");
    }

    private void handleActor(long chat_id, String actor_name) {
        SearchActorResult[] actors = api.getActors(actor_name);

        if ((actors == null || actors.length == 0) && actor_name.contains(" ")) {
            // Prova per nome
            actors = api.getActors(actor_name.split(" ")[0]);
        }

        if (actors.length > 1) {
            List<String> options = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            List<Prefix> prefixs = new ArrayList<>();

            for (SearchActorResult a : actors) {
                options.add(a.getPerson().getName());
                ids.add(a.getPerson().getId());
                prefixs.add(Prefix.ACTOR);
            }
            sendButtons(chat_id, "Seleziona l'attore:", options, ids, prefixs);
        }
        else if (actors.length == 1) {
            handleSingleActor(chat_id, actors[0].getPerson().getId());
        }
        else{
            sendText(chat_id, "*Nessun attore trovato per:_" + (actor_name) + "_" + "\nRiprova*");
        }
    }

    private void handleSingleActor(long chat_id, int id) {
        Actor actor = api.getActorById(id);

        // Salvataggio DB
        actorsDB.addOrUpdateActor(actor);
        searchDB.addSearch(chat_id, ACTOR, actor.getId(), actor.getName());

        if (actor.getImage() != null && actor.getImage().getOriginal() != null)
            sendPhoto(chat_id, actor.getImage().getOriginal());

        sendText(chat_id, actor.toString());

        List<String> options = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        List<Prefix> prefixes = new ArrayList<>();

        options.add("Cast Credits");
        ids.add(actor.getId());
        prefixes.add(Prefix.CAST_CREDITS);

        options.add("Guest Credits");
        ids.add(actor.getId());
        prefixes.add(Prefix.GUEST_CREDITS);

        addOrRemoveFavourite(chat_id, ACTOR, actor.getId(), actor.getName());
        sendButtons(chat_id, "Partecipazioni:", options, ids, prefixes);
    }

    private void handleEpisode(long chat_id, String text) {
        Integer serie_id = episodeSerieCache.get(chat_id);

        if (serie_id == null) {
            sendText(chat_id, "Nessuna serie selezionata. Usa /episode.");
            return;
        }

        try {
            text = text.toUpperCase().replace(",", " ");
            String[] parts = text.split(" ");

            if (parts.length < 2) {
                sendText(chat_id, "Formato non valido. Usa: S1 E1");
                return;
            }

            int season = Integer.parseInt(parts[0].replace("S", ""));
            int number = Integer.parseInt(parts[1].replace("E", ""));

            Episode episode = api.getEpisodeByNumber(serie_id, season, number);

            if(episode != null) {
                // Salvataggio DB
                episodesDB.addOrUpdateEpisode(episode);
                searchDB.addSearch(chat_id, EPISODE, episode.getId(), episode.getName());

                if (episode.getImage() != null && episode.getImage().getOriginal() != null)
                    sendPhoto(chat_id, episode.getImage().getOriginal());
                sendText(chat_id, episode.toString());
                // changeUserState(chat_id, UserState.WAITING_EPISODE_NUMBER);
                // episodeSerieCache.remove(chat_id);
                sendLink(chat_id, "Guest Cast", Bot.bot_url + "?start=guestCast_" + episode.getId());
                addOrRemoveFavourite(chat_id, EPISODE, episode.getId(), episode.getName());
                return;
            }

            sendText(chat_id, "Episodio non trovato, riprova");

        } catch (NumberFormatException e) {
            sendText(chat_id, "Stagione ed episodio devono essere numeri.");
        }
    }

    private void handleEpisodeWithID(long chat_id, int id){
        Episode episode = api.getEpisodeById(id);

        if(episode != null) {
            // Salvataggio DB
            episodesDB.addOrUpdateEpisode(episode);
            searchDB.addSearch(chat_id, EPISODE, episode.getId(),  episode.getName());

            if (episode.getImage() != null && episode.getImage().getOriginal() != null)
                sendPhoto(chat_id, episode.getImage().getOriginal());
            sendText(chat_id, episode.toString());
            sendLink(chat_id, "Guest Cast", Bot.bot_url + "?start=guestCast_" + episode.getId());
            addOrRemoveFavourite(chat_id, EPISODE, episode.getId(), episode.getName());
        }
    }

    private void handleCastCredits(long chat_id, int id){
        CastCredits[] casts = api.getActorCastCredits(id);

        int limit = Math.min(casts.length, MAX_CAST_ITEMS);

        StringBuilder sb = new StringBuilder("");
        if(casts.length > 0){
            sb .append("È nel cast principale in:\n");
            for (int i = 0; i < limit; i++) {
                sb.append(casts[i].toString());
            }

            if (casts.length > MAX_CAST_ITEMS) {
                sb.append("\n(+ altri ")
                        .append(casts.length - MAX_CAST_ITEMS)
                        .append(" ruoli)");
            }

            sendText(chat_id, (sb.toString()), "Markdown");
        }
        else {
            sendText(chat_id, "Non é nel cast principale di *nessuna* _serie TV_", "Markdown");
        }
    }

    private void handleGuestCredits(long chat_id, int id){
        GuestCredits[] casts = api.getActorGuestCredits(id);

        int limit = Math.min(casts.length, MAX_CAST_ITEMS);

        StringBuilder sb = new StringBuilder("");
        if(casts.length > 0){
            sb .append("Ha partecipato come guest in:\n");
            for (int i = 0; i < limit; i++) {
                sb.append(casts[i].toString());
            }

            if (casts.length > MAX_CAST_ITEMS) {
                sb.append("\n(+ altri ")
                        .append(casts.length - MAX_CAST_ITEMS)
                        .append(" ruoli)");
            }

            sendText(chat_id, (sb.toString()), "Markdown");
        }
        else {
            sendText(chat_id, "Non compare come guest in *nessuna* _serie TV_", "Markdown");
        }
    }

    private void handleCharacter(long chat_id, int id) {
        Character character = api.getCharacterById(id);
        if(character != null) {
            if(character.getImage() != null && character.getImage().getOriginal() != null)
                sendPhoto(chat_id, character.getImage().getOriginal());
            sendText(chat_id, character.toString());
        }
    }

    private void changeFavouriteButton(long chat_id, Entity entityType, int entityId, Integer message_id){
        List<String> options = new ArrayList<>();
        List<Prefix> prefixs = new ArrayList<>();

        switch(entityType) {
            case ACTOR:
                prefixs.add(Prefix.FAVOURITE_ACTOR);
                break;
            case EPISODE:
                prefixs.add(Prefix.FAVOURITE_EPISODE);
                break;
            case SERIE:
                prefixs.add(Prefix.FAVOURITE_SERIE);
                break;
        }

        if(favouriteDB.favouriteExists(chat_id, entityType, entityId)) {
            options.add("➖ Rimuovi dai preferiti ⭐");
        }
        else {
            options.add("➕ Aggiungi ai preferiti ⭐");
        }

        updateButtons(chat_id, "Opzioni disponibili", options, List.of(entityId), prefixs, message_id);
    }

    private void addToFavouritesDB(long chat_id, Entity entityType, int entityId, Integer message_id) {
        favouriteDB.addFavourite(chat_id, entityType, entityId);
        changeFavouriteButton(chat_id, entityType, entityId, message_id);
        sendText(chat_id, entityType.name() + " aggiunto ai preferiti ⭐");
    }

    private void deleteFromFavouritesDB(long chat_id, Entity entityType, int entityId, Integer message_id) {
        favouriteDB.removeFavourite(chat_id, entityType, entityId);
        changeFavouriteButton(chat_id, entityType, entityId, message_id);
        sendText(chat_id, entityType.name() + " rimosso dai preferiti ⭐");
    }

    // Per la stampa del bottone
    private void addOrRemoveFavourite(long chat_id, Entity entityType, int entityId, String entityName) {
        List<String> options = new ArrayList<>();
        List<Prefix> prefixs = new ArrayList<>();

        switch(entityType) {
            case ACTOR:
                prefixs.add(Prefix.FAVOURITE_ACTOR);
                break;
            case EPISODE:
                prefixs.add(Prefix.FAVOURITE_EPISODE);
                break;
            case SERIE:
                prefixs.add(Prefix.FAVOURITE_SERIE);
                break;
        }

        // Controlla se é nei preferiti, sul database
        if(favouriteDB.favouriteExists(chat_id, entityType, entityId)) {
            // Stampa il bottone per RIMUOVERLO dai preferiti
            options.add("➖ Rimuovi ai preferiti ⭐");
            sendButtons(chat_id,"Opzioni disponibili", options, List.of(entityId), prefixs);
        }
        // Altrimenti quello per AGGIUNGERLO ai preferiti
        else {
            options.add("➕ Aggiungi ai preferiti ⭐");
            sendButtons(chat_id, "Opzioni disponibili", options, List.of(entityId), prefixs);
        }
    }

    private void handleFavourites(long chatId) {
        List<String> series = favouriteDB.getFavouritesByType(chatId, SERIE);
        List<String> episodes = favouriteDB.getFavouritesByType(chatId, EPISODE);
        List<String> actors = favouriteDB.getFavouritesByType(chatId, ACTOR);

        StringBuilder sb = new StringBuilder();

        if (!series.isEmpty()) {
            sb.append("Serie preferite:\n");
            int limit = Math.min(series.size(), MAX_FAVOURITES);
            for (int i = 0; i < limit; i++) {
                String[] parts = series.get(i).split("-", 2);
                String id = parts[0];
                String name = parts[1];
                sb.append("- [").append(name).append("](").append(Bot.bot_url).append("?start=serie_").append(id).append(")\n");
            }
            if (series.size() > MAX_FAVOURITES) {
                sb.append("(+ altri ").append(series.size() - MAX_FAVOURITES).append(" serie)\n");
            }
            sb.append("\n");
        }

        if (!episodes.isEmpty()) {
            sb.append("Episodi preferiti:\n");
            int limit = Math.min(episodes.size(), MAX_FAVOURITES);
            for (int i = 0; i < limit; i++) {
                String[] parts = episodes.get(i).split("-", 2);
                String id = parts[0];
                String name = parts[1];
                sb.append("- [").append(name).append("](").append(Bot.bot_url).append("?start=episode_").append(id).append(")\n");
            }
            if (episodes.size() > MAX_FAVOURITES) {
                sb.append("(+ altri ").append(episodes.size() - MAX_FAVOURITES).append(" episodi)\n");
            }
            sb.append("\n");
        }

        if (!actors.isEmpty()) {
            sb.append("Attori preferiti:\n");
            int limit = Math.min(actors.size(), MAX_FAVOURITES);
            for (int i = 0; i < limit; i++) {
                String[] parts = actors.get(i).split("-", 2);
                String id = parts[0];
                String name = parts[1];
                sb.append("- [").append(name).append("](").append(Bot.bot_url).append("?start=actor_").append(id).append(")\n");
            }
            if (actors.size() > MAX_FAVOURITES) {
                sb.append("(+ altri ").append(actors.size() - MAX_FAVOURITES).append(" attori)\n");
            }
        }

        if(sb.isEmpty())
            sendText(chatId, "Non hai *niente* di salvato nei _preferiti_ ⭐", "Markdown");
        else
            sendText(chatId, sb.toString(), "Markdown");
    }

    private void sendOneText(long chat_id, String text, String parseMode) {
        // Creazione del messaggio da inviare
        SendMessage message_to_send = SendMessage
                .builder()
                .chatId(chat_id)
                .parseMode(parseMode)
                .text(text)
                .build();   // Costruisce l'oggetto

        // Il bot prova a inviare il messaggio
        try {
            telegramClient.execute(message_to_send);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendText(long chat_id, String text, String parseMode){
        sendOneText(chat_id, text, parseMode);
    }

    private void sendText(long chat_id, String text) {
        sendText(chat_id, text, "Markdown");
    }

    private void sendLink(long chat_id, String text, String link) {
        sendText(chat_id, "[" + text + "](" + link + ")" , "Markdown");
    }

    private void sendButtons(long chat_id, String text, List<String> options, List<Integer> ids, List<Prefix> prefixs) {
        // Ogni bottone su una riga
        // InlineKeyboardRow, riga di bottoni
        List<InlineKeyboardRow> rows = new ArrayList<>();
        // text = EscapeUtils.escapeMarkdown(text);

        for (int i = 0; i < options.size(); i++) {
            // Crea l'oggetto InlineKeyboardButton
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(options.get(i))               // Testo del bottone
                    .callbackData(prefixs.get(i).name() + ":" + ids.get(i))     // Dato inviato a Telegram, al Bot
                    .build();

            // Crea una riga, per ogni bottone
            InlineKeyboardRow row = new InlineKeyboardRow();
            // E ci aggiunge il bottone
            row.add(button);
            // Aggiunge la riga alle righe
            rows.add(row);
        }

        // Markup, una struttura di bottoni
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(rows)     // Lista di righe di bottoni
                .build();

        // Messaggio da inviare, con i bottoni
        SendMessage message = SendMessage
                .builder()
                .chatId(chat_id)
                .text((text))
                .parseMode("Markdown")
                .replyMarkup(markup)    // Aggiunge i bottoni sotto il messaggio
                .build();

        try {
            telegramClient.execute(message);
            // Se qualcosa va storto viene lanciata un eccezione di tipo TelegramApiException
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void updateButtons(long chat_id, String text, List<String> options, List<Integer> ids, List<Prefix> prefixs,int message_id) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (int i = 0; i < options.size(); i++) {
            // Crea il bottone aggiornato
            InlineKeyboardButton button = InlineKeyboardButton
                    .builder()
                    .text(options.get(i))
                    .callbackData(prefixs.get(i).name() + ":" + ids.get(i))
                    .build();

            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(button);
            rows.add(row);
        }

        InlineKeyboardMarkup markup = InlineKeyboardMarkup
                .builder()
                .keyboard(rows)
                .build();

        // Modifica il messaggio esistente
        EditMessageText editMessage = EditMessageText
                .builder()
                .chatId(chat_id)
                .messageId(message_id)
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(markup)
                .build();

        try {
            telegramClient.execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendPhotoAndCaption(long chat_id, String path, String caption) {
        // SendPhoto classe Telegram per mandare foto
        // InputFile accetta sia un URL e sia un file Locale
        SendPhoto sendPhotoRequest = new SendPhoto(String.valueOf(chat_id), new InputFile(path));
        // Didascalia: massimo 1024 caratteri
        if (caption != null && !caption.isEmpty()) {
            sendPhotoRequest.setCaption(caption);
            sendPhotoRequest.setParseMode("Markdown");
        }

        try {
            telegramClient.execute(sendPhotoRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendPhoto(long chat_id, String path) {
        sendPhotoAndCaption(chat_id, path, "");
    }
}