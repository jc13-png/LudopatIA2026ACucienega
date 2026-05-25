package com.udg.betmasterai.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Modelo de respuesta de The Odds API (endpoint /v4/sports/{sport}/odds).
 *
 * Cada objeto representa un partido con las cuotas de múltiples casas de apuestas.
 * La app promedia las cuotas entre todas las casas disponibles para obtener un
 * precio de mercado más justo y representativo.
 */
public class OddsApiMatch {

    @SerializedName("id")
    private String id;

    @SerializedName("sport_key")
    private String sportKey;

    @SerializedName("sport_title")
    private String sportTitle;

    /** Fecha y hora del partido en formato ISO 8601 (UTC). Ej: "2026-05-25T15:00:00Z" */
    @SerializedName("commence_time")
    private String commenceTime;

    @SerializedName("home_team")
    private String homeTeam;

    @SerializedName("away_team")
    private String awayTeam;

    /** Lista de casas de apuestas (bookmakers) que ofrecen cuotas para este partido. */
    @SerializedName("bookmakers")
    private List<Bookmaker> bookmakers;

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String getId()            { return id; }
    public String getSportKey()      { return sportKey; }
    public String getSportTitle()    { return sportTitle; }
    public String getCommenceTime()  { return commenceTime; }
    public String getHomeTeam()      { return homeTeam; }
    public String getAwayTeam()      { return awayTeam; }
    public List<Bookmaker> getBookmakers() { return bookmakers; }

    // ─── Clases anidadas (estructura JSON de la API) ───────────────────────────

    public static class Bookmaker {
        @SerializedName("key")
        private String key;

        @SerializedName("title")
        private String title;

        @SerializedName("last_update")
        private String lastUpdate;

        @SerializedName("markets")
        private List<Market> markets;

        public String getKey()           { return key; }
        public String getTitle()         { return title; }
        public String getLastUpdate()    { return lastUpdate; }
        public List<Market> getMarkets() { return markets; }
    }

    public static class Market {
        @SerializedName("key")
        private String key;

        @SerializedName("last_update")
        private String lastUpdate;

        @SerializedName("outcomes")
        private List<Outcome> outcomes;

        public String getKey()              { return key; }
        public String getLastUpdate()       { return lastUpdate; }
        public List<Outcome> getOutcomes()  { return outcomes; }
    }

    public static class Outcome {
        @SerializedName("name")
        private String name;

        /** Cuota decimal (e.g., 2.10 significa ganar $2.10 por cada $1 apostado). */
        @SerializedName("price")
        private double price;

        public String getName()  { return name; }
        public double getPrice() { return price; }
    }
}
