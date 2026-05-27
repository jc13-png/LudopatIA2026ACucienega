package com.udg.betmasterai.data.repository;

import com.udg.betmasterai.data.model.MatchData;
import com.udg.betmasterai.data.model.OddsApiMatch;
import com.udg.betmasterai.data.remote.ApiConstants;
import com.udg.betmasterai.data.remote.RetrofitClient;
import com.udg.betmasterai.data.remote.SportsApi;
import com.udg.betmasterai.domain.BetEngine;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio de partidos: capa que abstrae el origen de los datos (API vs mock).
 *
 * Flujo:
 *  1. Llama a The Odds API con Retrofit (asíncrono).
 *  2. Si la respuesta es exitosa → convierte y entrega los datos reales.
 *  3. Si falla (sin internet, clave inválida, liga sin partidos) → entrega datos mock
 *     y reporta el error para mostrarlo en la UI.
 *
 * La conversión de OddsApiMatch → MatchData promedia las cuotas de todos los
 * bookmakers disponibles para obtener el precio de mercado más representativo.
 */
public class MatchRepository {

    // ─── Interfaz de callbacks ────────────────────────────────────────────────

    public interface MatchesCallback {
        void onSuccess(List<MatchData> matches, String source);
        void onError(String errorMessage, List<MatchData> fallbackData);
    }

    // ─── Petición principal ───────────────────────────────────────────────────

    public void fetchMatches(String sport, MatchesCallback callback) {

        // Verificar si hay API key configurada antes de intentar la red
        if ("TU_API_KEY_AQUI".equals(ApiConstants.API_KEY)) {
            callback.onError(
                    "Sin API Key. Obtén una gratis en the-odds-api.com y configúrala en ApiConstants.java",
                    getMockMatches()
            );
            return;
        }

        SportsApi api = RetrofitClient.getSportsApi();

        Call<List<OddsApiMatch>> call = api.getOdds(
                sport,
                ApiConstants.API_KEY,
                ApiConstants.REGIONS,
                ApiConstants.MARKETS,
                ApiConstants.ODDS_FORMAT
        );

        call.enqueue(new Callback<List<OddsApiMatch>>() {

            @Override
            public void onResponse(Call<List<OddsApiMatch>> call,
                                   Response<List<OddsApiMatch>> response) {

                if (response.isSuccessful() && response.body() != null
                        && !response.body().isEmpty()) {

                    List<MatchData> matches = mapApiResponseToMatchData(response.body());

                    if (matches.isEmpty()) {
                        // La API respondió pero no hay partidos (fuera de temporada, etc.)
                        callback.onError(
                                "Liga sin partidos activos. Prueba otra liga en el selector ↑",
                                getMockMatches()
                        );
                    } else {
                        callback.onSuccess(matches, "En vivo · The Odds API");
                    }

                } else {
                    String msg = buildHttpErrorMessage(response.code());
                    callback.onError(msg, getMockMatches());
                }
            }

            @Override
            public void onFailure(Call<List<OddsApiMatch>> call, Throwable t) {
                callback.onError("Sin conexión a internet", getMockMatches());
            }
        });
    }

    // ─── Mapeo: OddsApiMatch → MatchData ─────────────────────────────────────

