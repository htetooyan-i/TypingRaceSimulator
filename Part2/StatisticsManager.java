import java.util.HashMap;
import javax.swing.SwingUtilities;

/**
 * Global statistics manager for the typing race simulator.
 * Maintains historical data for all typists and provides access to statistics.
 */
public class StatisticsManager {
    private static HashMap<String, TypistHistory> typistHistories = new HashMap<>();

    // Records a race result for a typist
    //
    public static void recordRaceResult(String typistName, RaceResult result) {
        if (!typistHistories.containsKey(typistName)) {
            typistHistories.put(typistName, new TypistHistory(typistName));
        }
        typistHistories.get(typistName).addRaceResult(result);
        // Inform leaderboard manager of new race result
        LeaderboardManager.updateWithResult(result);

        // Refresh any UI leaderboard on the EDT
        try {
            SwingUtilities.invokeLater(() -> {
                LeaderboardPanel.refreshTable();
            });
        } catch (Exception e) {
            // ignore if UI not present
        }
    }

    // Gets the history for a specific typist
    //
    public static TypistHistory getTypistHistory(String typistName) {
        return typistHistories.getOrDefault(typistName, new TypistHistory(typistName));
    }

    // Gets all typist histories
    //
    public static HashMap<String, TypistHistory> getAllHistories() {
        return typistHistories;
    }

    // Gets the typist with the best overall WPM
    //
    public static TypistHistory getTopPerformer() {
        TypistHistory topPerformer = null;
        double bestWPM = 0.0;

        for (TypistHistory history : typistHistories.values()) {
            if (history.getBestWPM() > bestWPM) {
                bestWPM = history.getBestWPM();
                topPerformer = history;
            }
        }
        return topPerformer;
    }

    // Gets all-time best WPM across all typists and races
    //
    public static double getAllTimebestWPM() {
        double best = 0.0;
        for (TypistHistory history : typistHistories.values()) {
            if (history.getBestWPM() > best) {
                best = history.getBestWPM();
            }
        }
        return best;
    }

    // Clears all statistics
    //
    public static void clearAllStatistics() {
        typistHistories.clear();
    }

    // Gets overall statistics as a string
    public static String getOverallStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== OVERALL STATISTICS ===\n\n");

        if (typistHistories.isEmpty()) {
            stats.append("No races completed yet.\n");
        } else {
            for (TypistHistory history : typistHistories.values()) {
                stats.append(history.toString()).append("\n");
            }
        }

        return stats.toString();
    }
}
