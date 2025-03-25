package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Scoreboard {
    private static final Logger logger = LoggerFactory.getLogger(Scoreboard.class);
    //task could have been done only by using machesMap but by adding Treeset  we ensured that at any given moment we have sorted matches
    //more details in readme
    private final TreeSet<Match> matches;
    private final Map<Integer, Match> matchesMap;
    private final Lock lock;
    private int matchCounter;
    //this set has been done as an extra measure to prevent adding the same match 2 times
    private final Set<String> activeMatches; //

    public Scoreboard() {
        matches = new TreeSet<>(new MatchComparator());
        matchesMap = new HashMap<>();
        lock = new ReentrantLock();
        matchCounter = 0;
        activeMatches = new HashSet<>();
    }

    public int startMatch(String homeTeam, String awayTeam) {
        lock.lock();
        try {
            String matchDescription = homeTeam + "-" + awayTeam;
            if (activeMatches.contains(matchDescription)) {
                //attempting to add already existing match throw exception
                throw new IllegalArgumentException("Match " + homeTeam + " : " + awayTeam + " is already in progress");
            }
            matchCounter++;
            Match match = new Match(matchCounter, homeTeam, awayTeam);
            matches.add(match);
            matchesMap.put(matchCounter, match);

            activeMatches.add(matchDescription); // add to active matches
            logger.debug("Started match {}: {} vs {}, added to TreeSet", matchCounter, homeTeam, awayTeam);
            return matchCounter;
        } finally {
            lock.unlock();
        }
    }

    public void updateScore(int matchId, int homeGoals, int awayGoals) {
        lock.lock();
        try {
            Match match = matchesMap.get(matchId);
            if (match == null) {
                //attempting to update score of non existent match throw exception
                throw new IllegalArgumentException("There is no ongoing match with id " + matchId);
            }
            matches.remove(match);
            match.updateScore(homeGoals, awayGoals);
            matches.add(match);
            logger.debug("Updated match {}:  new score={}", matchId, match);
        } finally {
            lock.unlock();
        }
    }

    public void finishMatch(int matchId) {
        lock.lock();
        try {
            Match match = matchesMap.get(matchId);
            if (match == null) {
                //there is no ongoing match throw exception
                throw new IllegalArgumentException("There is no ongoing match with id " + matchId);
            }
            //remove match from hashset hashmap and treeset
            matches.remove(match);
            String matchDescription = match.getHomeTeam() + "-" + match.getAwayTeam();
            activeMatches.remove(matchDescription);
            matchesMap.remove(matchId);
            logger.debug("Finished match {}", matchId);
        } finally {
            lock.unlock();
        }
    }

    public List<String> getSummary() {
        lock.lock();
        try {
            List<String> summary = new ArrayList<>();
            int counter = 1;
            for (Match match : matches) {
                summary.add(String.format("%d. %s", counter++, match.toString()));
            }
            logger.debug("Summary size: {}, matches size: {}", summary.size(), matches.size());
            return summary;
        } finally {
            lock.unlock();
        }
    }
}