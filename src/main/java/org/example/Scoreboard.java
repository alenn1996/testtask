package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
//I created scorebord assuming that getSUmmary would be most called method, there wont be many matches and therefore there wont be
//many calls to updateMatch so getSUmmary would be more called then updateMatch.Since on Wc matches usually starts at the same time
//i added a simple multithreading as well.In case of more matches that start at the same time i would have probably went with
//hashmap of locks instead of single lock.
public class Scoreboard {
    private static final Logger logger = LoggerFactory.getLogger(Scoreboard.class);
    private final TreeSet<Match> matches; //needed since getSummary would be pretty often called. Therefore it is useful to have always sorted matches
    private final Map<Integer, Match> matchesMap; //actually the only structure ever needed in this task.We could have used it for sorting and
    //other operations however other structures ensured greater efficiency
    private final AtomicInteger matchCounter; // used to determine matchId
    private volatile List<String> cachedSummary; // Volatile for thread safety. Simple caching so in case of no results we can immediately
    //handle the result without iterating through treeset matches
    private final ReadWriteLock lock;

    private volatile boolean cacheFlag;

    public Scoreboard() {
        matches = new TreeSet<>(new MatchComparator());
        matchesMap = new HashMap<>();
        matchCounter = new AtomicInteger(0);
        cachedSummary = Collections.emptyList();
        lock = new ReentrantReadWriteLock();
        cacheFlag = false;
    }

    public int startMatch(String homeTeam, String awayTeam) {
        lock.writeLock().lock();
        try {
            if (matchesMap.values().stream().anyMatch(m ->
                    m.getHomeTeam().equals(homeTeam) && m.getAwayTeam().equals(awayTeam))) {
                logger.warn("Match: {} - {} already started", homeTeam, awayTeam);
                throw new IllegalArgumentException("Match " + homeTeam + " : " + awayTeam + " is already in progress");
            }
            int matchId = matchCounter.incrementAndGet();
            Match match = new Match(matchId, homeTeam, awayTeam);
            matches.add(match);
            matchesMap.put(matchId, match);
            cacheFlag = true;
            logger.debug("Started match {}: {} vs {}", matchId, homeTeam, awayTeam);
            return matchId;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void updateScore(int matchId, int homeGoals, int awayGoals) {
        lock.writeLock().lock();
        try {
            Match match = matchesMap.get(matchId);
            if (match == null) {
                logger.warn("Attempted to update non-existent match with id {}", matchId);
                throw new IllegalArgumentException("There is no ongoing match with id " + matchId);
            }
            matches.remove(match);
            match.updateScore(homeGoals, awayGoals);
            matches.add(match);
            cacheFlag = true;
            logger.debug("Score changed on match {}: new score={}", matchId, match);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void finishMatch(int matchId) {
        lock.writeLock().lock();
        try {
            Match match = matchesMap.remove(matchId);
            if (match != null) {
                matches.remove(match);
                cacheFlag = true;
                logger.debug("Finished match {}", matchId);
            } else {
                logger.warn("Attempted to finish non-existent match with id {}", matchId);
                throw new IllegalArgumentException("There is no ongoing match with id " + matchId);

            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<String> getSummary() {
        lock.readLock().lock();
        try {
            if (cacheFlag) {
                // Upgrade to write lock if we need to update the cache
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    if (cacheFlag) { // Double-check after acquiring write lock
                        updateCachedSummary();
                        cacheFlag = false;
                    }
                    lock.readLock().lock(); // Downgrade to read lock
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return cachedSummary;
        } finally {
            lock.readLock().unlock();
        }
    }
    private void updateCachedSummary() {
        List<String> summary = new ArrayList<>(matches.size());
        int counter = 1;
        for (Match match : matches) {
            summary.add(String.format("%d. %s", counter++, match.toString()));
        }
        cachedSummary = Collections.unmodifiableList(summary);
    }


}