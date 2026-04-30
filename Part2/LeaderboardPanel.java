import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LeaderboardPanel extends JPanel {

    private static JTable table;
    private static DefaultTableModel model;
    private JTextField typistNameField;
    private JComboBox<String> upgradeBox;
    private JLabel statusLabel;

    public LeaderboardPanel() {
        setLayout(new BorderLayout(12, 12));

        add(buildUpgradePanel(), BorderLayout.NORTH);

        String[] cols = { "Rank", "Name", "Points", "Coins", "Wins", "Races", "Burnouts", "Avg WPM",
                "Titles/Badges", "Upgrades" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(22);
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);
        refreshTable();
    }

    private JPanel buildUpgradePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Upgrade Accessories"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Typist Name:");
        typistNameField = new JTextField(14);

        JLabel upgradeLabel = new JLabel("Accessory:");
        upgradeBox = new JComboBox<>(new String[] { "Wrist Support", "Mechanical Keyboard", "Energy Drink" });

        JButton buyButton = new JButton("Buy Upgrade");
        buyButton.addActionListener(e -> handlePurchase());

        statusLabel = new JLabel(" ");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        panel.add(typistNameField, gbc);

        gbc.gridx = 2;
        panel.add(upgradeLabel, gbc);

        gbc.gridx = 3;
        panel.add(upgradeBox, gbc);

        gbc.gridx = 4;
        panel.add(buyButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        panel.add(statusLabel, gbc);

        return panel;
    }

    private void handlePurchase() {
        String typistName = typistNameField.getText().trim();
        String upgrade = (String) upgradeBox.getSelectedItem();

        if (typistName.isEmpty()) {
            statusLabel.setText("Enter a typist name first.");
            return;
        }

        int cost = LeaderboardManager.getUpgradeCost(upgrade);
        if (cost < 0) {
            statusLabel.setText("Unknown upgrade selected.");
            return;
        }

        boolean purchased = LeaderboardManager.purchaseUpgrade(typistName, upgrade);
        if (purchased) {
            statusLabel.setText(
                    upgrade + " purchased for " + typistName + " (" + cost + " coins). It will apply next race.");
            refreshTable();
        } else {
            int coins = LeaderboardManager.getCoins(typistName);
            statusLabel.setText(
                    "Purchase failed. " + typistName + " has " + coins + " coins; " + upgrade + " costs " + cost + ".");
        }
    }

    public static void refreshTable() {
        if (model == null)
            return;
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            java.util.List<LeaderboardEntry> list = LeaderboardManager.getSortedEntries();
            int rank = 1;
            for (LeaderboardEntry e : list) {
                String titles = String.join(", ", e.titles);
                String ups = String.join(", ", e.upgrades);
                model.addRow(new Object[] { rank, e.name, e.points, e.coins, e.wins, e.racesPlayed, e.burnouts,
                        String.format("%.2f", e.getAvgWPM()), titles, ups });
                rank++;
            }
        });
    }

}
