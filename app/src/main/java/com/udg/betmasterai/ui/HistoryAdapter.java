package com.udg.betmasterai.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.udg.betmasterai.R;
import com.udg.betmasterai.data.local.BetHistory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para mostrar el historial de apuestas con soporte para animaciones de entrada,
 * formateo de datos, y botones de acción manual para definir el resultado de la apuesta.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface OnBetInteractionListener {
        void onMarkWon(BetHistory bet);
        void onMarkLost(BetHistory bet);
    }

    private List<BetHistory> betsList = new ArrayList<>();
    private OnBetInteractionListener listener;
    private final DecimalFormat df = new DecimalFormat("0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public void setBets(List<BetHistory> bets) {
        this.betsList = bets;
        notifyDataSetChanged();
    }

    public void setOnBetInteractionListener(OnBetInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bet_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        BetHistory bet = betsList.get(position);

        holder.tvMatchDetails.setText(bet.getMatchDetails() != null ? bet.getMatchDetails() : "Partido Desconocido");
        holder.tvDate.setText(dateFormat.format(new Date(bet.getTimestamp())));
        holder.tvBetAmount.setText(String.format("$%.2f", bet.getActualBetAmount()));
        holder.tvSelectedTeam.setText(bet.getSelectedTeam() != null ? bet.getSelectedTeam() : "-");
        holder.tvOdds.setText(df.format(bet.getOdds() > 0 ? bet.getOdds() : 1.0));
        
        // EV formato: expectedValue es un porcentaje en decimal (ej: 0.12 para 12%) o absoluto.
        double evVal = bet.getExpectedValue() * 100;
        String evStr = (evVal >= 0 ? "+" : "") + df.format(evVal) + "%";
        holder.tvExpectedValue.setText(evStr);

        // Diseño de Estado
        String status = bet.getResult() != null ? bet.getResult().toUpperCase() : "PENDING";
        holder.tvBetStatus.setText(status);

        if ("WON".equals(status)) {
            holder.cardStatus.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.status_won));
            holder.tvBetStatus.setTextColor(Color.WHITE);
            holder.llActions.setVisibility(View.GONE);
        } else if ("LOST".equals(status)) {
            holder.cardStatus.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.status_lost));
            holder.tvBetStatus.setTextColor(Color.WHITE);
            holder.llActions.setVisibility(View.GONE);
        } else { // PENDING
            holder.cardStatus.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending));
            holder.tvBetStatus.setTextColor(Color.WHITE);
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
        return betsList != null ? betsList.size() : 0;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvMatchDetails, tvDate, tvBetStatus, tvBetAmount, tvSelectedTeam, tvOdds, tvExpectedValue;
        MaterialCardView cardStatus;
        View llActions;
        MaterialButton btnWon, btnLost;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMatchDetails = itemView.findViewById(R.id.tvMatchDetails);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvBetStatus = itemView.findViewById(R.id.tvBetStatus);
            tvBetAmount = itemView.findViewById(R.id.tvBetAmount);
            tvSelectedTeam = itemView.findViewById(R.id.tvSelectedTeam);
            tvOdds = itemView.findViewById(R.id.tvOdds);
            tvExpectedValue = itemView.findViewById(R.id.tvExpectedValue);
            cardStatus = itemView.findViewById(R.id.cardStatus);
            llActions = itemView.findViewById(R.id.llActions);
            btnWon = itemView.findViewById(R.id.btnWon);
            btnLost = itemView.findViewById(R.id.btnLost);
        }
    }
}
