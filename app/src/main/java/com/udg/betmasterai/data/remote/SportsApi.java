package com.udg.betmasterai.data.remote;

import com.udg.betmasterai.data.model.MatchData;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface SportsApi {
    // Ejemplo de endpoint, a personalizar por el Ingeniero de Datos
    @GET("matches/today")
    Call<List<MatchData>> getMatchesForToday();
}
