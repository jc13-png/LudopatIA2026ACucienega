package com.udg.betmasterai.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.udg.betmasterai.data.local.AppDatabase;
import com.udg.betmasterai.data.local.BetHistory;
import com.udg.betmasterai.data.local.UserBalance;
import com.udg.betmasterai.data.model.MatchData;

import java.util.ArrayList;
import java.util.List;

public class MatchesViewModel extends AndroidViewModel {
    
    private MutableLiveData<List<MatchData>> matchesLiveData;
    private AppDatabase database;
    private LiveData<List<BetHistory>> allBets;
    private LiveData<UserBalance> userBalance;

    public MatchesViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getDatabase(application);
        allBets = database.betDao().getAllBets();
        userBalance = database.betDao().getUserBalance();
        matchesLiveData = new MutableLiveData<>();
        
        // Simulación de datos (Mock)
        List<MatchData> mockMatches = new ArrayList<>();
        
        // Partido 1 (Value Bet simulado)
        MatchData m1 = new MatchData();
        m1.setId(1);
        m1.setHomeTeam("Real Madrid");
        m1.setAwayTeam("Man City");
        m1.setHomeOdds(2.10); // Prob implícita: 47%. Si el motor cree que la real es 60%, hay EV.
        m1.setDrawOdds(3.40);
        m1.setAwayOdds(3.20);
        mockMatches.add(m1);

        // Partido 2 (Alto Riesgo simulado)
        MatchData m2 = new MatchData();
        m2.setId(2);
        m2.setHomeTeam("Arsenal");
        m2.setAwayTeam("Bayern Munich");
        m2.setHomeOdds(1.40); // Prob implícita: 71%. Difícil sacar EV de aquí.
        m2.setDrawOdds(4.50);
        m2.setAwayOdds(7.00);
        mockMatches.add(m2);

        // Partido 3
        MatchData m3 = new MatchData();
        m3.setId(3);
        m3.setHomeTeam("PSG");
        m3.setAwayTeam("Barcelona");
        m3.setHomeOdds(1.95);
        m3.setDrawOdds(3.80);
        m3.setAwayOdds(3.60);
        mockMatches.add(m3);

        matchesLiveData.setValue(mockMatches);
    }

    public LiveData<List<MatchData>> getMatches() {
        return matchesLiveData;
    }
    
    public LiveData<List<BetHistory>> getAllBets() {
        return allBets;
    }
    
    public LiveData<UserBalance> getUserBalance() {
        return userBalance;
    }
}
