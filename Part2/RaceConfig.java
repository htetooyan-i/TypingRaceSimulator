import java.util.*;

public class RaceConfig {
    public String passageText;
    public String passageType; // Short/Medium/Long/Custom
    public int numberOfTypists;
    public List<Typist> typists = new ArrayList<>();
    public List<String> difficultyModifiers = new ArrayList<>();

    // helper method to check if a specific difficulty modifier
    public boolean hasDifficultyModifier(String modifier) {
        return difficultyModifiers.contains(modifier);
    }

    // getter method to get the list of difficulty modifiers
    public List<StatModifier> getDifficultyModifiers() {
        List<StatModifier> modifiers = new ArrayList<>();
        for (String mod : difficultyModifiers) {
            switch (mod) {
                case "Night Shift":
                    modifiers.add(GameModifiers.NIGHT_SHIFT);
                    break;
                default:
                    break;
            }
        }
        return modifiers;
    }
}
