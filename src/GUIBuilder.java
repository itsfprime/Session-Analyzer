import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GUIBuilder {
    double[] solves;
    double[] averages;
    double[] averagesOf100;
    double sessionMean;
    String report;
    LinkedHashMap<String, String> stats;
    int dnfCount;
    private final Dimension PANEL_SIZE = new Dimension(800, 400);
    private JFrame frame;

    public GUIBuilder(SessionData data){
        applyData(data);
    }

    private void applyData(SessionData data){
        this.solves = data.solves();
        this.averages = data.averages();
        this.averagesOf100 = data.averagesOf100();
        this.sessionMean = data.sessionMean();
        this.report = data.report();
        this.stats = data.stats();
        this.dnfCount = data.dnfCount();
    }

    public void buildGUI(){
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        ChartPanel solvePanel = new ChartPanel(buildSolveChart());
        ChartPanel ao100Panel = new ChartPanel(buildAo100Chart());
        styleChart(solvePanel.getChart());
        styleChart(ao100Panel.getChart());
        solvePanel.setPreferredSize(PANEL_SIZE);
        ao100Panel.setPreferredSize(PANEL_SIZE);

        contentPanel.add(buildStatsPanel());
        contentPanel.add(solvePanel);
        contentPanel.add(ao100Panel);
        contentPanel.add(createResetButtonPanel());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        if(frame == null){
            frame = new JFrame("Session Analyzer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 800);
        }

        frame.setContentPane(scrollPane);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }

    private JPanel createResetButtonPanel(){
        JButton resetGraphsBtn = new JButton("Reset Graphs");
        JButton newSessionBtn = new JButton("New Session");

        resetGraphsBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        resetGraphsBtn.setBackground(Color.DARK_GRAY);
        resetGraphsBtn.setForeground(Color.LIGHT_GRAY);
        resetGraphsBtn.setFocusPainted(false);
        resetGraphsBtn.setContentAreaFilled(false);
        resetGraphsBtn.setBorder(BorderFactory.createRaisedSoftBevelBorder());

        newSessionBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        newSessionBtn.setBackground(Color.DARK_GRAY);
        newSessionBtn.setForeground(Color.LIGHT_GRAY);
        newSessionBtn.setFocusPainted(false);
        newSessionBtn.setContentAreaFilled(false);
        newSessionBtn.setBorder(BorderFactory.createRaisedSoftBevelBorder());

        resetGraphsBtn.addActionListener(_ -> buildGUI());

        newSessionBtn.addActionListener(_ -> {
            try {
                String newPath = Main.selectNewFile();
                if (newPath == null) return; // user cancelled
                SessionData data = Main.computeStats(newPath);
                applyData(data);
                buildGUI();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Failed to load new session: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel panel = new JPanel();
        panel.add(resetGraphsBtn);
        panel.add(newSessionBtn);
        panel.setBackground(Color.DARK_GRAY);
        return panel;
    }

    private JFreeChart buildAo100Chart(){
        XYSeries ao100Series = new XYSeries("Ao100");
        for (int i = 0; i < averagesOf100.length; i++)
            ao100Series.add(i + 1, averagesOf100[i]);

        XYSeries meanSeries = new XYSeries("Session mean");
        meanSeries.add(1, sessionMean);
        meanSeries.add(averagesOf100.length, sessionMean);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(ao100Series);
        dataset.addSeries(meanSeries);
        JFreeChart chart = ChartFactory.createXYLineChart("Ao100 Over Session", "Solve #", "Time(s)", dataset);

        XYPlot plot = chart.getXYPlot();
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        double zoomLevel = 1.2;
        yAxis.setLowerBound(sessionMean / zoomLevel);
        yAxis.setAutoRangeIncludesZero(false);

        return chart;
    }

    private JPanel buildStatsPanel(){
        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(BorderFactory.createTitledBorder("Session Stats"));
        panel.setBackground(Color.DARK_GRAY);
        panel.setForeground(Color.DARK_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 12, 4, 12);
        gbc.anchor = GridBagConstraints.WEST;

        int yCount = 0;
        for (Map.Entry<String, String> entry : stats.entrySet()) {
            gbc.gridx = 0; gbc.gridy = yCount;
            JLabel key = new JLabel(entry.getKey());
            key.setForeground(Color.LIGHT_GRAY);
            key.setFont(new Font("Monospaced", Font.ITALIC, 13));
            panel.add(key, gbc);

            gbc.gridx = 1;
            JLabel val = new JLabel(entry.getValue());
            val.setForeground(entry.getKey().equals("Best Solve") ? new Color(255, 255, 0) : Color.WHITE);
            val.setFont(new Font("Monospaced", Font.BOLD, 13));
            panel.add(val, gbc);
            yCount++;
        }

        return panel;
    }

    private void styleChart(JFreeChart chart){
        Color bg = Color.DARK_GRAY;
        chart.setBackgroundPaint(bg);
        chart.getPlot().setBackgroundPaint(bg);
        chart.getPlot().setOutlinePaint(Color.GRAY);
        
        if (chart.getPlot() instanceof XYPlot plot) {
            plot.getDomainAxis().setLabelPaint(Color.LIGHT_GRAY);
            plot.getDomainAxis().setTickLabelPaint(Color.LIGHT_GRAY);
            plot.getRangeAxis().setLabelPaint(Color.LIGHT_GRAY);
            plot.getRangeAxis().setTickLabelPaint(Color.LIGHT_GRAY);
            plot.setDomainGridlinePaint(Color.GRAY);
            plot.setRangeGridlinePaint(Color.GRAY);
        }

        chart.getLegend().setBackgroundPaint(bg);
        chart.getLegend().setItemPaint(Color.WHITE);
        chart.getTitle().setPaint(Color.WHITE);
    }

    private JFreeChart buildSolveChart(){
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.RELATIVE_FREQUENCY);
        dataset.addSeries("Solve Times", solves, Main.BUCKETS);
        return ChartFactory.createHistogram(
                "Solve Time Distribution", "Solve Time(s)", "Frequency", dataset
        );
    }
}
