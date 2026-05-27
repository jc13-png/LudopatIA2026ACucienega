package com.udg.betmasterai.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.udg.betmasterai.R;
import com.udg.betmasterai.data.local.BetHistory;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<BetHistory> betsList = new ArrayList<>();

    public void setBets(List<BetHistory> bets) {
        this.betsList = bets;
        notifyDataSetChanged();
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
        holder.tvBetAmount.setText(String.format("$%.2f", bet.getActualBetAmount()));
        holder.tvSelectedTeam.setText(bet.getSelectedTeam() != null ? bet.getSelectedTeam() : "-");
        holder.tvExpectedValue.setText(String.format("+%.2f", bet.getExpectedValue()));

        String status = bet.getResult() != null ? bet.getResult().toUpperCase() : "PENDING";
        
        switch (status) {
            case "WON":
                holder.tvBetStatus.setText("GANADA ✓");
                holder.tvBetStatus.setTextColor(Color.parseColor("#00C853"));
                holder.tvBetStatus.setBackgroundColor(Color.parseColor("#1B5E20"));
                break;
            case "LOST":
                holder.tvBetStatus.setText("PERDIDA ✗");
                holder.tvBetStatus.setTextColor(Color.parseColor("#D50000"));
                holder.tvBetStatus.setBackgroundColor(Color.parseColor("#B71C1C"));
                break;
            default:
                holder.tvBetStatus.setText("PENDIENTE ⏳");
                holder.tvBetStatus.setTextColor(Color.parseColor("#FFB300"));
                holder.tvBetStatus.setBackgroundColor(Color.parseColor("#3E2723"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return betsList != null ? betsList.size() : 0;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvMatchDetails, tvBetStatus, tvBetAmount, tvSelectedTeam, tvExpectedValue;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMatchDetails = itemView.findViewById(R.id.tvMatchDetails);
            tvBetStatus = itemView.findViewById(R.id.tvBetStatus);
            tvBetAmount = itemView.findViewById(R.id.tvBetAmount);
            tvSelectedTeam = itemView.findViewById(R.id.tvSelectedTeam);
            tvExpectedValue = itemView.findViewById(R.id.tvExpectedValue);
        }
    }
}
