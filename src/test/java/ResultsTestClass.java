import org.example.Scoreboard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ResultsTestClass {
    private Scoreboard scoreboard;

    @BeforeEach
    void setUp() {
        scoreboard = new Scoreboard();
    }

    @Test
    void testStartMatch() {
        int matchId = scoreboard.startMatch("Mexico", "Canada");
        assertEquals(1, matchId, "First match ID should be 1");
        List<String> summary = scoreboard.getSummary();
        assertEquals(1, summary.size(), "Summary should contain one match");
        assertEquals("1. Mexico 0 - 0 Canada", summary.get(0), "Match should be initialized with 0-0 score");
    }

    @Test
    void testStartDuplicateMatch() {
        scoreboard.startMatch("Mexico", "Canada");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scoreboard.startMatch("Mexico", "Canada");
        }, "Should throw exception for duplicate match");
        assertEquals("Match Mexico : Canada is already in progress", exception.getMessage());
    }

    @Test
    void testFinishNonExistentMatch() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scoreboard.finishMatch(8);
        }, "Should throw exception for non-existent match");
        assertEquals("There is no ongoing match with id 8", exception.getMessage());
    }

    @Test
    void testSummaryOrder() {
        int match1 = scoreboard.startMatch("Mexico", "Canada");
        scoreboard.updateScore(match1, 0, 5);

        int match2 = scoreboard.startMatch("Spain", "Brazil");
        scoreboard.updateScore(match2, 10, 2);

        int match3 = scoreboard.startMatch("Germany", "France");
        scoreboard.updateScore(match3, 2, 2);

        int match4 = scoreboard.startMatch("Uruguay", "Italy");
        scoreboard.updateScore(match4, 6, 6);

        int match5 = scoreboard.startMatch("Argentina", "Australia");
        scoreboard.updateScore(match5, 3, 1);

        List<String> summary = scoreboard.getSummary();
        assertEquals(5, summary.size(), "Summary should contain all 5 matches");
        assertEquals("1. Uruguay 6 - 6 Italy", summary.get(0), "Highest score, most recent");
        assertEquals("2. Spain 10 - 2 Brazil", summary.get(1), "Highest score, earlier");
        assertEquals("3. Mexico 0 - 5 Canada", summary.get(2), "Next highest score");
        assertEquals("4. Argentina 3 - 1 Australia", summary.get(3), "Total 4, most recent");
        assertEquals("5. Germany 2 - 2 France", summary.get(4), "Total 4, earlier");
    }

    @Test
    void testUpdateNonExistentMatch() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scoreboard.updateScore(11, 1, 1);
        }, "Should throw exception for non-existent match");
        assertEquals("There is no ongoing match with id 11", exception.getMessage());
    }

    @Test
    void testCacheConsistency() {
        int matchId = scoreboard.startMatch("Mexico", "Canada");
        List<String> summary1 = scoreboard.getSummary();
        assertEquals("1. Mexico 0 - 0 Canada", summary1.get(0));

        // Update score and check if cache is invalidated
        scoreboard.updateScore(matchId, 2, 1);
        List<String> summary2 = scoreboard.getSummary();
        assertEquals("1. Mexico 2 - 1 Canada", summary2.get(0));
        assertNotSame(summary1, summary2, "Summary should be a new list after update");

        // Check if cache is reused when no changes occur
        List<String> summary3 = scoreboard.getSummary();
        assertEquals("1. Mexico 2 - 1 Canada", summary3.get(0));
        assertSame(summary2, summary3, "Should return the same unmodified copy");
    }

    @Test
    void testConcurrentSummaryAccess() throws InterruptedException {
        int matchId = scoreboard.startMatch("Mexico", "Canada");
        scoreboard.updateScore(matchId, 3, 2);

        // Ensure setup completes before concurrent tasks
        Thread.sleep(100); // Small delay to avoid race with setup

        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);
        Runnable summaryTask = () -> {
            try {
                List<String> summary = scoreboard.getSummary();
                assertEquals(1, summary.size(), "Summary should have one match");
                assertEquals("1. Mexico 3 - 2 Canada", summary.get(0), "Summary should show updated score starting at 1");
                latch.countDown();
            } catch (Throwable t) {
                t.printStackTrace(); // Log any unexpected errors
            }
        };

        for (int i = 0; i < 4; i++) {
            executor.submit(summaryTask);
        }

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(completed, "All threads should complete within timeout");
        assertEquals(0, latch.getCount(), "Latch should reach zero");
    }

    @Test
    void testConcurrentUpdateAndSummary() throws InterruptedException {
        int matchId = scoreboard.startMatch("Mexico", "Canada");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        executor.submit(() -> {
            try {
                startLatch.await();
                scoreboard.updateScore(matchId, 1, 1);
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                List<String> summary = scoreboard.getSummary();
                assertEquals(1, summary.size(), "Summary should have one match");
                assertTrue(summary.get(0).equals("1. Mexico 0 - 0 Canada") || summary.get(0).equals("1. Mexico 1 - 1 Canada"),
                        "Summary should reflect pre- or post-update state starting at 1");
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable t) {
                t.printStackTrace(); // Log any unexpected errors
            }
        });

        startLatch.countDown();
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(completed, "All threads should complete within timeout");
        assertEquals(0, latch.getCount(), "Latch should reach zero");

        List<String> summary = scoreboard.getSummary();
        assertEquals("1. Mexico 1 - 1 Canada", summary.get(0), "Final state should reflect update");
    }
}

