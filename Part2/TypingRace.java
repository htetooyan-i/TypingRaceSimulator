import java.util.concurrent.TimeUnit;
import java.util.*;
import java.lang.Math;
import java.awt.Color;
import javax.swing.*;

/**
 * A typing race simulation. Three typists race to complete a passage of text,
 * advancing character by character — or sliding backwards when they mistype.
 *
 * Originally written by Ty Posaurus, who left this project to "focus on his
 * two-finger technique". He assured us the code was "basically done".
 * We have found evidence to the contrary.
 *
 * @author TyPosaurus
 * @version 0.7 (the other 0.3 is left as an exercise for the reader)
 */
public class TypingRace {
    private int passageLength; // Total characters in the passage to type

    // Accuracy thresholds for mistype and burnout events
    // (Ty tuned these values "by feel". They may need adjustment.)
    private static final double MISTYPE_BASE_CHANCE = 0.3;
    private static final int SLIDE_BACK_AMOUNT = 2;
    private static final int BURNOUT_DURATION = 3;

    // Bonus accuracy awarded to the winner
    private static final double WIN_ACCURACY_BONUS = 0.02;

    // Track race timing and burnout counts for each typist
    private HashMap<String, Long> raceStartTimes;
    private HashMap<String, Integer> burnoutCounts;
    private HashMap<String, Double> accuraciesBeforeRace;

    /**
     * Constructor for objects of class TypingRace.
     * Sets up the race with a passage of the given length.
     * Initially there are no typists seated.
     *
     * @param passageLength the number of characters in the passage to type
     */
    public TypingRace(int passageLength) {
        this.passageLength = passageLength;
        this.raceStartTimes = new HashMap<>();
        this.burnoutCounts = new HashMap<>();
        this.accuraciesBeforeRace = new HashMap<>();
    }

    /**
     * Starts the typing race with live UI updates.
     * Similar to startRace, but updates the provided text panes during execution.
     *
     * @param cfg   the race configuration with typists and passage
     * @param panes list of JTextPane objects to update (one per typist)
     */
    public void startRaceWithUpdates(RaceConfig cfg, ArrayList<JTextPane> panes) {
        boolean finished = false;

        try {
            for (Typist t : cfg.typists) {
                t.resetToStart();
                accuraciesBeforeRace.put(t.getName(), t.getAccuracy());
                // Rank impact
                // Champions and typists with high rank face more pressure, which can reduce their accuracy slightly
                try {
                    double rankMult = LeaderboardManager.getRankImpactFor(t.getName());
                    t.setAccuracy(t.getAccuracy() * rankMult);
                } catch (Exception ex) {
                    // ignore if leaderboard not initialized
                }

                // Apply purchased upgrades
                try {
                    Set<String> ups = LeaderboardManager.getUpgrades(t.getName());
                    if (ups.contains("Wrist Support")) {
                        if (!t.hasAccessory("Wrist Support"))
                            t.addAccessory("Wrist Support");
                    }
                    if (ups.contains("Mechanical Keyboard")) {
                        t.setKeyboardType("Mechanical");
                    }
                    if (ups.contains("Energy Drink")) {
                        if (!t.hasAccessory("Energy Drink"))
                            t.addAccessory("Energy Drink");
                    }
                } catch (Exception ex) {
                    // ignore
                }
                // Initialize tracking for this race
                long raceStartTime = System.currentTimeMillis();
                raceStartTimes.put(t.getName(), raceStartTime);
                t.setRaceStartTime(raceStartTime);
                burnoutCounts.put(t.getName(), 0);
            }
        } catch (NullPointerException e) {
            System.out.println("All seats must be filled to start the race!");
            return;
        }

        while (!finished) {
            // Advance each typist by one turn
            for (Typist t : cfg.typists) {
                advanceTypist(cfg, t);
            }

            // Update UI in real-time
            for (int i = 0; i < cfg.typists.size() && i < panes.size(); i++) {
                Typist t = cfg.typists.get(i);
                JTextPane pane = panes.get(i);
                RaceFrame.updateTypistDisplay(pane, cfg.passageText, t, i);
            }

            // Check if any typist has finished the passage
            for (Typist t : cfg.typists) {
                if (raceFinishedBy(t)) {
                    finished = true;
                    break;
                }
            }

            // Wait 200ms between turns so the animation is visible
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {
            }
        }

        // Determine finishing order and record results
        ArrayList<Typist> finishOrder = new ArrayList<>();
        for (Typist t : cfg.typists) {
            if (raceFinishedBy(t)) {
                finishOrder.add(t);
            }
        }

        final Typist finalWinner = finishOrder.isEmpty() ? null : finishOrder.get(0);

        if (finalWinner != null) {
            double oldAcc = finalWinner.getAccuracy();
            finalWinner.setAccuracy(oldAcc + WIN_ACCURACY_BONUS);

            String formattedFinalAcc = String.format("%.2f", finalWinner.getAccuracy());
            String formattedOldAcc = String.format("%.2f", oldAcc);

            // Record race results for all typists
            recordRaceResults(new ArrayList<>(cfg.typists), finishOrder);

            RaceFrame.showWinnerDialog(finalWinner, formattedFinalAcc, formattedOldAcc, cfg);
        }
    }

