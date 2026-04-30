import java.util.ArrayList;

/**
 * Maintains the complete race history for a single typist.
 * Tracks all race results and personal bests.
 */
public class TypistHistory {
    private String typistName;
    private ArrayList<RaceResult> raceHistory;
    private double bestWPM;
    private int totalRaces;
    private double averageAccuracy;
    private int totalBurnouts;

    public TypistHistory(String typistName) {
        this.typistName = typistName;
        this.raceHistory = new ArrayList<>();
        this.bestWPM = 0.0;
        this.totalRaces = 0;
        this.averageAccuracy = 0.0;
        this.totalBurnouts = 0;
    }

    // Adds a race result to the history
    //
    public void addRaceResult(RaceResult result) {
        raceHistory.add(result);
        totalRaces++;

        // Update best WPM
        if (result.getWordsPerMinute() > bestWPM) {
            bestWPM = result.getWordsPerMinute();
        }

        // Update total burnouts
        totalBurnouts += result.getBurnoutCount();

        // Update average accuracy
        updateAverageAccuracy();
    }

    // Recalculates average accuracy from all races
    private void updateAverageAccuracy() {
        if (raceHistory.isEmpty()) {
            averageAccuracy = 0.0;
            return;
        }

        double sum = 0.0;
        for (RaceResult result : raceHistory) {
            sum += result.getAccuracyPercentage();
        }
        averageAccuracy = sum / raceHistory.size();
    }

    // Gets the number of races won
    //
    public int getWinsCount() {
        int wins = 0;
        for (RaceResult result : raceHistory) {
            if (result.getPosition() == 1) {
                wins++;
            }
        }
        return wins;
    }

    // Gets all races where the typist was in top 3
    //
    public ArrayList<RaceResult> getTopFinishes() {
        ArrayList<RaceResult> topFinishes = new ArrayList<>();
        for (RaceResult result : raceHistory) {
            if (result.getPosition() <= 3) {
                topFinishes.add(result);
            }
        }
        return topFinishes;
    }

    // Getters
    public String getTypistName() {
        return typistName;
    }

    public ArrayList<RaceResult> getRaceHistory() {
        return raceHistory;
    }

    public double getBestWPM() {
        return bestWPM;
    }

    public int getTotalRaces() {
        return totalRaces;
    }

    public double getAverageAccuracy() {
        return averageAccuracy;
    }

    public int getTotalBurnouts() {
        return totalBurnouts;
    }

    public RaceResult getMostRecentRace() {
        if (raceHistory.isEmpty()) {
            return null;
        }
        return raceHistory.get(raceHistory.size() - 1);
    }

    @Override
    public String toString() {
        return String.format("Typist: %s | Races: %d | Best WPM: %.2f | Avg Accuracy: %.2f%% | Wins: %d",
                typistName, totalRaces, bestWPM, averageAccuracy, getWinsCount());
    }
}
