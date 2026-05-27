package com.udg.betmasterai.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.udg.betmasterai.R;
import com.udg.betmasterai.data.local.BetHistory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private MatchesViewModel     viewModel;
    private SwipeRefreshLayout   swipeRefreshLayout;
    private ProgressBar          progressBar;
    private TextView             tvStatus;
    private LineChart            bankrollChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MatchesViewModel.class);

        // Vistas
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar        = findViewById(R.id.progressBar);
        tvStatus           = findViewById(R.id.tvStatus);
        bankrollChart      = findViewById(R.id.bankrollChart);

        android.widget.ImageView btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, HistoryActivity.class));
        });

        // ─── RecyclerView de Partidos ──────────────────────────────────────────
        RecyclerView rvMatches = findViewById(R.id.rvMatches);
        MatchesAdapter adapter = new MatchesAdapter();
        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        rvMatches.setAdapter(adapter);

        viewModel.getMatches().observe(this, adapter::setMatches);

        // ─── Indicadores de estado en tiempo real ─────────────────────────────

        // Texto de estado (hora de actualización o mensaje de error)
        viewModel.getStatusMessage().observe(this, msg -> {
            tvStatus.setText(msg);
            // Verde si OK, amarillo si warning, gris si cargando
            if (msg != null && msg.startsWith("✓")) {
                tvStatus.setTextColor(Color.parseColor("#03DAC5"));
            } else if (msg != null && msg.startsWith("⚠")) {
                tvStatus.setTextColor(Color.parseColor("#FFB300"));
            } else {
                tvStatus.setTextColor(Color.parseColor("#888888"));
            }
        });

        // Spinner de carga junto al header
        viewModel.getIsLoading().observe(this, loading -> {
            progressBar.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE);
            // El SwipeRefreshLayout también muestra su propio indicador cuando cargamos
            if (swipeRefreshLayout.isRefreshing() && (loading == null || !loading)) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // ─── SwipeRefreshLayout (actualización manual al deslizar) ────────────
        swipeRefreshLayout.setColorSchemeColors(
                Color.parseColor("#BB86FC"),  // Púrpura
                Color.parseColor("#03DAC5"),  // Teal
                Color.parseColor("#00C853")   // Verde
        );
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.parseColor("#1E1E1E"));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            viewModel.fetchMatches();
        });

        // ─── Gráfico de Bankroll ──────────────────────────────────────────────
        setupBankrollChart();

        viewModel.getAllBets().observe(this, bets -> {
            updateBankrollChart(bets);
            
            // Calcular el bankroll actual y recopilar partidos apostados para el adaptador
            double currentBankroll = 1000.0;
            Set<String> bettedMatches = new HashSet<>();
            
            if (bets != null) {
                for (BetHistory bet : bets) {
                    if (bet.getMatchId() != null) {
                        bettedMatches.add(bet.getMatchId());
                    }
                    if ("WON".equalsIgnoreCase(bet.getResult())) {
                        currentBankroll += bet.getActualBetAmount() * bet.getExpectedValue();
                    } else if ("LOST".equalsIgnoreCase(bet.getResult())) {
                        currentBankroll -= bet.getActualBetAmount();
                    }
                }
            }
            adapter.setCurrentBankroll(currentBankroll);
            adapter.setBettedMatches(bettedMatches);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GRÁFICO DE BANKROLL
    // ─────────────────────────────────────────────────────────────────────────

    private void setupBankrollChart() {
        bankrollChart.setBackgroundColor(Color.TRANSPARENT);
        bankrollChart.setDrawGridBackground(false);
        bankrollChart.setDrawBorders(false);

        Description desc = new Description();
        desc.setText("");
        bankrollChart.setDescription(desc);

        bankrollChart.setPinchZoom(true);
        bankrollChart.setScaleXEnabled(true);
        bankrollChart.setScaleYEnabled(false);

        bankrollChart.getLegend().setTextColor(Color.argb(200, 255, 255, 255));
        bankrollChart.getLegend().setTextSize(11f);

        XAxis xAxis = bankrollChart.getXAxis();
        xAxis.setTextColor(Color.argb(140, 255, 255, 255));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == 0 ? "Inicio" : "#" + (int) value;
            }
        });

        YAxis leftAxis = bankrollChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#BB86FC"));
        leftAxis.setGridColor(Color.argb(30, 255, 255, 255));
        leftAxis.setDrawAxisLine(false);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "$" + (int) value;
            }
        });

        bankrollChart.getAxisRight().setEnabled(false);

        bankrollChart.setNoDataText("Aún no hay apuestas registradas.\nEl gráfico aparecerá automáticamente.");
        bankrollChart.setNoDataTextColor(Color.argb(130, 255, 255, 255));
        bankrollChart.invalidate();
    }

    private void updateBankrollChart(List<BetHistory> bets) {
        if (bets == null || bets.isEmpty()) {
            bankrollChart.clear();
            bankrollChart.invalidate();
            return;
        }

        // La consulta Room devuelve orden DESC; invertir para graficar izq → der
        List<BetHistory> ordered = new ArrayList<>(bets);
        Collections.reverse(ordered);

        double balance = 1000.0;
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, (float) balance));

        for (int i = 0; i < ordered.size(); i++) {
            BetHistory bet = ordered.get(i);
            if ("WON".equalsIgnoreCase(bet.getResult())) {
                balance += bet.getActualBetAmount() * (bet.getExpectedValue());
            } else if ("LOST".equalsIgnoreCase(bet.getResult())) {
                balance -= bet.getActualBetAmount();
            }
            entries.add(new Entry(i + 1, (float) balance));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Bankroll ($)");
        dataSet.setColor(Color.parseColor("#BB86FC"));
        dataSet.setValueTextColor(Color.TRANSPARENT);
        dataSet.setLineWidth(2.5f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.parseColor("#03DAC5"));
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleColor(Color.parseColor("#1E1E1E"));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#BB86FC"));
        dataSet.setFillAlpha(40);
        dataSet.setHighLightColor(Color.parseColor("#03DAC5"));

        bankrollChart.setData(new LineData(dataSet));
        bankrollChart.animateX(600);
        bankrollChart.invalidate();
    }
}