    /**
     * Simulates one turn for a typist.
     *
     * If the typist is burnt out, they recover one turn's worth and skip typing.
     * Otherwise:
     * - They may type a character (advancing progress) based on their accuracy.
     * - They may mistype (sliding back) — the chance of a mistype should decrease
     * for more accurate typists.
     * - They may burn out — more likely for very high-accuracy typists
     * who are pushing themselves too hard.
     *
     * @param theTypist the typist to advance
     */
    private void advanceTypist(RaceConfig cfg, Typist theTypist) {
        if (theTypist.isBurntOut()) {
            // Recovering from burnout — skip this turn
            theTypist.recoverFromBurnout();
            return;
        }
        StatModifier typingStyle = theTypist.getTypingStyle();
        StatModifier keyboardType = theTypist.getKeyboardType();

        // Count this as an active turn for the typist for caffeine mode
        theTypist.incrementTurnsTaken();

        double updatedAccuracy = theTypist.getAccuracy() * typingStyle.getAccuracy() * keyboardType.getAccuracy();

        if (cfg.hasDifficultyModifier("Night Shift")) {
            updatedAccuracy *= GameModifiers.NIGHT_SHIFT.getAccuracy();
        }

        // if the typist use energy drink, they boost for the first half and then drop
        // for the second half
        if (theTypist.hasAccessory("Energy Drink")) {
            if (theTypist.getProgress() < passageLength / 2) {
                updatedAccuracy *= 1.1;
            } else {
                updatedAccuracy *= 0.9;
            }
        }

        updatedAccuracy = Math.min(1.0, updatedAccuracy);

        // Attempt to type a character
        // a single successful attempt.
        if (Math.random() < updatedAccuracy) {

            double speedFactor = typingStyle.getSpeed() * keyboardType.getSpeed();

            // caffine mode is active spped is boosted for the first 10 turns, but then
            // drops off as the crash hits
            if (cfg.hasDifficultyModifier("Caffeine Mode") && theTypist.getTurnsTaken() <= 10) {
                speedFactor *= 1.2;
            }

            int baseChars = (int) Math.floor(speedFactor);
            double chanceToBoost = speedFactor - baseChars;
            int charsToType = Math.max(1, baseChars);

            if (Math.random() < chanceToBoost) {
                charsToType += 1;
            }
            for (int i = 0; i < charsToType; i++) {
                theTypist.typeCharacter();
            }
            theTypist.setJustMistyped(false);
            return;
        }

        // Mistype check — the probability should reflect the typist's accuracy
        // Lower chance for more accurate typists
        // can reduce the mistype probability
        double accessoriesModifier = 1.0;
        if (theTypist.hasAccessory("Noise-Cancelling Headphones")) {
            accessoriesModifier *= 0.7; // reduce mistype chance by 30%
        }

        if (Math.random() < (1 - updatedAccuracy) * MISTYPE_BASE_CHANCE * accessoriesModifier) {
            if (cfg.hasDifficultyModifier("Autocorrect")) { // if autocorrect is on, mistypes are reduced by 50%
                theTypist.slideBack((int) Math.ceil(SLIDE_BACK_AMOUNT * 0.5));
            } else {
                theTypist.slideBack(SLIDE_BACK_AMOUNT);
            }

            theTypist.setJustMistyped(true);
            return;
        }

        // Burnout check — pushing too hard increases burnout risk
        // (probability scales with accuracy squared, capped at ~0.05)
        // Burnout probability (scaled by typing style's burnout stat).
        double burnoutProb = 0.05 * updatedAccuracy * updatedAccuracy * typingStyle.getBurnout();

        // if caffeine mode is active, burnout chance increases after 10 turns
        if (cfg.hasDifficultyModifier("Caffeine Mode") && theTypist.getTurnsTaken() > 10) {
            burnoutProb *= 1.3;
        }

        if (Math.random() < burnoutProb) {
            theTypist.burnOut(BURNOUT_DURATION);
            // Track burnout count
            theTypist.incrementCurrentRaceBurnoutCount();
            String typistName = theTypist.getName();
            burnoutCounts.put(typistName, burnoutCounts.getOrDefault(typistName, 0) + 1);
            theTypist.setJustMistyped(false);
            return;
        }
    }

