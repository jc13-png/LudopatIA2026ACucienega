package com.udg.betmasterai.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OddsApiScore {
    @SerializedName("id")
    private String id;

    @SerializedName("sport_key")
    private String sportKey;

    @SerializedName("commence_time")
    private String commenceTime;

    @SerializedName("completed")
    private boolean completed;

    @SerializedName("home_team")
    private String homeTeam;

    @SerializedName("away_team")
    private String awayTeam;

    @SerializedName("scores")
    private List<Score> scores;

    public String getId() { return id; }
    public boolean isCompleted() { return completed; }
    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public List<Score> getScores() { return scores; }

    public static class Score {
        @SerializedName("name")
        private String name;

        @SerializedName("score")
        private String score;

        public String getName() { return name; }
        public String getScore() { return score; }
    }
}
