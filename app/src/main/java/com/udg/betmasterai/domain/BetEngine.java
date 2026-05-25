package com.udg.betmasterai.domain;

/**
 * Motor de apuestas que encapsula todos los cálculos matemáticos de la IA.
 *
 * ─── CONCEPTO CLAVE ───────────────────────────────────────────────────────────
 * La probabilidad IMPLÍCITA de la cuota NO puede usarse para calcular el EV.
 * Razón: Si P = 1/Cuota y luego calculamos EV = P*(Cuota-1) - (1-P)*1 = 0 siempre.
 *
 * La IA debe tener su PROPIA estimación de probabilidad, independiente de la cuota.
 * Si la IA cree que la probabilidad real es mayor que la implícita → Value Bet (EV > 0).
 * ──────────────────────────────────────────────────────────────────────────────
 */
public class BetEngine {

    /**
     * Factor de ventaja de jugar en casa.
     * Ajuste empírico basado en estudios de fútbol europeo (~0.58 = 58% de ventaja local).
     * El factor se aplica como multiplicador sobre la fuerza cruda derivada de las cuotas.
     */
    private static final double HOME_ADVANTAGE_FACTOR = 1.18;

    // ─────────────────────────────────────────────────────────────────────────
    // ESTIMACIÓN DE PROBABILIDAD (IA independiente de la casa de apuestas)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Estima la probabilidad de victoria del equipo local usando un modelo
     * de fuerza relativa derivada de las cuotas, combinada con un factor de
     * ventaja de local.
     *
     * El flujo es:
     *  1. Extraer la "fuerza bruta" de cada equipo a partir de 1/cuota (probabilidad implícita).
     *  2. Aplicar el factor de ventaja local al equipo anfitrión.
     *  3. Normalizar (dividir entre la suma total) para obtener probabilidades ajustadas.
     *
     * Este proceso produce una probabilidad DIFERENTE a la implícita en la cuota original
     * de la casa, lo que hace posible que exista un EV positivo (Value Bet).
     *
     * @param homeOdds  Cuota decimal del equipo local según la casa de apuestas.
     * @param awayOdds  Cuota decimal del equipo visitante según la casa de apuestas.
     * @param drawOdds  Cuota decimal del empate según la casa de apuestas.
     * @return          Probabilidad ajustada de victoria del equipo local (valor entre 0 y 1).
     */
    public static double estimateAIProbabilityHome(double homeOdds, double awayOdds, double drawOdds) {
        // Paso 1: Probabilidades implícitas brutas (pueden sumar > 1 por el margen de la casa)
        double rawHome = 1.0 / homeOdds;
        double rawAway = 1.0 / awayOdds;
        double rawDraw = 1.0 / drawOdds;

        // Paso 2: Remover el overround (margen de la casa) normalizando
        double totalRaw = rawHome + rawAway + rawDraw;
        double normalizedHome = rawHome / totalRaw;
        double normalizedAway = rawAway / totalRaw;
        double normalizedDraw = rawDraw / totalRaw;

        // Paso 3: Aplicar la ventaja de local (ajuste sobre el equipo anfitrión)
        double adjustedHome = normalizedHome * HOME_ADVANTAGE_FACTOR;

        // Paso 4: Renormalizar para que las probabilidades sigan sumando 1
        double totalAdjusted = adjustedHome + normalizedAway + normalizedDraw;
        return adjustedHome / totalAdjusted;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FUERZA DE EQUIPO (para usos con datos reales de Poisson en el futuro)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcula la probabilidad de ganar del equipo local según la fortaleza relativa
     * de ambos equipos. Este modelo es adecuado para cuando se tienen estadísticas
     * reales (goles esperados, forma reciente, etc.).
     *
     * @param homeStrength Fuerza ofensiva/defensiva del equipo local (e.g., goles esperados).
     * @param awayStrength Fuerza ofensiva/defensiva del equipo visitante.
     * @return Probabilidad de victoria del local (valor entre 0 y 1).
     */
    public static double calculateWinProbabilityFromStrength(double homeStrength, double awayStrength) {
        double totalStrength = homeStrength + awayStrength;
        if (totalStrength == 0) return 0.5;
        return homeStrength / totalStrength;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VALOR ESPERADO (EV)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcula el Valor Esperado (EV) de una apuesta de $1.
     *
     * Fórmula: EV = (P_ganar × Ganancia_neta) − (P_perder × Apuesta)
     *           EV = (P × (Cuota − 1)) − ((1 − P) × 1)
     *
     * IMPORTANTE: La probabilidad que se pasa DEBE ser la estimada por la IA,
     * NO la probabilidad implícita derivada de la cuota. Si se usara 1/Cuota
     * como probabilidad, el EV sería matemáticamente siempre 0.
     *
     * Un EV > 0 indica una "Value Bet": la IA cree que la probabilidad real
     * es mayor que la que la casa de apuestas ha pricedo en la cuota.
     *
     * @param aiEstimatedProbability Probabilidad de ganar estimada por la IA (NO 1/odds).
     * @param odds                   Cuota decimal ofrecida por la casa de apuestas.
     * @return                       EV por cada $1 apostado.
     */
    public static double calculateExpectedValue(double aiEstimatedProbability, double odds) {
        double netProfit = odds - 1.0;
        double probabilityToLose = 1.0 - aiEstimatedProbability;
        return (aiEstimatedProbability * netProfit) - (probabilityToLose * 1.0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRITERIO DE KELLY
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcula el Criterio de Kelly: la fracción óptima del bankroll a apostar
     * para maximizar el crecimiento del capital a largo plazo.
     *
     * Fórmula: f* = (b × p − q) / b
     * Donde:
     *   b = Cuota decimal − 1  (ganancia neta por unidad apostada)
     *   p = Probabilidad de ganar estimada por la IA
     *   q = 1 − p              (probabilidad de perder)
     *
     * Se aplica una fracción del Kelly conservadora ("Half Kelly" = f_full / 2) para
     * reducir la volatilidad del bankroll, lo cual es práctica estándar.
     *
     * @param aiEstimatedProbability Probabilidad de ganar estimada por la IA.
     * @param odds                   Cuota decimal ofrecida por la casa de apuestas.
     * @return                       Fracción del bankroll a apostar (Half Kelly). Nunca negativo.
     */
    public static double calculateKellyCriterion(double aiEstimatedProbability, double odds) {
        double b = odds - 1.0;
        if (b <= 0) return 0;

        double p = aiEstimatedProbability;
        double q = 1.0 - p;

        double fullKelly = (b * p - q) / b;

        // Half Kelly: más conservador, reduce el riesgo de ruina del bankroll
        double halfKelly = fullKelly / 2.0;

        // Nunca retornar un valor negativo (si Kelly es negativo, significa "no apostar")
        return halfKelly > 0 ? halfKelly : 0.0;
    }
}
