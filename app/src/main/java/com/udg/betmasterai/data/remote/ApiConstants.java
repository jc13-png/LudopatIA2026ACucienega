package com.udg.betmasterai.data.remote;

/**
 * Constantes globales para la conexión con The Odds API.
 *
 * ─── CONFIGURACIÓN ────────────────────────────────────────────────────────────
 * 1. Registrarse GRATIS en: https://the-odds-api.com  (500 requests/mes gratis)
 * 2. Copiar la API Key del dashboard y pegarla en API_KEY.
 * 3. Cambiar DEFAULT_SPORT a la liga deseada (ver lista abajo).
 * ──────────────────────────────────────────────────────────────────────────────
 */
public class ApiConstants {

    public static final String BASE_URL = "https://api.the-odds-api.com/";

    // ⚠️  REEMPLAZA ESTE VALOR CON TU API KEY DE https://the-odds-api.com
    public static final String API_KEY = "TU_API_KEY_AQUI";

    // Liga por defecto. Otras opciones:
    //   "soccer_epl"                  → Premier League
    //   "soccer_spain_la_liga"        → La Liga
    //   "soccer_germany_bundesliga"   → Bundesliga
    //   "soccer_italy_serie_a"        → Serie A
    //   "soccer_france_ligue_one"     → Ligue 1
    //   "soccer_uefa_champs_league"   → Champions League
    //   "soccer_mexico_ligamx"        → Liga MX
    public static final String DEFAULT_SPORT = "soccer_spain_la_liga";

    // Región de casas de apuestas (eu = europeas, las mejores cuotas)
    public static final String REGIONS = "eu";

    // Mercado head-to-head (1X2: Local, Empate, Visitante)
    public static final String MARKETS = "h2h";

    // Formato de cuotas decimales (estándar europeo)
    public static final String ODDS_FORMAT = "decimal";

    // Intervalo de refresco automático en segundos
    public static final int REFRESH_INTERVAL_SECONDS = 60;
}
