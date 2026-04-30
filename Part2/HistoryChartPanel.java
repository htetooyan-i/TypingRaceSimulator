import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
public class HistoryChartPanel extends JPanel {
    private final RaceConfig cfg;

    public HistoryChartPanel(RaceConfig cfg) {
        this.cfg = cfg;
        setPreferredSize(new Dimension(760, 460));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 60;
            int right = 30;
            int top = 30;
            int bottom = 60;
            int chartWidth = Math.max(1, width - left - right);
            int chartHeight = Math.max(1, height - top - bottom);

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);

            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(left, top, left, top + chartHeight);
            g2.drawLine(left, top + chartHeight, left + chartWidth, top + chartHeight);

            java.util.List<TypistHistory> histories = new ArrayList<>();
            double maxWpm = 0.0;
            int maxPoints = 0;
            for (Typist t : cfg.typists) {
                TypistHistory history = StatisticsManager.getTypistHistory(t.getName());
                histories.add(history);
                maxPoints = Math.max(maxPoints, history.getRaceHistory().size());
                for (RaceResult result : history.getRaceHistory()) {
                    maxWpm = Math.max(maxWpm, result.getWordsPerMinute());
                }
            }

            if (maxPoints == 0) {
                g2.setColor(Color.GRAY);
                g2.drawString("No history data available yet.", left + 20, top + 30);
                return;
            }

            if (maxWpm <= 0) {
                maxWpm = 1;
            }

            g2.setColor(new Color(220, 220, 220));
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
            for (int i = 0; i <= 5; i++) {
                int y = top + chartHeight - (i * chartHeight / 5);
                g2.drawLine(left, y, left + chartWidth, y);
                String label = String.format("%.0f", (maxWpm * i) / 5.0);
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(label, 15, y + 4);
                g2.setColor(new Color(220, 220, 220));
            }

            int legendX = left + 10;
            int legendY = 12;

            for (int idx = 0; idx < cfg.typists.size(); idx++) {
                Typist t = cfg.typists.get(idx);
                TypistHistory history = histories.get(idx);
                java.util.List<RaceResult> results = history.getRaceHistory();
                
                Color lineColor = Color.BLACK;
                if (!results.isEmpty() && t.getColor() != null) {
                    lineColor = t.getColor();
                }

                if (results.size() >= 1) {
                    g2.setColor(lineColor);
                    g2.setStroke(new BasicStroke(2.2f));
                    int prevX = -1;
                    int prevY = -1;
                    for (int i = 0; i < results.size(); i++) {
                        RaceResult result = results.get(i);
                        int x = left + (results.size() == 1 ? chartWidth / 2 : (i * chartWidth) / (results.size() - 1));
                        int y = top + chartHeight
                                - (int) Math.round((result.getWordsPerMinute() / maxWpm) * chartHeight);
                        if (prevX >= 0) {
                            g2.drawLine(prevX, prevY, x, y);
                        }
                        g2.fillOval(x - 3, y - 3, 6, 6);
                        prevX = x;
                        prevY = y;
                    }

                    g2.fillRect(legendX, legendY - 9, 12, 4);
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawString(t.getName(), legendX + 18, legendY - 5);
                    legendY += 18;
                }
            }

            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Race number", left + chartWidth / 2 - 30, height - 20);
            g2.rotate(-Math.PI / 2);
            g2.drawString("WPM", -(top + chartHeight / 2 + 10), 20);
        } finally {
            g2.dispose();
        }
    }
}