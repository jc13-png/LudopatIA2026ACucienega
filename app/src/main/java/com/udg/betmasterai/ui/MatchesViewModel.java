package com.udg.betmasterai.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.udg.betmasterai.data.local.AppDatabase;
import com.udg.betmasterai.data.local.BetHistory;
import com.udg.betmasterai.data.local.UserBalance;
import com.udg.betmasterai.data.model.MatchData;
import com.udg.betmasterai.data.remote.ApiConstants;
import com.udg.betmasterai.data.repository.MatchRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatchesViewModel extends AndroidViewModel {

    // ─── LiveData expuesto a la UI ────────────────────────────────────────────

    private final MutableLiveData<List<MatchData>> matchesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String>  statusMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading     = new MutableLiveData<>(false);

    // ─── Dependencias ─────────────────────────────────────────────────────────

    private final AppDatabase     database;
    private final MatchRepository repository;
    private final LiveData<List<BetHistory>> allBets;
    private final LiveData<UserBalance>      userBalance;

    // ─── Auto-refresh ─────────────────────────────────────────────────────────

    private final Handler        refreshHandler = new Handler(Looper.getMainLooper());
    private static final int     REFRESH_MS     = ApiConstants.REFRESH_INTERVAL_SECONDS * 1000;

    /**
     * Runnable que se ejecuta en el hilo principal cada REFRESH_MS milisegundos.
     * Llama a fetchMatches() y luego se re-registra a sí mismo para el siguiente ciclo.
     */
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchMatches();
            refreshHandler.postDelayed(this, REFRESH_MS);
        }
    };

    // ─── Constructor ─────────────────────────────────────────────────────────

    public MatchesViewModel(@NonNull Application application) {
        super(application);

        database    = AppDatabase.getDatabase(application);
        repository  = new MatchRepository();
        allBets     = database.betDao().getAllBets();
        userBalance = database.betDao().getUserBalance();

        // Primera carga inmediata al abrir la app
        fetchMatches();

        // Programar refresco automático cada REFRESH_INTERVAL_SECONDS segundos
        refreshHandler.postDelayed(refreshRunnable, REFRESH_MS);
    }

    // ─── Carga de datos ───────────────────────────────────────────────────────

    private String currentSport = ApiConstants.DEFAULT_SPORT;

    /**
     * Solicita datos frescos al repositorio para el deporte seleccionado por defecto.
     */
    public void fetchMatches() {
        fetchMatchesBySport(currentSport);
    }

    /**
     * Solicita datos frescos al repositorio para una liga/deporte específico.
     */
    public void fetchMatchesBySport(String sport) {
        this.currentSport = sport;
        isLoading.setValue(true);
        statusMessage.setValue("Actualizando…");

        repository.fetchMatches(sport, new MatchRepository.MatchesCallback() {

            @Override
            public void onSuccess(List<MatchData> matches, String source) {
                matchesLiveData.postValue(matches);
                isLoading.postValue(false);
                statusMessage.postValue("✓ " + source + " · " + now());
            }

            @Override
            public void onError(String errorMessage, List<MatchData> fallbackData) {
                matchesLiveData.postValue(fallbackData);
                isLoading.postValue(false);
                statusMessage.postValue("⚠ " + errorMessage + "  (datos demo)");
            }
        });
    }

    // ─── Limpieza al destruir el ViewModel ────────────────────────────────────

    @Override
    protected void onCleared() {
        super.onCleared();
        // Cancelar el auto-refresh para evitar memory leaks
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    // ─── Getters de LiveData ──────────────────────────────────────────────────

    public LiveData<List<MatchData>>    getMatches()       { return matchesLiveData; }
    public LiveData<String>             getStatusMessage() { return statusMessage; }
    public LiveData<Boolean>            getIsLoading()     { return isLoading; }
    public LiveData<List<BetHistory>>   getAllBets()        { return allBets; }
    public LiveData<UserBalance>        getUserBalance()    { return userBalance; }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String now() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
