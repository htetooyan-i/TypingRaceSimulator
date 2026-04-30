import java.awt.Color;
import java.util.ArrayList;

/**
 * Represents a typist participant in a typing race competition.
 * 
 * Each typist tracks their progress through a passage, accuracy rating, and burnout state.
 * Typists can customize their performance through typing styles (e.g. Touch Typist, Hunt & Peck),
 * Typists can also customize their performance through keyboard types (e.g. Mechanical, Touchscreen), and accessories (e.g. Wrist Support, Energy Drink).
 * 
 * A typist's progress can advance by typing characters correctly or slide backwards when mistypes occur.
 * When accuracy is too low, the typist may enter a burnout state where they cannot type temporarily.
 * Each typist is visually represented by a unique symbol and color during live race rendering.
 * 
 * Starter code generously abandoned by Ty Posaurus, your predecessor,
 * who typed with two fingers and considered that "good enough".
 * He left a sticky note: "the slide-back thing is optional probably".
 * It is not optional. Good luck.
 *
 * @author HTET OO YAN
 * @version 0.2
 */
public class Typist {
    // Fields of class Typist
    // Hint: you will need six fields. Think carefully about their types.
    // One of them tracks how far along the passage the typist has reached.
    // Another tracks whether the typist is currently burnt out.
    // A third tracks HOW MANY turns of burnout remain (not just whether they are
    // burnt out).
    // The remaining three should be fairly obvious.

    private double accuracy; // the typist's accuracy rating, between 0.0 and 1.0
    private char symbol; // a single Unicode character representing this typist (e.g. '①', '②', '③')
    private String name; // the name of the typist
    private int progress; // how far along the passage the typist has reached
    private boolean isBurntOut; // whether the typist is currently burnt out
    private int burnoutTurnsRemaining; // how many turns of burnout remain
    private boolean justMistyped; // whether the typist just mistyped on the current turn
    private int turnsTaken; // how many active turns this typist has taken for caffeine mode
    private int currentRaceBurnoutCount; // how many times the typist burnt out in the current race
    private long raceStartTime; // timestamp when the race started for this typist (in milliseconds)

    private Color color;
    private String typingStyle;
    private String keyboardType;
    private ArrayList<String> accessories = new ArrayList<>();

    // Constructor of class Typist
    /**
     * Constructor for objects of class Typist.
     * Creates a new typist with a given symbol, name, and accuracy rating.
     *
     * @param typistSymbol   a single Unicode character representing this typist
     *                       (e.g. '①', '②', '③')
     * @param typistName     the name of the typist (e.g. "TURBOFINGERS")
     * @param typistAccuracy the typist's accuracy rating, between 0.0 and 1.0
     */
    public Typist(char typistSymbol, String typistName, double typistAccuracy) {
        this.symbol = typistSymbol;
        this.name = typistName;
        this.progress = 0;
        this.isBurntOut = false;
        this.burnoutTurnsRemaining = 0;
        this.justMistyped = false;
        this.turnsTaken = 0;
        this.currentRaceBurnoutCount = 0;
        this.raceStartTime = 0;

        setAccuracy(typistAccuracy); // use the setter func to ensure acc is within bounds

    }

    public Typist() {
        this.symbol = '?';
        this.name = "Unnamed Typist";
        this.progress = 0;
        this.isBurntOut = false;
        this.burnoutTurnsRemaining = 0;
        this.justMistyped = false;
        this.accuracy = 0.5;
        this.typingStyle = null;
        this.keyboardType = null;
        this.accessories = new ArrayList<>();
        this.turnsTaken = 0;
        this.currentRaceBurnoutCount = 0;
        this.raceStartTime = 0;
    }

    // Methods of class Typist

