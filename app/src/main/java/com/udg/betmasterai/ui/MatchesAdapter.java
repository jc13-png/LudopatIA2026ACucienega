package com.udg.betmasterai.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.udg.betmasterai.R;
import com.udg.betmasterai.data.model.MatchData;
import com.udg.betmasterai.domain.BetEngine;
import java.util.ArrayList;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {

    private List<MatchData> matchesList = new ArrayList<>();

    public void setMatches(List<MatchData> matches) {
        this.matchesList = matches;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchData match = matchesList.get(position);
        
        holder.tvTeams.setText(match.getHomeTeam() + " vs " + match.getAwayTeam());
        holder.tvHomeOdds.setText(String.valueOf(match.getHomeOdds()));
        holder.tvDrawOdds.setText(String.valueOf(match.getDrawOdds()));
        holder.tvAwayOdds.setText(String.valueOf(match.getAwayOdds()));

        // --- LÓGICA SIMULADA CON NUESTRO MOTOR (BetEngine) ---
        // Asumiendo que las cuotas muy altas son riesgosas y las bajas son seguras (solo como base simulada)
        // 1 / Cuota = probabilidad implicita simulada.
        double probHome = 1.0 / match.getHomeOdds();
        double ev = BetEngine.calculateExpectedValue(probHome, match.getHomeOdds());
        double kelly = BetEngine.calculateKellyCriterion(probHome, match.getHomeOdds());

        // Evaluar "Value Bet" (Si esperamos ganancia)
        if (ev > 0) {
            holder.cardIAIndicator.setCardBackgroundColor(Color.parseColor("#00C853")); // Verde
            holder.tvIAStatus.setText("VALUE BET");
            holder.tvIADetails.setText(String.format("La IA sugiere apostar %.1f%% de tu Bankroll al Local. (EV: +%.2f)", (kelly * 100), ev));
        } else {
            holder.cardIAIndicator.setCardBackgroundColor(Color.parseColor("#D50000")); // Rojo
            holder.tvIAStatus.setText("ALTO RIESGO");
            holder.tvIADetails.setText("EV Negativo. Sugerencia de la IA: Mejor evitar apostar en este evento.");
        }
    }

    @Override
    public int getItemCount() {
        return matchesList != null ? matchesList.size() : 0;
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeams, tvHomeOdds, tvDrawOdds, tvAwayOdds, tvIAStatus, tvIADetails;
        MaterialCardView cardIAIndicator;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeams = itemView.findViewById(R.id.tvTeams);
            tvHomeOdds = itemView.findViewById(R.id.tvHomeOdds);
            tvDrawOdds = itemView.findViewById(R.id.tvDrawOdds);
            tvAwayOdds = itemView.findViewById(R.id.tvAwayOdds);
            tvIAStatus = itemView.findViewById(R.id.tvIAStatus);
            tvIADetails = itemView.findViewById(R.id.tvIADetails);
            cardIAIndicator = itemView.findViewById(R.id.cardIAIndicator);
        }
    }
}
