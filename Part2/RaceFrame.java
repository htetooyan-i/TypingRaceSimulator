import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class RaceFrame {
    // Store status labels to update during race
    private static ArrayList<JLabel> statusLabels = new ArrayList<>();

    public static ArrayList<JTextPane> showRaceFrame(RaceConfig cfg) {

        JFrame raceFrame = new JFrame("Typing Race");
        raceFrame.setLayout(new BorderLayout());
        raceFrame.setBounds(200, 200, 800, 600);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        int numTypists = cfg.numberOfTypists;

        ArrayList<JTextPane> panes = new ArrayList<>();

        for (int i = 0; i < numTypists; i++) {

            String name = (cfg.typists.size() > i) ? cfg.typists.get(i).getName() : "Unknown";
            String symbol = (cfg.typists.size() > i) ? String.valueOf(cfg.typists.get(i).getSymbol()) : "?";
            String typingStyle = (cfg.typists.size() > i && cfg.typists.get(i).getTypingStyleName() != null)
                    ? cfg.typists.get(i).getTypingStyleName()
                    : "Unknown";
            String keyboardType = (cfg.typists.size() > i && cfg.typists.get(i).getKeyboardTypeName() != null)
                    ? cfg.typists.get(i).getKeyboardTypeName()
                    : "Unknown";
            String accessories = "None";
            if (cfg.typists.size() > i) {
                ArrayList<String> activeAccessories = cfg.typists.get(i).getAccessoryNames();
                if (!activeAccessories.isEmpty()) {
                    accessories = String.join(", ", activeAccessories);
                }
            }

            JPanel typistPanel = new JPanel();
            typistPanel.setLayout(new BoxLayout(typistPanel, BoxLayout.Y_AXIS));

            JLabel typistLabel = new JLabel(symbol + " Typist " + (i + 1) + " - " + name);
            typistLabel.setFont(new Font("Arial", Font.BOLD, 12));
            typistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            typistPanel.add(typistLabel);

            JLabel detailLabel = new JLabel("<html><span style='font-size:10px;'>"
                    + "Style: " + typingStyle + " | "
                    + "Keyboard: " + keyboardType + " | "
                    + "Accessories: " + accessories
                    + "</span></html>");
            detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            detailLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));
            typistPanel.add(detailLabel);

            // Add status label to show burnout and mistype
            JLabel statusLabel = new JLabel(" ");
            statusLabel.setFont(new Font("Arial", Font.BOLD, 11));
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            statusLabels.add(statusLabel);

            typistPanel.add(statusLabel);

            content.add(typistPanel);

            JTextPane textPane = new JTextPane();
            textPane.setEditable(false);

            // Add pane to list
            panes.add(textPane);

            // Initialize with empty progress
            updateTextPane(textPane, cfg.passageText, 0);

            JScrollPane scrollPane = new JScrollPane(textPane);
            scrollPane.setPreferredSize(new Dimension(700, 100));

            content.add(scrollPane);
        }

        JScrollPane mainScroll = new JScrollPane(content);
        raceFrame.add(mainScroll, BorderLayout.CENTER);

        // FOOTER
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel statusLabel = new JLabel("Active Race Difficulty: "
                + (cfg.difficultyModifiers.isEmpty() ? "None" : String.join(" | ", cfg.difficultyModifiers)));

        footer.add(statusLabel, BorderLayout.CENTER);
        raceFrame.add(footer, BorderLayout.SOUTH);

        raceFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        raceFrame.setVisible(true);

        return panes;
    }

    public static void updateTextPane(JTextPane pane, String text, int progress) {
        StyledDocument doc = pane.getStyledDocument();

        try {
            // Clear old content
            doc.remove(0, doc.getLength());

            // Default style (normal text)
            Style defaultStyle = pane.getStyle("default");
            if (defaultStyle == null) {
                defaultStyle = pane.addStyle("default", null);
                StyleConstants.setForeground(defaultStyle, Color.BLACK);
            }

            // Typed text
            Style typedStyle = pane.getStyle("typed");
            if (typedStyle == null) {
                typedStyle = pane.addStyle("typed", null);
                StyleConstants.setForeground(typedStyle, Color.GREEN.darker());
            }

            // Cursor style
            Style cursorStyle = pane.getStyle("cursor");
            if (cursorStyle == null) {
                cursorStyle = pane.addStyle("cursor", null);
                StyleConstants.setBackground(cursorStyle, Color.BLACK);
                StyleConstants.setForeground(cursorStyle, Color.WHITE);
            }

            // Insert text with styles
            for (int i = 0; i < text.length(); i++) {
                Style style;

                if (i < progress) {
                    style = typedStyle;
                } else if (i == progress) {
                    style = cursorStyle;
                } else {
                    style = defaultStyle;
                }

                doc.insertString(doc.getLength(),
                        String.valueOf(text.charAt(i)),
                        style);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the typist display with current progress and status indicators
     */
    public static void updateTypistDisplay(JTextPane pane, String text, Typist typist, int typistIndex) {
        // Update text pane with progress
        updateTextPane(pane, text, typist.getProgress());

        // Update status label with burnout and mistype info
        if (typistIndex < statusLabels.size()) {
            JLabel statusLabel = statusLabels.get(typistIndex);
            String status = "";

            if (typist.isBurntOut()) {
                status = "BURNT OUT (" + typist.getBurnoutTurnsRemaining() + " turns remaining)";
                statusLabel.setForeground(Color.RED);
            } else if (typist.getJustMistyped()) {
                status = "MISTYPED (sliding back)";
                statusLabel.setForeground(Color.RED);
            }
            statusLabel.setText(status);
        }
    }

}
