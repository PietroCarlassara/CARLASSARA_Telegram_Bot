package models.enumerable;

// Un ELENCO di costanti, la variabile assume un insieme limitato di valori
public enum UserState {
    NONE,                           // Nessuna azione in corso
    // SERIE
    WAITING_SERIE_NAME,                 // L'utente deve scrivere il nome della serie
    WAITING_MAIN_CAST_SERIE_NAME,       // L'utente deve scrivere il nome della serie
    WAITING_EPISODE_CAST_SERIE_NAME,    // L'utente deve scrivere il nome della serie
    // ACOTOR
    WAITING_ACTOR_NAME,             // L'utente deve scrivere il nome dell'attore
    // EPISODE
    WAITING_EPISODE_SERIE_NAME,     // Nome serie
    WAITING_EPISODE_NUMBER,         // Stagione + Episodio
    WAITING_EPISODE_CAST_NUMBER     // Stagione + Episodio
}