    /**
     * Returns true if the given typist has completed the full passage.
     *
     * @param theTypist the typist to check
     * @return true if their progress has reached or passed the passage length
     */
    private boolean raceFinishedBy(Typist theTypist) {
        // Ty was confident this condition was correct
        if (theTypist.getProgress() >= passageLength) {
            return true;
        } else {
            return false;
        }
    }

    // Records the race results for all typists into the StatisticsManager
    //
    private void recordRaceResults(ArrayList<Typist> allTypists, ArrayList<Typist> finishOrder) {
        // Calculate metrics for each typist
        for (int position = 0; position < allTypists.size(); position++) {
            Typist typist = allTypists.get(position);
            String typistName = typist.getName();

            // Calculate time taken in ms
            long timeTaken = System.currentTimeMillis()
                    - raceStartTimes.getOrDefault(typistName, System.currentTimeMillis());

            // Calculate WPM and assuming average word length is 5 characters
            double wordsTyped = (double) typist.getProgress() / 5.0;
            double minutesTaken = (double) timeTaken / (1000.0 * 60.0);
            double wpm = minutesTaken > 0 ? wordsTyped / minutesTaken : 0;

            // Calculate accuracy percentage (progress without mistypes / total attempts)
            // For now, use the typist's current accuracy * 100
            double accuracyPercentage = typist.getAccuracy() * 100.0;

            // Get burnout count
            int burnoutCount = burnoutCounts.getOrDefault(typistName, 0);

            // Determine position (1-indexed)
            int finishPosition = finishOrder.contains(typist) ? finishOrder.indexOf(typist) + 1 : position + 1;

            // Get accuracy before race
            double accuracyBefore = accuraciesBeforeRace.getOrDefault(typistName, 0.0) * 100.0;
            double accuracyAfter = typist.getAccuracy() * 100.0;

            // Create and record race result
            Color typistColor = typist.getColor();
            RaceResult result = new RaceResult(typistName, wpm, accuracyPercentage, burnoutCount,
                    finishPosition, timeTaken, accuracyBefore, accuracyAfter, typistColor);

            StatisticsManager.recordRaceResult(typistName, result);
        }
    }

}
