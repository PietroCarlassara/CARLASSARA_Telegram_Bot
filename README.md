# Telegram TV Series Bot

Un **Chat bot Telegram** per cercare Serie TV, Episodi, Attori e Personaggi, visualizzare informazioni dettagliate, cast principali e guest cast, e gestire i propri preferiti.

---

## Funzionalità

- **Ricerca Serie TV**
    - Cerca una serie per nome.
    - Se esistono più risultati, mostra opzioni con bottoni inline.
    - Visualizza dettagli della serie e link al cast principale.

- **Gestione Cast**
    - Mostra il cast principale di una serie.
    - Mostra il guest cast di un episodio specifico.
    - Permette di cercare attori e vedere le loro partecipazioni principali o come guest.

- **Gestione Episodi**
    - Visualizza informazioni dettagliate su un episodio tramite numero di stagione ed episodio.
    - Mostra il guest cast di un episodio.

- **Gestione Attori**
    - Cerca attori per nome.
    - Mostra dettagli, immagine e partecipazioni (cast principale e guest).

- **Gestione Personaggi**
    - Visualizza informazioni e immagine di personaggi specifici.

- **Preferiti**
    - Aggiungi e rimuovi serie, episodi o attori dai preferiti.
    - Mostra la lista dei preferiti.

- **Interfaccia Telegram**
    - Messaggi Markdown.
    - Bottoni inline per selezioni e azioni rapide.
    - Link telegram per serie, episodi o attori.

---

## Come Funziona

 **Comandi principali**:

   | Comando       | Descrizione                                  |
   |---------------|----------------------------------------------|
   | `/help`       | Mostra questa guida.                         |
   | `/serie`      | Cerca una serie TV.                          |
   | `/episode`    | Cerca un episodio tramite serie e numero.    |
   | `/cast`       | Cerca il cast di una serie o di un episodio. |
   | `/actor`      | Cerca un attore.                             |
   | `/favourites` | Visualizza i preferiti.                      |

1. **Bottoni Inline**:
    - Permettono di selezionare una delle opzioni trovate o di aggiungere/rimuovere dai preferiti.
    - I bottoni inviano dati al bot tramite callback.

2. **Gestione Stati**:
    - Il bot tiene traccia dello stato dell’utente per operazioni che richiedono input sequenziali:
        - `WAITING_EPISODE_NUMBER`
        - `WAITING_EPISODE_CAST_NUMBER`
        - ecc.

3. **Cache temporanea**:
    - Viene mantenuta la serie corrente per i comandi `/episode` e `/cast` tramite `episodeSerieCache` e `guestSerieCache`.

---

## Struttura del Progetto

- **API**  
  Interfaccia per recuperare dati di Serie TV, Episodi, Attori e Personaggi.

- **Database locale**
    - `seriesDB`, `episodesDB`, `actorsDB` - Salvataggio delle informazioni recuperate.
    - `favouriteDB` - Gestione dei preferiti degli utenti.
    - `searchDB` - Storico ricerche degli utenti.

- **Gestione messaggi Telegram**
    - `sendText`, `sendPhoto`, `sendLink`, `sendButtons`, `updateButtons`  
      Metodi per inviare messaggi, foto e bottoni inline tramite l’API di Telegram.

- **Gestione logica bot**
    - `handleSerie`, `handleSingleSerie`, `handleEpisode`, `handleCast`, `handleActor`, ecc.  
      Metodi che gestiscono la logica in base al tipo di entità e all’input dell’utente.

---

## Tecnologie Utilizzate

- Telegram Bots API (tramite libreria ufficiale Telegram)
- API esterna per Serie TV
- Database locale (in-memory o persistente)
- Markdown per formattare i messaggi

---

## Installazione

1. Clona il repository:

   ```bash
   git clone https://github.com/PietroCarlassara/CARLASSARA_Telegram_Bot.git
   cd CARLASSARA_Telegram_Bot

### Configurazione:
#### Configurazione config.properties:
1. Copia il file di configurazione d'esempio, esegui:
    ```bash 
   cp config.example.properties config.properties`
2. Completa i campi

#### Configurazione Database:
1. Aprire il file SetupDB ed eseguire il codice

## Schema del Database

### Tabella `actors`
| Campo     | Tipo    | Note                  |
|-----------|--------|----------------------|
| id        | INTEGER | Primary Key          |
| name      | TEXT   | Non nullo            |
| gender    | TEXT   |                      |
| birthday  | TEXT   |                      |
| deathday  | TEXT   |                      |

### Tabella `users`
| Campo       | Tipo       | Note                  |
|-------------|-----------|----------------------|
| chat_id     | LONG      | Primary Key          |
| first_name  | VARCHAR   | Non nullo            |
| last_name   | VARCHAR   |                      |
| username    | VARCHAR   |                      |
| last_entry  | DATETIME  |                      |
| language    | VARCHAR   |                      |
| state       | TEXT      | Stato utente         |

### Tabella `series`
| Campo   | Tipo    | Note                  |
|---------|--------|----------------------|
| id      | INTEGER | Primary Key          |
| name    | TEXT   | Non nullo            |
| rating  | DOUBLE |                      |

### Tabella `episodes`
| Campo    | Tipo    | Note                  |
|----------|--------|----------------------|
| id       | INTEGER | Primary Key          |
| serie_id | INTEGER | Riferimento a `series.id` |
| season   | INTEGER |                      |
| number   | INTEGER |                      |
| name     | TEXT    |                      |

### Tabella `searches`
| Campo       | Tipo     | Note                           |
|-------------|---------|--------------------------------|
| id          | INTEGER | Primary Key autoincrement      |
| chat_id     | INTEGER |                                |
| entity_type | TEXT    | Tipo di entità cercata        |
| entity_id   | INTEGER | ID dell'entità                |
| entity_name | TEXT    | Nome dell'entità              |
| searched_at | DATETIME| Timestamp della ricerca       |

### Tabella `user_favourites`
| Campo       | Tipo     | Note                              |
|-------------|---------|----------------------------------|
| id          | INTEGER | Primary Key autoincrement        |
| chat_id     | INTEGER | ID utente                         |
| entity_type | TEXT    | Tipo di entità (serie, episodio, attore) |
| entity_id   | INTEGER | ID dell'entità                    |
| entity_name | TEXT    | Nome dell'entità                  |
| add_at      | DATETIME| Timestamp di aggiunta             |

## Data Source & Licensing

This project uses the **TVmaze API** to retrieve information about TV shows, episodes and people.

Data provided by [TVmaze](https://www.tvmaze.com).

The TVmaze API is licensed under the **Creative Commons Attribution-ShareAlike 4.0 (CC BY-SA 4.0)** license.
You can find more information about the license here:
https://creativecommons.org/licenses/by-sa/4.0/

This license applies **only to the data provided by TVmaze**, not to the source code of this project.
