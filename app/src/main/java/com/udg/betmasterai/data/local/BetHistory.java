package com.udg.betmasterai.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bet_history")
public class BetHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String matchId;
    private String matchDetails;
    private String selectedTeam;
    private double expectedValue;
    private double suggestedBet;
    private double actualBetAmount;
    private String result; // "WON", "LOST", "PENDING"
    private long timestamp;
    private double odds; // Cuota decimal a la que se realizó la apuesta

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getMatchDetails() { return matchDetails; }
    public void setMatchDetails(String matchDetails) { this.matchDetails = matchDetails; }

    public String getSelectedTeam() { return selectedTeam; }
    public void setSelectedTeam(String selectedTeam) { this.selectedTeam = selectedTeam; }

    public double getExpectedValue() { return expectedValue; }
    public void setExpectedValue(double expectedValue) { this.expectedValue = expectedValue; }

    public double getSuggestedBet() { return suggestedBet; }
    public void setSuggestedBet(double suggestedBet) { this.suggestedBet = suggestedBet; }

    public double getActualBetAmount() { return actualBetAmount; }
    public void setActualBetAmount(double actualBetAmount) { this.actualBetAmount = actualBetAmount; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getOdds() { return odds; }
    public void setOdds(double odds) { this.odds = odds; }
}
