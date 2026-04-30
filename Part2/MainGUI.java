import java.awt.*;
import java.util.*;
import javax.swing.*;

public class MainGUI {

    // UI refs to collect config
    private static JTextArea passageTextArea;
    private static JRadioButton shortBtn, mediumBtn, longBtn, customBtn;
    private static JSpinner typistSpinner;
    private static JPanel typistEntriesPanel;
    private static JCheckBox autocorrectBox, caffeineBox, nightShiftBox;
    private static final Map<Integer, TypistFields> typistFieldsMap = new HashMap<>();

    private static final String SHORT_PASSAGE = "The quick brown fox jumps over the lazy dog.";
    private static final String MEDIUM_PASSAGE = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    private static final String LONG_PASSAGE = "In a village of La Mancha, the name of which I have no desire to call to mind, there lived not long since one of those gentlemen that keep a lance in the lancerack, an old buckler, a lean hack, and a greyhound for coursing. An olla of rather more beef than mutton, a salad on most nights, scraps on Saturdays, lentils on Fridays, and a pigeon or so extra on Sundays, made away with three-quarters of his income.";

    public static void main(String[] args) {

        JFrame frame = new JFrame("Typing Race Game");
        frame.setLayout(new BorderLayout());

        JPanel content = raceConfiguration();

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1000);
        frame.setVisible(true);
    }

    public static JPanel raceConfiguration() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Passage length selection
        panel.add(passageLengthConfiguration());
        panel.add(Box.createVerticalStrut(12));

        // Player configuration
        panel.add(playerConfiguration());
        panel.add(Box.createVerticalStrut(12));

        // Difficulty modifiers
        panel.add(difficultyConfiguration());
        panel.add(Box.createVerticalStrut(12));

        // Start race button
        JButton startButton = new JButton("Start Race");
        startButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        startButton.addActionListener(e -> {
            String validationError = validateConfig();
            if (validationError != null) {
                JOptionPane.showMessageDialog(null, validationError, "Configuration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            RaceConfig cfg = getCurrentConfig();

            // Show the race frame FIRST
            java.util.ArrayList<JTextPane> panes = RaceFrame.showRaceFrame(cfg);

            // Run the race on a background thread for live updates
            new Thread(() -> {
                TypingRace race = new TypingRace(cfg.passageText.length());
                race.startRaceWithUpdates(cfg, panes);
            }).start();
        });

        panel.add(startButton);

        return panel;
    }

    // Player configuration panel where users can enter typist details and select modifiers
    //
    public static JPanel playerConfiguration() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel typistPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("Typists:");
        typistSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 6, 1));
        typistPanel.add(label);
        typistPanel.add(typistSpinner);

        panel.add(typistPanel);
        panel.add(Box.createVerticalStrut(5));

        // Info panel about modifiers
        panel.add(showModifierInfo());
        panel.add(Box.createVerticalStrut(10));

        typistEntriesPanel = new JPanel();
        typistEntriesPanel.setLayout(new BoxLayout(typistEntriesPanel, BoxLayout.Y_AXIS));
        panel.add(typistEntriesPanel);

        int numberOfTypists = (int) typistSpinner.getValue();

        for (int i = 1; i <= numberOfTypists; i++) {
            typistEntriesPanel.add(typistEntry(i));
        }

        // Update typist entries when the spinner value changes
        typistSpinner.addChangeListener(e -> {
            int n = (int) typistSpinner.getValue();
            typistEntriesPanel.removeAll();
            typistFieldsMap.clear();
            for (int i = 1; i <= n; i++) {
                typistEntriesPanel.add(typistEntry(i));
            }
            typistEntriesPanel.revalidate();
            typistEntriesPanel.repaint();
        });

        return panel;
    }

    // Panel for selecting passage length and entering custom passage
    //
    public static JPanel passageLengthConfiguration() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(passageSelection());

        return panel;
    }

    // Passage selection panel with radio buttons and text area
    //
    public static JPanel passageSelection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Row of radio buttons
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("Select Passage Length:");
        optionsPanel.add(label);

        // keep references so we can read config later
        shortBtn = new JRadioButton("Short");
        mediumBtn = new JRadioButton("Medium");
        longBtn = new JRadioButton("Long");
        customBtn = new JRadioButton("Custom");

        ButtonGroup group = new ButtonGroup();
        group.add(shortBtn);
        group.add(mediumBtn);
        group.add(longBtn);
        group.add(customBtn);

        optionsPanel.add(shortBtn);
        optionsPanel.add(mediumBtn);
        optionsPanel.add(longBtn);
        optionsPanel.add(customBtn);

        passageTextArea = new JTextArea(5, 25);
        passageTextArea.setEditable(false);
        passageTextArea.setLineWrap(true);
        passageTextArea.setWrapStyleWord(true);

        // Action listeners for radio buttons
        shortBtn.addActionListener(e -> {
            passageTextArea.setText(SHORT_PASSAGE);
            passageTextArea.setEditable(false);
        });

        mediumBtn.addActionListener(e -> {
            passageTextArea.setText(MEDIUM_PASSAGE);
            passageTextArea.setEditable(false);
        });

        longBtn.addActionListener(e -> {
            passageTextArea.setText(LONG_PASSAGE);
            passageTextArea.setEditable(false);
        });

        customBtn.addActionListener(e -> {
            passageTextArea.setText("");
            passageTextArea.setEditable(true);
        });

        // Default selection
        shortBtn.setSelected(true);
        passageTextArea.setText(SHORT_PASSAGE);

        panel.add(optionsPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passageTextArea);

        return panel;
    }

    // Panel for entering typist details - name, symbol, color, typing style, keyboard type
    //
    public static JPanel typistEntry(int typistNumber) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel typistLabel = new JLabel("Typist " + typistNumber);
        typistLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(typistLabel);
        panel.add(Box.createVerticalStrut(5));

        // Symbol entry
        JPanel symbolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel symbolLabel = new JLabel("Symbol:");
        JTextField symbolField = new JTextField(5);

        symbolPanel.add(symbolLabel);
        symbolPanel.add(symbolField);

        panel.add(symbolPanel);
        panel.add(Box.createVerticalStrut(2));

        // Color selection
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel colorLabel = new JLabel("Color:");
        JButton colorBtn = new JButton("Choose Color");
        JPanel preview = new JPanel();
        preview.setBackground(Color.BLACK);

        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(null, "Pick a color", preview.getBackground());
            if (c != null) {
                preview.setBackground(c);
            }
        });

        colorPanel.add(colorLabel);
        colorPanel.add(colorBtn);
        colorPanel.add(preview);

        panel.add(colorPanel);
        panel.add(Box.createHorizontalStrut(2));

        // Typing style selection
        JPanel typingStylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel typingStyleLabel = new JLabel("Typing Style:");
        String[] styles = { "Touch Typist", "Hunt & Peck", "Phone Thumbs", "Voice to Text" };
        JComboBox<String> styleCombo = new JComboBox<>(styles);

        typingStylePanel.add(typingStyleLabel);
        typingStylePanel.add(styleCombo);

        panel.add(typingStylePanel);
        panel.add(Box.createHorizontalStrut(2));

        // Keyboard type selection
        JPanel keyboardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel keyboardLabel = new JLabel("Keyboard Type:");
        String[] keyboards = { "Mechanical", "Membrane", "Touchscreen", "Stenography" };
        JComboBox<String> keyboardCombo = new JComboBox<>(keyboards);

        keyboardPanel.add(keyboardLabel);
        keyboardPanel.add(keyboardCombo);

        panel.add(keyboardPanel);
        panel.add(Box.createHorizontalStrut(2));

        // Accessories selection - multiple selection with checkboxes
        JPanel accessoriesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel accessoriesLabel = new JLabel("Accessories:");
        JCheckBox wristSupportBox = new JCheckBox("Wrist Support");
        JCheckBox energyDrinkBox = new JCheckBox("Energy Drink");
        JCheckBox headphonesBox = new JCheckBox("Noise-Cancelling Headphones");

        accessoriesPanel.add(accessoriesLabel);
        accessoriesPanel.add(wristSupportBox);
        accessoriesPanel.add(energyDrinkBox);
        accessoriesPanel.add(headphonesBox);

        panel.add(accessoriesPanel);

        // store references for config extraction
        TypistFields tf = new TypistFields();
        tf.symbolField = symbolField;
        tf.colorPreview = preview;
        tf.styleCombo = styleCombo;
        tf.keyboardCombo = keyboardCombo;
        tf.wristSupportBox = wristSupportBox;
        tf.energyDrinkBox = energyDrinkBox;
        tf.headphonesBox = headphonesBox;

        applyPresetToFields(typistNumber, tf);
        typistFieldsMap.put(typistNumber, tf);

        return panel;
    }

    public static JPanel difficultyConfiguration() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Difficulty modifiers info table
        String[] difficultyColumns = { "Difficulty Modifier", "Effect" };

        Object[][] difficultyData = {
                { "Autocorrect (On/Off)", "SlideBack −50% (when ON)" },
                { "Caffeine Mode", "Speed +20% (first 10 turns), Burnout +30% (after)" },
                { "Night Shift", "Accuracy −15% (all typists)" }
        };

        JTable difficultyTable = new JTable(difficultyData, difficultyColumns);
        difficultyTable.setEnabled(false);
        difficultyTable.setRowHeight(20);

        JScrollPane difficultyScroll = new JScrollPane(difficultyTable);
        difficultyScroll.setPreferredSize(new Dimension(400, 100));

        panel.add(difficultyScroll);

        panel.add(Box.createVerticalStrut(10));

        // Difficulty modifier entry
        JPanel difficultyEntryPanel = new JPanel();
        difficultyEntryPanel.setLayout(new BoxLayout(difficultyEntryPanel, BoxLayout.Y_AXIS));
        difficultyEntryPanel.setBorder(BorderFactory.createTitledBorder("Difficulty Modifiers"));

        // modifier options checkbox buttons
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        autocorrectBox = new JCheckBox("Autocorrect");
        caffeineBox = new JCheckBox("Caffeine Mode");
        nightShiftBox = new JCheckBox("Night Shift");

        optionsPanel.add(autocorrectBox);
        optionsPanel.add(caffeineBox);
        optionsPanel.add(nightShiftBox);

        difficultyEntryPanel.add(optionsPanel);

        panel.add(difficultyEntryPanel);

        return panel;
    }

    // ensure name and symbol are filled for each typist, and passage is not empty if custom
    //
    public static String validateConfig() {
        int numberOfTypists = typistSpinner == null ? 0 : (int) typistSpinner.getValue();

        for (int i = 1; i <= numberOfTypists; i++) {
            TypistFields tf = typistFieldsMap.get(i);
            if (tf != null) {
                if (tf.symbolField.getText().trim().isEmpty()) {
                    return "Typist " + i + ": Symbol cannot be empty.";
                }
            }
        }

        String passage = passageTextArea == null ? "" : passageTextArea.getText().trim();
        if (passage.isEmpty()) {
            return "Passage cannot be empty. Please select a passage length or enter a custom passage.";
        }

        return null;
    }

    // get current config from UI elements and return a RaceConfig object
    //
    public static RaceConfig getCurrentConfig() {
        RaceConfig cfg = new RaceConfig();

        if (shortBtn != null && shortBtn.isSelected())
            cfg.passageType = "Short";
        else if (mediumBtn != null && mediumBtn.isSelected())
            cfg.passageType = "Medium";
        else if (longBtn != null && longBtn.isSelected())
            cfg.passageType = "Long";
        else
            cfg.passageType = "Custom";

        cfg.passageText = passageTextArea == null ? "" : passageTextArea.getText();
        cfg.numberOfTypists = typistSpinner == null ? 0 : (int) typistSpinner.getValue();

        for (int i = 1; i <= cfg.numberOfTypists; i++) {

            TypistFields tf = typistFieldsMap.get(i);

            // Use centralized default typist for this seat so defaults are defined in TypistPresets
            Typist t = TypistPresets.getForSeat(i);
            if (tf != null) {
                // Apply UI overrides on top of preset values
                String symbolText = tf.symbolField.getText().trim();
                if (!symbolText.isEmpty()) {
                    t.setSymbol(symbolText.charAt(0));
                }
                t.setColor(tf.colorPreview.getBackground());
                t.setTypingStyle((String) tf.styleCombo.getSelectedItem());
                t.setKeyboardType((String) tf.keyboardCombo.getSelectedItem());

                // reset accessories to match UI
                t.removeAccessory("Wrist Support");
                t.removeAccessory("Energy Drink");
                t.removeAccessory("Noise-Cancelling Headphones");
                if (tf.wristSupportBox.isSelected()) {
                    t.addAccessory("Wrist Support");
                }
                if (tf.energyDrinkBox.isSelected()) {
                    t.addAccessory("Energy Drink");
                }
                if (tf.headphonesBox.isSelected()) {
                    t.addAccessory("Noise-Cancelling Headphones");
                }
            }
            cfg.typists.add(t);
        }

        if (autocorrectBox != null && autocorrectBox.isSelected()) {
            cfg.difficultyModifiers.add("Autocorrect");
        }
        if (caffeineBox != null && caffeineBox.isSelected()) {
            cfg.difficultyModifiers.add("Caffeine Mode");
        }
        if (nightShiftBox != null && nightShiftBox.isSelected()) {
            cfg.difficultyModifiers.add("Night Shift");
        }

        return cfg;
    }

    // show info table about typing styles, keyboard types, and accessories modifiers
    //
    public static JPanel showModifierInfo() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Typist Modifiers"));

        // Typing styles modifier table
        String[] typingColumns = { "Typing Style", "Speed", "Accuracy", "Burnout" };

        Object[][] typingData = {
                { "Touch Typist", 1.0, 1.1, 1.0 },
                { "Hunt & Peck", 1.0, 0.9, 1.1 },
                { "Phone Thumbs", 1.0, 0.8, 1.2 },
                { "Voice to Text", 1.0, 0.6, 0.8 }
        };

        JTable typingTable = new JTable(typingData, typingColumns);
        typingTable.setEnabled(false);
        typingTable.setRowHeight(20);

        JScrollPane typingScroll = new JScrollPane(typingTable);
        typingScroll.setBorder(BorderFactory.createTitledBorder("Typing Styles"));
        typingScroll.setPreferredSize(new Dimension(400, 120));

        panel.add(typingScroll);
        panel.add(Box.createVerticalStrut(10));

        // Keyboard types modifier table
        String[] keyboardColumns = { "Keyboard Type", "Speed", "Accuracy", "Burnout" };

        Object[][] keyboardData = {
                { "Mechanical", 1.3, 1.2, 1.0 },
                { "Membrane", 1.0, 1.0, 1.0 },
                { "Touchscreen", 0.8, 0.7, 1.0 },
                { "Stenography", 2.0, 1.4, 1.0 }
        };

        JTable keyboardTable = new JTable(keyboardData, keyboardColumns);
        keyboardTable.setEnabled(false);
        keyboardTable.setRowHeight(20);

        JScrollPane keyboardScroll = new JScrollPane(keyboardTable);
        keyboardScroll.setBorder(BorderFactory.createTitledBorder("Keyboard Types"));
        keyboardScroll.setPreferredSize(new Dimension(400, 120));

        panel.add(keyboardScroll);

        String[] accessoriesColumns = { "Accessory", "Effect" };

        Object[][] accessoriesData = {
                { "Wrist Support", "Burnout duration −20%" },
                { "Energy Drink", "Accuracy +10% (first half), −10% (second half)" },
                { "Noise-Cancelling Headphones", "Mistype chance −30%" }
        };

        JTable accessoriesTable = new JTable(accessoriesData, accessoriesColumns);
        accessoriesTable.setEnabled(false);
        accessoriesTable.setRowHeight(20);

        JScrollPane accessoriesScroll = new JScrollPane(accessoriesTable);
        accessoriesScroll.setBorder(BorderFactory.createTitledBorder("Accessories"));
        accessoriesScroll.setPreferredSize(new Dimension(400, 120));

        panel.add(accessoriesScroll);

        return panel;
    }

    // helper func to create a label
    //
    public static JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        return label;
    }

    // Prefill a typist UI row from centralized presets
    private static void applyPresetToFields(int typistNumber, TypistFields tf) {
        Typist preset = TypistPresets.getForSeat(typistNumber);
        if (preset == null)
            return;
        tf.symbolField.setText(String.valueOf(preset.getSymbol()));
        Color c = preset.getColor();
        if (c == null)
            c = Color.BLACK;
        tf.colorPreview.setBackground(c);
        if (preset.getTypingStyleName() != null) {
            tf.styleCombo.setSelectedItem(preset.getTypingStyleName());
        }
        if (preset.getKeyboardTypeName() != null) {
            tf.keyboardCombo.setSelectedItem(preset.getKeyboardTypeName());
        }
        // by default presets have no accessories; ensure UI reflects that
        tf.wristSupportBox.setSelected(preset.hasAccessory("Wrist Support"));
        tf.energyDrinkBox.setSelected(preset.hasAccessory("Energy Drink"));
        tf.headphonesBox.setSelected(preset.hasAccessory("Noise-Cancelling Headphones"));
    }

}
