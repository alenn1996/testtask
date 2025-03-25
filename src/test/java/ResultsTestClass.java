import org.example.Scoreboard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
            scoreboard.finishMatch(999);
        }, "Should throw exception for non-existent match");
        assertEquals("There is no ongoing match with id 999", exception.getMessage());
    }

    @Test
    void testSummaryOrder() {
        int match1 = scoreboard.startMatch("Mexico", "Canada");
        scoreboard.updateScore(match1, 0, 5);  // Total: 5

        int match2 = scoreboard.startMatch("Spain", "Brazil");
        scoreboard.updateScore(match2, 10, 2);  // Total: 12

        int match3 = scoreboard.startMatch("Germany", "France");
        scoreboard.updateScore(match3, 2, 2);  // Total: 4

        int match4 = scoreboard.startMatch("Uruguay", "Italy");
        scoreboard.updateScore(match4, 6, 6);  // Total: 12

        int match5 = scoreboard.startMatch("Argentina", "Australia");
        scoreboard.updateScore(match5, 3, 1);  // Total: 4

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
}
