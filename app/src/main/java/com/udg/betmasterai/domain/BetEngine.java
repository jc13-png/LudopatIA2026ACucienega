package com.udg.betmasterai.domain;

public class BetEngine {
    
    // Calcula la probabilidad de ganar según el Algoritmo de Poisson (simulado para este ejemplo base)
    public static double calculateWinProbability(double homeStrength, double awayStrength) {
        // Lógica simplificada de Poisson
        double totalStrength = homeStrength + awayStrength;
        if (totalStrength == 0) return 0.5;
        return homeStrength / totalStrength;
    }

    // Calcula el Expected Value (EV)
    // EV = (Probabilidad de Ganar * Ganancia Neta) - (Probabilidad de Perder * Apuesta)
    public static double calculateExpectedValue(double probabilityToWin, double odds) {
        double netProfit = odds - 1.0;
        double probabilityToLose = 1.0 - probabilityToWin;
        return (probabilityToWin * netProfit) - (probabilityToLose * 1.0);
    }

    // Criterio de Kelly (recomienda % del bankroll a apostar)
    // Kelly % = (bp - q) / b
    // donde b = cuota decimal - 1, p = probabilidad de ganar, q = probabilidad de perder
    public static double calculateKellyCriterion(double probabilityToWin, double odds) {
        double b = odds - 1.0;
        if (b <= 0) return 0; // Evitar división por cero
        double p = probabilityToWin;
        double q = 1.0 - p;
        
        double kellyFraction = (b * p - q) / b;
        
        // Retornar 0 si es negativo (no apostar)
        return kellyFraction > 0 ? kellyFraction : 0.0;
    }
}
