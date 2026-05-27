package com.udg.betmasterai.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.udg.betmasterai.R;
import com.udg.betmasterai.data.local.AppDatabase;
import com.udg.betmasterai.data.local.BetHistory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Pantalla que muestra el historial de apuestas realizadas y permite marcarlas como GANADAS o PERDIDAS.
 */
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvBetHistory;
    private TextView tvEmptyState;
    private BetHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvBetHistory = findViewById(R.id.rvBetHistory);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvBetHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BetHistoryAdapter(new ArrayList<>(), new BetHistoryAdapter.OnBetInteractionListener() {
            @Override
            public void onMarkWon(BetHistory bet) {
                actualizarResultadoApuesta(bet.getId(), "WON");
            }

            @Override
            public void onMarkLost(BetHistory bet) {
                actualizarResultadoApuesta(bet.getId(), "LOST");
            }
        });
        rvBetHistory.setAdapter(adapter);

        // Observar apuestas desde Room
        AppDatabase.getDatabase(this).betDao().getAllBets().observe(this, bets -> {
            if (bets == null || bets.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                rvBetHistory.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvBetHistory.setVisibility(View.VISIBLE);
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

    // ─── Adaptador Recycler View ─────────────────────────────────────────────
    public static class BetHistoryAdapter extends RecyclerView.Adapter<BetHistoryAdapter.ViewHolder> {

        public interface OnBetInteractionListener {
            void onMarkWon(BetHistory bet);
            void onMarkLost(BetHistory bet);
        }

        private List<BetHistory> bets;
        private final OnBetInteractionListener listener;
        private final DecimalFormat df = new DecimalFormat("0.00");
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

        public BetHistoryAdapter(List<BetHistory> bets, OnBetInteractionListener listener) {
            this.bets = bets;
            this.listener = listener;
        }

        public void setBets(List<BetHistory> bets) {
            this.bets = bets;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bet_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BetHistory bet = bets.get(position);

            holder.tvMatchDetails.setText(bet.getMatchDetails());
            holder.tvDate.setText(dateFormat.format(new Date(bet.getTimestamp())));
            holder.tvAmount.setText("$" + df.format(bet.getActualBetAmount()));
            holder.tvOdds.setText(df.format(bet.getOdds() > 0 ? bet.getOdds() : 1.0));
            
            // EV formato: expectedValue es un porcentaje en decimal (ej: 0.12 para 12%) o absoluto.
            // Para mantener consistencia con el resto del diseño, lo multiplicamos por 100 si es decimal.
            double evVal = bet.getExpectedValue() * 100;
            String evStr = (evVal >= 0 ? "+" : "") + df.format(evVal) + "%";
            holder.tvEV.setText(evStr);

            // Diseño de Estado
            String status = bet.getResult() != null ? bet.getResult().toUpperCase() : "PENDING";
            holder.tvStatus.setText(status);

            if ("WON".equals(status)) {
                holder.cardStatus.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.status_won)); // Verde
                holder.tvStatus.setTextColor(Color.WHITE);
                holder.llActions.setVisibility(View.GONE);
            } else if ("LOST".equals(status)) {
                holder.cardStatus.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.status_lost)); // Rojo
                holder.tvStatus.setTextColor(Color.WHITE);
                holder.llActions.setVisibility(View.GONE);
            } else { // PENDING
                holder.cardStatus.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending)); // Amarillo / Naranja
                holder.tvStatus.setTextColor(Color.WHITE);
                holder.llActions.setVisibility(View.VISIBLE);
            }

            holder.btnWon.setOnClickListener(v -> {
                if (listener != null) listener.onMarkWon(bet);
            });

            holder.btnLost.setOnClickListener(v -> {
                if (listener != null) listener.onMarkLost(bet);
            });

            // Animación de entrada para el item
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in));
        }

        @Override
        public int getItemCount() {
            return bets.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMatchDetails, tvDate, tvStatus, tvAmount, tvOdds, tvEV;
            MaterialCardView cardStatus;
            View llActions;
            MaterialButton btnWon, btnLost;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMatchDetails = itemView.findViewById(R.id.tvMatchDetails);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvOdds = itemView.findViewById(R.id.tvOdds);
                tvEV = itemView.findViewById(R.id.tvEV);
                cardStatus = itemView.findViewById(R.id.cardStatus);
                llActions = itemView.findViewById(R.id.llActions);
                btnWon = itemView.findViewById(R.id.btnWon);
                btnLost = itemView.findViewById(R.id.btnLost);
            }
        }
    }
}
