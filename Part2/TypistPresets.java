import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Centralized default typist definitions.
 * By default these typists use black color (0x000000) and no accessories.
 */
public final class TypistPresets {

    public static final List<Typist> DEFAULTS = createDefaults();

    private static List<Typist> createDefaults() {
        List<Typist> list = new ArrayList<>();

        list.add(build("Turbo", '①', 0.35, Color.BLACK, "Touch Typist", "Mechanical"));
        list.add(build("Steady", '②', 0.35, Color.BLACK, "Hunt & Peck", "Membrane"));
        list.add(build("Flash", '③', 0.35, Color.BLACK, "Phone Thumbs", "Touchscreen"));
        list.add(build("Echo", '④', 0.35, Color.BLACK, "Voice to Text", "Mechanical"));
        list.add(build("Vector", '⑤', 0.35, Color.BLACK, "Touch Typist", "Stenography"));
        list.add(build("Nova", '⑥', 0.35, Color.BLACK, "Hunt & Peck", "Membrane"));

        return Collections.unmodifiableList(list);
    }

    private static Typist build(String name, char symbol, double accuracy, Color color, String typingStyle,
            String keyboardType) {
        Typist t = new Typist();
        t.setName(name);
        t.setSymbol(symbol);
        t.setAccuracy(accuracy);
        t.setColor(color);
        t.setTypingStyle(typingStyle);
        t.setKeyboardType(keyboardType);
        // No accessories by default
        return t;
    }

    public static Typist getForSeat(int seatNumber) {
        int idx = Math.max(0, Math.min(seatNumber - 1, DEFAULTS.size() - 1));
        return DEFAULTS.get(idx);
    }
}
