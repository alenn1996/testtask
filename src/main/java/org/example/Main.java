package org.example;

import java.util.List;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        Scoreboard scoreboard = new Scoreboard();

        // Start and update matches in the specified order
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

        // Print the summary
        System.out.println("Current World Cup Scoreboard:");
        List<String> summary = scoreboard.getSummary();
        for (String match : summary) {
            System.out.println(match);
        }
    }
}
