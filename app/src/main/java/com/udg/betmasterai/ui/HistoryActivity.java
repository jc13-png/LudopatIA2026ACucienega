package com.udg.betmasterai.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.udg.betmasterai.R;
import com.udg.betmasterai.data.local.AppDatabase;
import com.udg.betmasterai.data.local.BetHistory;

/**
 * Pantalla que muestra el historial de apuestas realizadas, permitiendo navegar hacia atrás,
 * visualizar un estado vacío, y marcar manualmente las apuestas como GANADAS o PERDIDAS.
 */
public class HistoryActivity extends AppCompatActivity {

    private MatchesViewModel viewModel;
    private HistoryAdapter adapter;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvEmptyState = findViewById(R.id.tvEmptyState);
        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        
        adapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        // Configurar acciones manuales para definir el resultado de la apuesta
        adapter.setOnBetInteractionListener(new HistoryAdapter.OnBetInteractionListener() {
            @Override
            public void onMarkWon(BetHistory bet) {
                actualizarResultadoApuesta(bet.getId(), "WON");
            }

            @Override
            public void onMarkLost(BetHistory bet) {
                actualizarResultadoApuesta(bet.getId(), "LOST");
            }
        });

        // Obtener el ViewModel para observar LiveData de apuestas
        viewModel = new ViewModelProvider(this).get(MatchesViewModel.class);
        viewModel.getAllBets().observe(this, bets -> {
            if (bets == null || bets.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);
                adapter.setBets(bets);
            }
        });
    }

    private void actualizarResultadoApuesta(int id, String result) {
        new Thread(() -> {
            AppDatabase.getDatabase(getApplicationContext())
                    .betDao()
                    .updateBetResult(id, result);
            
            runOnUiThread(() -> {
                Toast.makeText(HistoryActivity.this, "Apuesta actualizada a " + result, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
