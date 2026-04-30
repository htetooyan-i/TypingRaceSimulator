import java.util.*;

public class LeaderboardManager {

    private static Map<String, LeaderboardEntry> entries = new HashMap<>();

    public static synchronized void updateWithResult(RaceResult r) {
        String name = r.getTypistName();
        LeaderboardEntry e = entries.getOrDefault(name, new LeaderboardEntry(name));

        e.racesPlayed += 1;
        // Only count WPM for races where typist finished (position 1, 2, or 3)
        if (r.getPosition() <= 3) {
            e.totalWPM += r.getWordsPerMinute();
        }
        e.burnouts += r.getBurnoutCount();

        // Points algorithm: base by position + WPM bonus - burnout penalty
        int base = 0;
        if (r.getPosition() == 1)
            base = 3;
        else if (r.getPosition() == 2)
            base = 2;
        else if (r.getPosition() == 3)
            base = 1;

        int wpmBonus = (int) Math.floor(r.getWordsPerMinute() / 20.0); // 1 point per 20 WPM
        int burnoutPenalty = r.getBurnoutCount();

        int gained = Math.max(0, base + wpmBonus - burnoutPenalty);
        e.points += gained;

        // Wins and consecutive wins
        if (r.getPosition() == 1) {
            e.wins += 1;
            e.consecutiveWins += 1;
        } else {
            e.consecutiveWins = 0;
        }

        // Track races without burnout for titles
        if (r.getBurnoutCount() == 0) {
            e.racesWithoutBurnout += 1;
        } else {
            e.racesWithoutBurnout = 0;
        }

        // Titles & badges
        if (e.consecutiveWins >= 3) {
            e.titles.add("Speed Demon");
        }
        if (e.racesWithoutBurnout >= 5) {
            e.titles.add("Iron Fingers");
        }

        // Apply earnings for this race
        int earned = computeEarnings(r);
        if (earned > 0) {
            e.coins += earned;
        }

        entries.put(name, e);
    }

    // Earnings and upgrades support
    private static Map<String, Integer> upgradeCosts = new HashMap<>();

    private static void initializeUpgradeCosts() {
        if (upgradeCosts.isEmpty()) {
            upgradeCosts.put("Wrist Support", 50);
            upgradeCosts.put("Mechanical Keyboard", 200);
            upgradeCosts.put("Energy Drink", 30);
        }
    }

    public static synchronized int getUpgradeCost(String upgrade) {
        initializeUpgradeCosts();
        return upgradeCosts.getOrDefault(upgrade, -1);
    }

    public static synchronized boolean purchaseUpgrade(String typistName, String upgrade) {
        initializeUpgradeCosts();
        LeaderboardEntry e = entries.get(typistName);
        if (e == null)
            return false;
        Integer cost = upgradeCosts.get(upgrade);
        if (cost == null)
            return false;
        if (e.coins >= cost) {
            e.coins -= cost;
            e.upgrades.add(upgrade);
            return true;
        }
        return false;
    }

    public static synchronized void assignSponsor(String typistName, String sponsorName) {
        LeaderboardEntry e = entries.getOrDefault(typistName, new LeaderboardEntry(typistName));
        e.sponsor = sponsorName;
        entries.put(typistName, e);
    }

    public static synchronized int getCoins(String typistName) {
        LeaderboardEntry e = entries.get(typistName);
        return e == null ? 0 : e.coins;
    }

    public static synchronized Set<String> getUpgrades(String typistName) {
        LeaderboardEntry e = entries.get(typistName);
        return e == null ? new HashSet<>() : new HashSet<>(e.upgrades);
    }

    // Internal helper: compute earnings for a single race result
    private static int computeEarnings(RaceResult r) {
        int basePrize = 0;
        if (r.getPosition() == 1) {
            basePrize = 200;

        } else if (r.getPosition() == 2) {
            basePrize = 100;
        } else if (r.getPosition() == 3) {
            basePrize = 50;
        }

        int speedBonus = 0;
        if (r.getWordsPerMinute() > 60) {
            speedBonus = (int) ((r.getWordsPerMinute() - 60) / 10) * 20; // 20 coins per 10 WPM above 60
        }

        int burnoutPenalty = r.getBurnoutCount() * 25; // -25 coins per burnout

        int total = Math.max(0, basePrize + speedBonus - burnoutPenalty);

        // Sponsor deals
        String sponsor = null;
        try {
            LeaderboardEntry e = entries.get(r.getTypistName());
            if (e != null) {
                sponsor = e.sponsor;
            }
        } catch (Exception ex) {
            // ignore if no entry or sponsor
        }

        if ("KeyCorp".equals(sponsor)) {
            if (r.getBurnoutCount() == 0)
                total += 50;
        } else if ("SpeedyInc".equals(sponsor)) {
            if (r.getWordsPerMinute() >= 100)
                total += 100;
        }

        return total;
    }

    // Expose earnings when updating results
    public static synchronized void applyEarningsForResult(RaceResult r) {
        int earned = computeEarnings(r);
        LeaderboardEntry e = entries.getOrDefault(r.getTypistName(), new LeaderboardEntry(r.getTypistName()));
        e.coins += earned;
        entries.put(e.name, e);
    }

    public static synchronized List<LeaderboardEntry> getSortedEntries() {
        List<LeaderboardEntry> list = new ArrayList<>(entries.values());
        list.sort((a, b) -> Integer.compare(b.points, a.points));
        return list;
    }

    // Rank impact multiplier for starting accuracy. Champions face pressure.
    public static synchronized double getRankImpactFor(String name) {
        List<LeaderboardEntry> list = getSortedEntries();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).name.equals(name)) {
                int rank = i + 1;
                if (rank == 1)
                    return 0.95; // 5% pressure penalty
                if (rank <= 3)
                    return 0.98; // small penalty
                return 1.0;
            }
        }
        return 1.0;
    }

    public static synchronized void clear() {
        entries.clear();
    }
}