    /**
     * Convierte la lista de respuesta de la API al modelo interno de la app.
     *
     * Para cada partido:
     *  - Itera todos los bookmakers y mercados h2h.
     *  - Suma las cuotas de local/empate/visitante en cada bookmaker.
     *  - Promedia entre todos para obtener el precio justo de mercado.
     *  - Calcula la probabilidad estimada por la IA usando BetEngine.
     */
    private List<MatchData> mapApiResponseToMatchData(List<OddsApiMatch> apiMatches) {
        List<MatchData> result = new ArrayList<>();

        for (OddsApiMatch apiMatch : apiMatches) {
            if (apiMatch.getBookmakers() == null || apiMatch.getBookmakers().isEmpty()) continue;

            double homeOddsSum = 0, awayOddsSum = 0, drawOddsSum = 0;
            int count = 0;

            for (OddsApiMatch.Bookmaker bm : apiMatch.getBookmakers()) {
                if (bm.getMarkets() == null) continue;

                for (OddsApiMatch.Market market : bm.getMarkets()) {
                    if (!"h2h".equals(market.getKey()) || market.getOutcomes() == null) continue;

                    double home = 0, away = 0, draw = 0;

                    for (OddsApiMatch.Outcome outcome : market.getOutcomes()) {
                        String name = outcome.getName();
                        if (apiMatch.getHomeTeam().equals(name)) {
                            home = outcome.getPrice();
                        } else if (apiMatch.getAwayTeam().equals(name)) {
                            away = outcome.getPrice();
                        } else {
                            // La tercera opción siempre es el empate en h2h de fútbol
                            draw = outcome.getPrice();
                        }
                    }

                    if (home > 1.0 && away > 1.0 && draw > 1.0) {
                        homeOddsSum += home;
                        awayOddsSum += away;
                        drawOddsSum += draw;
                        count++;
                    }
                }
            }

            if (count == 0) continue;

            // Promedio de cuotas entre todas las casas de apuestas
            double homeOdds = Math.round((homeOddsSum / count) * 100.0) / 100.0;
            double awayOdds = Math.round((awayOddsSum / count) * 100.0) / 100.0;
            double drawOdds = Math.round((drawOddsSum / count) * 100.0) / 100.0;

            MatchData match = new MatchData();
            match.setId(apiMatch.getId());
            match.setHomeTeam(apiMatch.getHomeTeam());
            match.setAwayTeam(apiMatch.getAwayTeam());
            match.setHomeOdds(homeOdds);
            match.setAwayOdds(awayOdds);
            match.setDrawOdds(drawOdds);
            // Estimación de probabilidad del motor de IA (independiente de la casa)
            match.setAiProbabilityHome(
                    BetEngine.estimateAIProbabilityHome(homeOdds, awayOdds, drawOdds)
            );

            result.add(match);
        }

        return result;
    }

    // ─── Datos mock (fallback sin conexión) ───────────────────────────────────

    public List<MatchData> getMockMatches() {
        List<MatchData> mockMatches = new ArrayList<>();

        // Partido 1: Real Madrid vs Man City
        MatchData m1 = new MatchData();
        m1.setId("mock_1");
        m1.setHomeTeam("Real Madrid");
        m1.setAwayTeam("Man City");
        m1.setHomeOdds(2.10);
        m1.setDrawOdds(3.40);
        m1.setAwayOdds(3.20);
        m1.setAiProbabilityHome(BetEngine.estimateAIProbabilityHome(2.10, 3.20, 3.40));
        mockMatches.add(m1);

        // Partido 2: Arsenal vs Bayern Munich
        MatchData m2 = new MatchData();
        m2.setId("mock_2");
        m2.setHomeTeam("Arsenal");
        m2.setAwayTeam("Bayern Munich");
        m2.setHomeOdds(1.40);
        m2.setDrawOdds(4.50);
        m2.setAwayOdds(7.00);
        m2.setAiProbabilityHome(BetEngine.estimateAIProbabilityHome(1.40, 7.00, 4.50));
        mockMatches.add(m2);

        // Partido 3: PSG vs Barcelona
        MatchData m3 = new MatchData();
        m3.setId("mock_3");
        m3.setHomeTeam("PSG");
        m3.setAwayTeam("Barcelona");
        m3.setHomeOdds(1.95);
        m3.setDrawOdds(3.80);
        m3.setAwayOdds(3.60);
        m3.setAiProbabilityHome(BetEngine.estimateAIProbabilityHome(1.95, 3.60, 3.80));
        mockMatches.add(m3);

        return mockMatches;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String buildHttpErrorMessage(int code) {
        switch (code) {
            case 401: return "API Key inválida. Verifica tu clave en ApiConstants.java";
            case 422: return "Liga no disponible o clave sin permisos para esta liga";
            case 429: return "Límite de peticiones alcanzado. Espera unos minutos";
            default:  return "Error del servidor (" + code + ")";
        }
    }
}
