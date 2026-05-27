package com.udg.betmasterai.data.remote;

import com.udg.betmasterai.data.model.OddsApiMatch;
import com.udg.betmasterai.data.model.OddsApiScore;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SportsApi {

    /**
     * Obtiene las cuotas actuales de los partidos de una liga deportiva.
     *
     * Endpoint: GET /v4/sports/{sport}/odds
     *
     * @param sport      Clave de la liga (ej: "soccer_spain_la_liga"). Ver ApiConstants.
     * @param apiKey     API Key de The Odds API (https://the-odds-api.com).
     * @param regions    Región de bookmakers: "eu" para casas europeas.
     * @param markets    Tipo de mercado: "h2h" = 1X2 (Local, Empate, Visitante).
     * @param oddsFormat Formato de cuotas: "decimal" = estándar europeo.
     * @return           Lista de partidos con sus cuotas por bookmaker.
     */
    @GET("v4/sports/{sport}/odds")
    Call<List<OddsApiMatch>> getOdds(
            @Path("sport")       String sport,
            @Query("apiKey")     String apiKey,
            @Query("regions")    String regions,
            @Query("markets")    String markets,
            @Query("oddsFormat") String oddsFormat
    );

    /**
     * Obtiene los resultados de los partidos (scores).
     */
    @GET("v4/sports/{sport}/scores")
    Call<List<OddsApiScore>> getScores(
            @Path("sport")       String sport,
            @Query("apiKey")     String apiKey,
            @Query("daysFrom")   int daysFrom
    );
}
