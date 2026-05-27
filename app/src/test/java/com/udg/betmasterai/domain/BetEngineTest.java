package com.udg.betmasterai.domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class BetEngineTest {

    private static final double DELTA = 1e-6;

    @Test
    public void testEstimateAIProbabilityHome() {
        // Test with simple odds
        double homeOdds = 2.0;
        double awayOdds = 3.0;
        double drawOdds = 3.0;

        // Probabilities: rawHome = 0.5, rawAway = 0.333333, rawDraw = 0.333333
        // sumRaw = 1.166666
        // normalizedHome = 0.5 / 1.166666 = 0.428571
        // normalizedAway = 0.333333 / 1.166666 = 0.285714
        // normalizedDraw = 0.285714
        // adjustedHome = 0.428571 * 1.18 = 0.505714
        // sumAdjusted = 0.505714 + 0.285714 + 0.285714 = 1.077142
        // final probability = 0.505714 / 1.077142 = 0.4695
        double prob = BetEngine.estimateAIProbabilityHome(homeOdds, awayOdds, drawOdds);
        assertTrue("Probability should be between 0 and 1", prob > 0 && prob < 1);
        assertEquals(0.469502, prob, 1e-4);
    }

    @Test
    public void testCalculateExpectedValue_PositiveEV() {
        // AI expects 60% win rate (0.60) on a 2.0 odds bet.
        // EV = 0.60 * (2.0 - 1) - (1 - 0.60) * 1 = 0.60 * 1.0 - 0.40 * 1.0 = +0.20
        double ev = BetEngine.calculateExpectedValue(0.60, 2.0);
        assertEquals(0.20, ev, DELTA);
    }

    @Test
    public void testCalculateExpectedValue_ZeroEVWithImpliedProbability() {
        // When using implied probability (P = 1/odds), EV should be 0.
        // For odds 2.5, P = 0.4.
        // EV = 0.4 * (2.5 - 1) - (1 - 0.4) * 1 = 0.4 * 1.5 - 0.6 * 1.0 = 0.6 - 0.6 = 0.0
        double ev = BetEngine.calculateExpectedValue(1.0 / 2.5, 2.5);
        assertEquals(0.0, ev, DELTA);
    }

    @Test
    public void testCalculateExpectedValue_NegativeEV() {
        // AI expects 30% win rate on a 2.0 odds bet.
        // EV = 0.3 * 1.0 - 0.7 * 1.0 = -0.40
        double ev = BetEngine.calculateExpectedValue(0.30, 2.0);
        assertEquals(-0.40, ev, DELTA);
    }

    @Test
    public void testCalculateKellyCriterion_NormalValue() {
        // P = 0.60, odds = 2.0
        // fullKelly = (1.0 * 0.6 - 0.4) / 1.0 = 0.20
        // halfKelly = 0.10
        double kelly = BetEngine.calculateKellyCriterion(0.60, 2.0);
        assertEquals(0.10, kelly, DELTA);
    }

    @Test
    public void testCalculateKellyCriterion_NeverNegative() {
        // Negative EV: P = 0.30, odds = 2.0
        // fullKelly = (1.0 * 0.3 - 0.7) / 1.0 = -0.40
        // Result should be 0.0 (no bet)
        double kelly = BetEngine.calculateKellyCriterion(0.30, 2.0);
        assertEquals(0.0, kelly, DELTA);
    }

    @Test
    public void testCalculateKellyCriterion_InvalidOdds() {
        // Odds <= 1.0 should result in 0.0
        double kelly = BetEngine.calculateKellyCriterion(0.50, 0.9);
        assertEquals(0.0, kelly, DELTA);
    }
}