    /**
     * Sets this typist into a burnout state for a given number of turns.
     * A burnt-out typist cannot type until their burnout has worn off.
     *
     * @param turns the number of turns the burnout will last
     */
    public void burnOut(int turns) {
        this.isBurntOut = true;
        // Apply any burnout modifiers f
        // rom accessories
        double burnoutModifier = 1.0;
        for (StatModifier m : getAccessories()) {
            burnoutModifier *= m.getBurnout();
        }

        int effectiveTurns = (int) Math.max(1, Math.round(turns * burnoutModifier));
        this.burnoutTurnsRemaining = effectiveTurns;
    }

    /**
     * Reduces the remaining burnout counter by one turn.
     * When the counter reaches zero, the typist recovers automatically.
     * Has no effect if the typist is not currently burnt out.
     */
    public void recoverFromBurnout() {
        if (this.isBurntOut) {
            this.burnoutTurnsRemaining -= 1;
            if (this.burnoutTurnsRemaining <= 0) {
                this.isBurntOut = false;
                this.burnoutTurnsRemaining = 0;
            }
        }
    }

    /**
     * Returns the typist's accuracy rating.
     *
     * @return accuracy as a double between 0.0 and 1.0
     */
    public double getAccuracy() {
        return this.accuracy;
    }

    /**
     * Returns the typist's current progress through the passage.
     * Progress is measured in characters typed correctly so far.
     * Note: this value can decrease if the typist mistypes.
     *
     * @return progress as a non-negative integer
     */
    public int getProgress() {
        return this.progress;
    }

    /**
     * Returns the name of the typist.
     *
     * @return the typist's name as a String
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the character symbol used to represent this typist.
     *
     * @return the typist's symbol as a char
     */
    public char getSymbol() {
        return this.symbol;
    }

    /**
     * Returns the number of turns of burnout remaining.
     * Returns 0 if the typist is not currently burnt out.
     *
     * @return burnout turns remaining as a non-negative integer
     */
    public int getBurnoutTurnsRemaining() {
        return this.burnoutTurnsRemaining;
    }

    /**
     * Resets the typist to their initial state, ready for a new race.
     * Progress returns to zero, burnout is cleared entirely.
     */
    public void resetToStart() {
        this.progress = 0;
        this.isBurntOut = false;
        this.burnoutTurnsRemaining = 0;
        this.justMistyped = false;
        this.turnsTaken = 0;
        this.currentRaceBurnoutCount = 0;
    }

    public void incrementTurnsTaken() {
        this.turnsTaken += 1;
    }

    public int getTurnsTaken() {
        return this.turnsTaken;
    }

    public int getCurrentRaceBurnoutCount() {
        return this.currentRaceBurnoutCount;
    }

    public void incrementCurrentRaceBurnoutCount() {
        this.currentRaceBurnoutCount++;
    }

    public void resetCurrentRaceBurnoutCount() {
        this.currentRaceBurnoutCount = 0;
    }

    public void setRaceStartTime(long startTime) {
        this.raceStartTime = startTime;
    }

    public double getCurrentRaceWPM() {
        if (raceStartTime == 0 || progress == 0) {
            return 0.0;
        }
        long elapsedMillis = System.currentTimeMillis() - raceStartTime;
        double elapsedMinutes = (double) elapsedMillis / (1000.0 * 60.0);
        if (elapsedMinutes <= 0) {
            return 0.0;
        }
        double wordsTyped = (double) progress / 5.0; // assuming average word length is 5 characters
        return wordsTyped / elapsedMinutes;
    }

    /**
     * Returns true if this typist is currently burnt out, false otherwise.
     *
     * @return true if burnt out
     */
    public boolean isBurntOut() {
        return this.isBurntOut;
    }

    /**
     * Advances the typist forward by one character along the passage.
     * Should only be called when the typist is not burnt out.
     */
    public void typeCharacter() {
        if (!isBurntOut) { // this func only be called when the typist is not burnt out but checking just
                           // in case
            progress++;
        }
    }

