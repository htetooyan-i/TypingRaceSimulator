import java.awt.Color;

/**
 * Stores the result of a single race for a typist.
 * Tracks WPM, accuracy, burnout count, position, and time taken.
 */
public class RaceResult {
    private String typistName;
    private double wordsPerMinute;
    private double accuracyPercentage;
    private int burnoutCount;
    private int position; // 1 for winner, 2 for second, 3 for third
    private long timeTakenMillis;
    private double accuracyBefore;
    private double accuracyAfter;
    private long timestamp; // When the race start
    private Color typistColor;

    public RaceResult(String typistName, double wpm, double accuracy, int burnoutCount,
            int position, long timeTakenMillis, double accuracyBefore, double accuracyAfter) {
        this(typistName, wpm, accuracy, burnoutCount, position, timeTakenMillis, accuracyBefore, accuracyAfter, null);
    }

    public RaceResult(String typistName, double wpm, double accuracy, int burnoutCount,
            int position, long timeTakenMillis, double accuracyBefore, double accuracyAfter, Color typistColor) {
        this.typistName = typistName;
        this.wordsPerMinute = wpm;
        this.accuracyPercentage = accuracy;
        this.burnoutCount = burnoutCount;
        this.position = position;
        this.timeTakenMillis = timeTakenMillis;
        this.accuracyBefore = accuracyBefore;
        this.accuracyAfter = accuracyAfter;
        this.typistColor = typistColor;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getTypistName() {
        return typistName;
    }

    public double getWordsPerMinute() {
        return wordsPerMinute;
    }

    public double getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public int getBurnoutCount() {
        return burnoutCount;
    }

    public int getPosition() {
        return position;
    }

    public long getTimeTakenMillis() {
        return timeTakenMillis;
    }

    public double getAccuracyBefore() {
        return accuracyBefore;
    }

    public double getAccuracyAfter() {
        return accuracyAfter;
    }

    public double getAccuracyChange() {
        return accuracyAfter - accuracyBefore;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Color getTypistColor() {
        return typistColor;
    }

    public String getFormattedTime() {
        long seconds = timeTakenMillis / 1000;
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    @Override
    public String toString() {
        return String.format("WPM: %.2f | Accuracy: %.2f%% | Burnouts: %d | Position: %d | Time: %s",
                wordsPerMinute, accuracyPercentage, burnoutCount, position, getFormattedTime());
    }
}
