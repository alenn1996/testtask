package org.example;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;

@Getter
@Setter
public class Match {
    private final String homeTeam;
    private final String awayTeam;
    private int homeScore;
    private int awayScore;
    private final int matchId;
    private final Instant startedAt;

    public Match(int matchId, String homeTeam, String awayTeam) {
        //adding null and empty string checks
        if (homeTeam == null || awayTeam == null || StringUtils.isBlank(homeTeam) || StringUtils.isBlank(awayTeam)) {
            throw new IllegalArgumentException("There is no ongoing match with id " + matchId);
        }
        this.matchId = matchId;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = 0;
        this.awayScore = 0;
        this.startedAt = Instant.now();
    }


    public void updateScore(int homeScore, int awayScore) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public int getTotalGoals() {
        return homeScore + awayScore;
    }


    @Override
    public String toString() {
        return String.format("%s %d - %d %s", homeTeam, homeScore, awayScore, awayTeam);
    }
}