    /**
     * Moves the typist backwards by a given number of characters (a mistype).
     * Progress cannot go below zero — the typist cannot slide off the start.
     *
     * @param amount the number of characters to slide back (must be positive)
     */
    public void slideBack(int amount) {
        if (amount > 0) {
            progress -= amount;
            if (progress < 0) {
                progress = 0;
            }
        }
    }

    /**
     * Sets the accuracy rating of the typist.
     * Values below 0.0 should be set to 0.0; values above 1.0 should be set to 1.0.
     *
     * @param newAccuracy the new accuracy rating
     */
    public void setAccuracy(double newAccuracy) {
        if (newAccuracy < 0.0) {
            this.accuracy = 0.0;
        } else if (newAccuracy > 1.0) {
            this.accuracy = 1.0;
        } else {
            this.accuracy = newAccuracy;
        }
    }

    /**
     * Sets the symbol used to represent this typist.
     *
     * @param newSymbol the new symbol character
     */
    public void setSymbol(char newSymbol) {
        this.symbol = newSymbol;

    }

    /**
     * Sets whether the typist is mistyping on the current turn.
     *
     * @param newJustMistyped the new value for the justMistyped flag
     */
    public void setJustMistyped(boolean newJustMistyped) {
        this.justMistyped = newJustMistyped;
    }

    /**
     * Returns true if the typist just mistyped on the current turn, false
     * otherwise.
     * 
     * @return true if the typist just mistyped
     */
    public boolean getJustMistyped() {
        return this.justMistyped;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public void setTypingStyle(String typingStyle) {
        this.typingStyle = typingStyle;
    }

    public StatModifier getTypingStyle() {
        if ("Touch Typist".equals(this.typingStyle)) {
            return GameModifiers.TOUCH_TYPIST;
        } else if ("Hunt & Peck".equals(this.typingStyle)) {
            return GameModifiers.HUNT_AND_PECK;
        } else if ("Phone Thumbs".equals(this.typingStyle)) {
            return GameModifiers.PHONE_THUMBS;
        } else if ("Voice to Text".equals(this.typingStyle)) {
            return GameModifiers.VOICE_TO_TEXT;
        }

        return new StatModifier(1.0, 1.0, 1.0);
    }

    public String getTypingStyleName() {
        return this.typingStyle;
    }

    public void setKeyboardType(String keyboardType) {
        this.keyboardType = keyboardType;
    }

    public StatModifier getKeyboardType() {
        if ("Mechanical".equals(this.keyboardType)) {
            return GameModifiers.MECHANICAL;
        } else if ("Membrane".equals(this.keyboardType)) {
            return GameModifiers.MEMBRANE;
        } else if ("Touchscreen".equals(this.keyboardType)) {
            return GameModifiers.TOUCHSCREEN;
        } else if ("Stenography".equals(this.keyboardType)) {
            return GameModifiers.STENOGRAPHY;
        }
        return new StatModifier(1.0, 1.0, 1.0);
    }

    public String getKeyboardTypeName() {
        return this.keyboardType;
    }

    public void addAccessory(String accessory) {
        if (!accessories.contains(accessory)) {
            accessories.add(accessory);
        }
    }

    public void removeAccessory(String accessory) {
        accessories.remove(accessory);
    }

    public ArrayList<StatModifier> getAccessories() {
        ArrayList<StatModifier> modifiers = new ArrayList<>();
        for (String accessory : accessories) {
            switch (accessory) {
                case "Wrist Support":
                    modifiers.add(GameModifiers.WRIST_SUPPORT);
                    break;
                case "Noise-Cancelling Headphones":
                    modifiers.add(GameModifiers.NOISE_CANCELLING);
                    break;
                default:
                    break;
            }
        }
        return modifiers;
    }

    public ArrayList<String> getAccessoryNames() {
        return new ArrayList<>(this.accessories);
    }

    // Helper method to check if the typist has a specific accessory
    //
    public boolean hasAccessory(String accessory) {
        return accessories.contains(accessory);
    }

}
