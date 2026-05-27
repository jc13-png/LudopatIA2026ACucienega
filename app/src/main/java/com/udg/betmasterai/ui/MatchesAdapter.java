package com.udg.betmasterai.ui;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.animation.AnimationUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.udg.betmasterai.R;
import com.udg.betmasterai.data.model.MatchData;
import com.udg.betmasterai.domain.BetEngine;
import java.util.ArrayList;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {

    private List<MatchData> matchesList = new ArrayList<>();
    private double currentBankroll = 1000.0; // Default inicial

    public void setCurrentBankroll(double bankroll) {
        this.currentBankroll = bankroll;
        notifyDataSetChanged();
    }

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

        // ─── MOTOR DE IA (BetEngine) ────────────────────────────────────────────────
        //
        // CORRECCIÓN MATEMÁTICA: usamos la probabilidad estimada por la IA (aiProbabilityHome),
        // que es DIFERENTE a la probabilidad implícita de la cuota (1/homeOdds).
        //
        // La IA la calcula tomando en cuenta:
        //   1. Normalizar las probabilidades implícitas crudas (eliminar el margen de la casa)
        //   2. Aplicar un factor de ventaja de local empírico (~18%)
        //   3. Renormalizar para que el modelo sea autocontenido
        //
        // Esto asegura que EV = (P_ai * (cuota-1)) - ((1-P_ai) * 1) pueda ser > 0.
        // ────────────────────────────────────────────────────────────────────────────

        double aiProb       = match.getAiProbabilityHome();           // Estimación de la IA (real)
        double impliedProb  = 1.0 / match.getHomeOdds();              // Probabilidad implícita de la casa
        double ev           = BetEngine.calculateExpectedValue(aiProb, match.getHomeOdds());
        double kelly        = BetEngine.calculateKellyCriterion(aiProb, match.getHomeOdds());

        if (ev > 0) {
            holder.cardIAIndicator.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.status_won));
            holder.tvIAStatus.setText("VALUE BET ✓");
            holder.tvIADetails.setText(String.format(
                    "IA: %.1f%% prob. | Casa: %.1f%% prob.\n" +
                    "Apostar %.1f%% del Bankroll (Half Kelly). EV: +%.2f por $1 apostado.",
                    aiProb * 100,
                    impliedProb * 100,
                    kelly * 100,
                    ev));
        } else {
            holder.cardIAIndicator.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.status_lost));
            holder.tvIAStatus.setText("ALTO RIESGO ✗");
            holder.tvIADetails.setText(String.format(
                    "IA: %.1f%% prob. | Casa: %.1f%% prob.\n" +
                    "EV negativo (%.2f). La IA sugiere NO apostar en este evento.",
                    aiProb * 100,
                    impliedProb * 100,
                    ev));
        }

        // Configurar botón Apostar
        holder.btnBet.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BetActivity.class);
            intent.putExtra("match_details", match.getHomeTeam() + " vs " + match.getAwayTeam());
            intent.putExtra("expected_value", ev);
            intent.putExtra("suggested_bet", kelly * currentBankroll);
            intent.putExtra("home_odds", match.getHomeOdds());
            v.getContext().startActivity(intent);
        });

        // Animación de entrada para el item
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in));
    }

    @Override
    public int getItemCount() {
        return matchesList != null ? matchesList.size() : 0;
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeams, tvHomeOdds, tvDrawOdds, tvAwayOdds, tvIAStatus, tvIADetails;
        MaterialCardView cardIAIndicator;
        MaterialButton btnBet;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeams = itemView.findViewById(R.id.tvTeams);
            tvHomeOdds = itemView.findViewById(R.id.tvHomeOdds);
            tvDrawOdds = itemView.findViewById(R.id.tvDrawOdds);
            tvAwayOdds = itemView.findViewById(R.id.tvAwayOdds);
            tvIAStatus = itemView.findViewById(R.id.tvIAStatus);
            tvIADetails = itemView.findViewById(R.id.tvIADetails);
            cardIAIndicator = itemView.findViewById(R.id.cardIAIndicator);
            btnBet = itemView.findViewById(R.id.btnBet);
        }
    }
}
