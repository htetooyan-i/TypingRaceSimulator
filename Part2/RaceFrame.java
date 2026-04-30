import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class RaceFrame {
    // Store status labels to update during race
    private static ArrayList<JLabel> statusLabels = new ArrayList<>();
    private static HashMap<String, Integer> lastProgressByTypist = new HashMap<>();

    public static ArrayList<JTextPane> showRaceFrame(RaceConfig cfg) {
        statusLabels.clear();
        lastProgressByTypist.clear();

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

            JLabel typistLabel = new JLabel(symbol + " - " + name);
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

            if (cfg.typists.size() > i) {
                lastProgressByTypist.put(cfg.typists.get(i).getName(), 0);
            }

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

    // Updates the typist display with current progress and status indicators
    //
    public static void updateTypistDisplay(JTextPane pane, String text, Typist typist, int typistIndex) {
        // Update text pane with progress
        updateTextPane(pane, text, typist.getProgress());

        // Update status label with burnout and mistype info
        if (typistIndex < statusLabels.size()) {
            JLabel statusLabel = statusLabels.get(typistIndex);
            String status = "";
            int currentProgress = typist.getProgress();
            int previousProgress = lastProgressByTypist.getOrDefault(typist.getName(), currentProgress);

            if (typist.isBurntOut()) {
                status = "BURNT OUT (" + typist.getBurnoutTurnsRemaining() + " turns remaining)";
                statusLabel.setForeground(Color.RED);
            } else if (typist.getJustMistyped() || currentProgress < previousProgress) {
                status = "MISTYPED (sliding back)";
                statusLabel.setForeground(Color.RED);
            }
            statusLabel.setText(status);
            lastProgressByTypist.put(typist.getName(), currentProgress);
        }
    }

    // Displays the winner dialog with Statistics button
    //
    public static void showWinnerDialog(Typist winner, String formattedFinalAcc, String formattedOldAcc,
            RaceConfig cfg) {
        // Create custom dialog with Statistics button
        JDialog dialog = new JDialog();
        dialog.setTitle("Race Results");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        // Main panel with winner info
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("RACE WINNER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Name: " + winner.getName());
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel accuracyLabel = new JLabel("Final Accuracy: " + formattedFinalAcc + "%");
        accuracyLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        accuracyLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel improvedLabel = new JLabel("(improved from " + formattedOldAcc + "%)");
        improvedLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        improvedLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(nameLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(accuracyLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(improvedLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton statsButton = new JButton("Statistics");
        statsButton.addActionListener(e -> showPlayerStatistics(cfg));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(statsButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // Displays statistics for all players in a new frame
    //
    public static void showPlayerStatistics(RaceConfig cfg) {
        JFrame statsFrame = new JFrame("Player Statistics");
        statsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        statsFrame.setSize(800, 600);
        statsFrame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Current Race Statistics
        String[] currentColumnNames = { "Name", "Symbol", "Accuracy", "Progress", "WPM", "Turns", "Burnt Out",
                "Burnout Count", "Style", "Keyboard", "Accessories" };
        Object[][] currentData = new Object[cfg.typists.size()][currentColumnNames.length];

        for (int i = 0; i < cfg.typists.size(); i++) {
            Typist t = cfg.typists.get(i);
            String progress = t.getProgress() + " / " + cfg.passageText.length();
            String accessories = t.getAccessoryNames().isEmpty() ? "None" : String.join(", ", t.getAccessoryNames());

            currentData[i][0] = t.getName();
            currentData[i][1] = t.getSymbol();
            currentData[i][2] = String.format("%.2f", t.getAccuracy() * 100) + "%";
            currentData[i][3] = progress;
            currentData[i][4] = String.format("%.2f", t.getCurrentRaceWPM());
            currentData[i][5] = t.getTurnsTaken();
            currentData[i][6] = t.isBurntOut() ? "Yes" : "No";
            currentData[i][7] = t.getCurrentRaceBurnoutCount();
            currentData[i][8] = t.getTypingStyleName();
            currentData[i][9] = t.getKeyboardTypeName();
            currentData[i][10] = accessories;
        }

        JTable currentTable = new JTable(currentData, currentColumnNames);
        currentTable.setEnabled(false);
        currentTable.getTableHeader().setReorderingAllowed(false);
        currentTable.setRowHeight(25);
        currentTable.setFont(new Font("Arial", Font.PLAIN, 11));
        currentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane currentRaceScroll = new JScrollPane(currentTable);
        tabbedPane.addTab("Current Race", currentRaceScroll);

        // Personal bests
        String[] bestColumnNames = { "Name", "Total Races", "Wins", "Best WPM", "Avg Accuracy", "Total Burnouts" };
        Object[][] bestData = new Object[cfg.typists.size()][bestColumnNames.length];

        for (int i = 0; i < cfg.typists.size(); i++) {
            Typist t = cfg.typists.get(i);
            TypistHistory history = StatisticsManager.getTypistHistory(t.getName());

            bestData[i][0] = t.getName();
            bestData[i][1] = history.getTotalRaces();
            bestData[i][2] = history.getWinsCount();
            bestData[i][3] = String.format("%.2f", history.getBestWPM());
            bestData[i][4] = String.format("%.2f", history.getAverageAccuracy()) + "%";
            bestData[i][5] = history.getTotalBurnouts();
        }

        JTable bestTable = new JTable(bestData, bestColumnNames);
        bestTable.setEnabled(false);
        bestTable.getTableHeader().setReorderingAllowed(false);
        bestTable.setRowHeight(25);
        bestTable.setFont(new Font("Arial", Font.PLAIN, 11));
        bestTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane bestScroll = new JScrollPane(bestTable);
        tabbedPane.addTab("Personal Bests", bestScroll);

        // History as chart
        tabbedPane.addTab("History", new JScrollPane(new HistoryChartPanel(cfg)));

        // Comparison View (Table Format)
        String[] comparisonColumnNames = { "Name", "Best WPM", "Total Races", "Wins", "Win Rate", "Avg Accuracy" };
        Object[][] comparisonData = new Object[cfg.typists.size()][comparisonColumnNames.length];

        for (int i = 0; i < cfg.typists.size(); i++) {
            Typist t = cfg.typists.get(i);
            TypistHistory history = StatisticsManager.getTypistHistory(t.getName());
            double winRate = history.getTotalRaces() > 0
                    ? (double) history.getWinsCount() / history.getTotalRaces() * 100
                    : 0;

            comparisonData[i][0] = t.getName();
            comparisonData[i][1] = String.format("%.2f", history.getBestWPM());
            comparisonData[i][2] = history.getTotalRaces();
            comparisonData[i][3] = history.getWinsCount();
            comparisonData[i][4] = String.format("%.1f", winRate) + "%";
            comparisonData[i][5] = String.format("%.2f", history.getAverageAccuracy()) + "%";
        }

        JTable comparisonTable = new JTable(comparisonData, comparisonColumnNames);
        comparisonTable.setEnabled(false);
        comparisonTable.getTableHeader().setReorderingAllowed(false);
        comparisonTable.setRowHeight(25);
        comparisonTable.setFont(new Font("Arial", Font.PLAIN, 11));
        comparisonTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane comparisonScroll = new JScrollPane(comparisonTable);
        tabbedPane.addTab("Comparison", comparisonScroll);

        statsFrame.add(tabbedPane);
        statsFrame.setVisible(true);
    }

}
