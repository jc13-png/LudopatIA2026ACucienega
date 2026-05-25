package com.udg.betmasterai.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConstants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /** Atajo para obtener directamente la interfaz de la API de deportes. */
    public static SportsApi getSportsApi() {
        return getRetrofitInstance().create(SportsApi.class);
    }
}
