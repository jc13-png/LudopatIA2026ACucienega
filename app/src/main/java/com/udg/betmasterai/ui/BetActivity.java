package com.udg.betmasterai.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.udg.betmasterai.data.local.AppDatabase;
import com.udg.betmasterai.data.local.BetHistory;

import java.text.DecimalFormat;

/**
 * Pantalla para registrar una apuesta manualmente.
 *
 * Recibe via Intent los datos del partido seleccionado:
 *   - "match_id"       → String  ID de la API
 *   - "match_details"  → String  nombre del partido
 *   - "expected_value" → double  EV calculado por BetEngine
 *   - "suggested_bet"  → double  monto sugerido por Half Kelly
 *   - "home_odds"      → double  cuota del equipo local
 *
 * Al guardar, inserta un BetHistory en Room en un hilo background.
 */
public class BetActivity extends AppCompatActivity {

    // ─── Vistas ──────────────────────────────────────────────────────────────

    private TextView         tvMatchDetails;
    private TextView         tvEV;
    private TextView         tvOdds;
    private TextView         tvSuggestedBet;
    private TextInputEditText etActualBet;
    private RadioGroup       rgForecast;
    private MaterialButton   btnSaveBet;
    private MaterialButton   btnCancel;

    // ─── Datos recibidos del partido ─────────────────────────────────────────

    private String matchId;
    private String matchDetails;
    private double expectedValue;
    private double suggestedBet;
    private double homeOdds;

    // ─── Formateador de números ───────────────────────────────────────────────

    private final DecimalFormat df = new DecimalFormat("0.00");

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.udg.betmasterai.R.layout.activity_bet);

        // 1. Leer datos del Intent
        leerDatosDelIntent();

        // 2. Conectar vistas
        inicializarVistas();

        // 3. Mostrar los datos del partido en pantalla
        mostrarDatosDelPartido();

        // 4. Configurar botones
        configurarBotones();
    }

    // ─── Paso 1: Leer los extras del Intent ──────────────────────────────────

    private void leerDatosDelIntent() {
        Intent intent = getIntent();
        matchId       = intent.getStringExtra("match_id");
        matchDetails  = intent.getStringExtra("match_details");
        expectedValue = intent.getDoubleExtra("expected_value", 0.0);
        suggestedBet  = intent.getDoubleExtra("suggested_bet", 0.0);
        homeOdds      = intent.getDoubleExtra("home_odds", 0.0);
    }

    // ─── Paso 2: Conectar vistas con sus IDs del layout ──────────────────────

    private void inicializarVistas() {
        tvMatchDetails  = findViewById(com.udg.betmasterai.R.id.tvMatchDetails);
        tvEV            = findViewById(com.udg.betmasterai.R.id.tvEV);
        tvOdds          = findViewById(com.udg.betmasterai.R.id.tvOdds);
        tvSuggestedBet  = findViewById(com.udg.betmasterai.R.id.tvSuggestedBet);
        etActualBet     = findViewById(com.udg.betmasterai.R.id.etActualBet);
        rgForecast      = findViewById(com.udg.betmasterai.R.id.rgForecast);
        btnSaveBet      = findViewById(com.udg.betmasterai.R.id.btnSaveBet);
        btnCancel       = findViewById(com.udg.betmasterai.R.id.btnCancel);
    }

    // ─── Paso 3: Llenar las vistas con los datos del partido ─────────────────

    private void mostrarDatosDelPartido() {
        tvMatchDetails.setText(matchDetails);

        // EV: mostrar con signo + si es positivo
        String evText = expectedValue >= 0
                ? "+" + df.format(expectedValue)
                : df.format(expectedValue);
        tvEV.setText(evText);

        tvOdds.setText(df.format(homeOdds));

        // Monto sugerido por Kelly pre-llenado
        tvSuggestedBet.setText("$" + df.format(suggestedBet));
        etActualBet.setText(df.format(suggestedBet));
    }

    // ─── Paso 4: Lógica de los botones ───────────────────────────────────────

    private void configurarBotones() {

        // Botón GUARDAR
        btnSaveBet.setOnClickListener(v -> {

            // Validar que el monto no esté vacío
            String montoTexto = etActualBet.getText() != null
                    ? etActualBet.getText().toString().trim()
                    : "";

            if (montoTexto.isEmpty()) {
                Toast.makeText(this, "Escribe el monto a apostar", Toast.LENGTH_SHORT).show();
                return;
            }

            double montoReal;
            try {
                montoReal = Double.parseDouble(montoTexto);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "El monto no es válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (montoReal <= 0) {
                Toast.makeText(this, "El monto debe ser mayor a $0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Leer el equipo seleccionado
            String pronostico = obtenerPronosticoSeleccionado();

            // Construir el objeto BetHistory para guardar en Room
            BetHistory apuesta = new BetHistory();
            apuesta.setMatchId(matchId);
            apuesta.setMatchDetails(matchDetails);
            apuesta.setSelectedTeam(pronostico);
            apuesta.setExpectedValue(expectedValue);
            apuesta.setSuggestedBet(suggestedBet);
            apuesta.setActualBetAmount(montoReal);
            apuesta.setResult("PENDING"); // SIEMPRE pendiente inicialmente
            apuesta.setTimestamp(System.currentTimeMillis());
            apuesta.setOdds(homeOdds);

            // Insertar en la base de datos en un hilo background
            // (Room no permite operaciones de BD en el hilo principal)
            new Thread(() -> {
                AppDatabase.getDatabase(getApplicationContext())
                        .betDao()
                        .insertBet(apuesta);

                // Regresar al hilo principal para mostrar el Toast y cerrar
                runOnUiThread(() -> {
                    Toast.makeText(this, "✓ Apuesta guardada correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra BetActivity y regresa a MainActivity
                });
            }).start();
        });

        // Botón CANCELAR — simplemente cierra la pantalla sin guardar nada
        btnCancel.setOnClickListener(v -> finish());
    }

    // ─── Helper: leer el pronóstico seleccionado ────────────────────────────

    private String obtenerPronosticoSeleccionado() {
        int seleccionId = rgForecast.getCheckedRadioButtonId();

        if (seleccionId == com.udg.betmasterai.R.id.rbHome) {
            return "Local";
        } else if (seleccionId == com.udg.betmasterai.R.id.rbDraw) {
            return "Empate";
        } else {
            return "Visita";
        }
    }
}
