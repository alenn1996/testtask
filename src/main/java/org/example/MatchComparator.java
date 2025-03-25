package org.example;

import java.util.Comparator;

class MatchComparator implements Comparator<Match> {
    @Override
    public int compare(Match m1, Match m2) {
        // first attempts to sort by goal
        int scoreCompare = Integer.compare(m2.getTotalGoals(), m1.getTotalGoals());
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        // Then by time of match start
        int timeCompare = m2.getStartedAt().compareTo(m1.getStartedAt());
        if (timeCompare != 0) {
            return timeCompare;
        }


        // In case where matches hve been added in a very short amount of time we might need to have matchid comparison as
        //the final checkup because otherwise treeset would think its the same match and completely ignore it
        return Integer.compare(m2.getMatchId(), m1.getMatchId());
    }
}
