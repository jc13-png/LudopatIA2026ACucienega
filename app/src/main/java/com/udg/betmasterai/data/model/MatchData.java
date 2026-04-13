package com.udg.betmasterai.data.model;

import com.google.gson.annotations.SerializedName;

public class MatchData {
    @SerializedName("id")
    private int id;
    
    @SerializedName("homeTeam")
    private String homeTeam;
    
    @SerializedName("awayTeam")
    private String awayTeam;
    
    @SerializedName("homeOdds")
    private double homeOdds;
    
    @SerializedName("awayOdds")
    private double awayOdds;
    
    @SerializedName("drawOdds")
    private double drawOdds;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }

    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }

    public double getHomeOdds() { return homeOdds; }
    public void setHomeOdds(double homeOdds) { this.homeOdds = homeOdds; }

    public double getAwayOdds() { return awayOdds; }
    public void setAwayOdds(double awayOdds) { this.awayOdds = awayOdds; }

    public double getDrawOdds() { return drawOdds; }
    public void setDrawOdds(double drawOdds) { this.drawOdds = drawOdds; }
}
