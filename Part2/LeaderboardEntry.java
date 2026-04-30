import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single typist's leaderboard entry.
 * Tracks points, earnings, wins, races, burnouts, titles, upgrades, and
 * sponsors.
 */
public class LeaderboardEntry {
    public String name;
    public int points;
    public int wins;
    public int racesPlayed;
    public int burnouts;
    public double totalWPM;
    public int coins;
    public int consecutiveWins;
    public int racesWithoutBurnout;
    public Set<String> titles = new HashSet<>();
    public Set<String> upgrades = new HashSet<>();
    public String sponsor = null;

    public LeaderboardEntry(String name) {
        this.name = name;
        this.points = 0;
        this.wins = 0;
        this.racesPlayed = 0;
        this.burnouts = 0;
        this.totalWPM = 0.0;
        this.coins = 0;
        this.consecutiveWins = 0;
        this.racesWithoutBurnout = 0;
    }

    public double getAvgWPM() {
        return racesPlayed > 0 ? totalWPM / racesPlayed : 0.0;
    }
}
