import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class GUIBuilder {
    double[] solves;
    double[] averages;
    double[] averagesOf100;
    double sessionMean;
    String report;
    String[] stats;
    int dnfCount;
    private final Dimension PANEL_SIZE = new Dimension(800, 400);
    private JFrame frame;

    public GUIBuilder(double[] solves, double[] averages, double[] averagesOf100, double sessionMean, String report, String[] stats, int dnfCount){
        this.solves = solves;
        this.averages = averages;
        this.averagesOf100 = averagesOf100;
        this.sessionMean = sessionMean;
        this.report = report;
        this.stats = stats;
        this.dnfCount = dnfCount;
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
        JButton button = new JButton("Reset Graphs");
        button.setFont(new Font("Monospaced", Font.BOLD, 13));
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.LIGHT_GRAY);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        button.addActionListener(e -> buildGUI());
        JPanel panel = new JPanel();
        panel.add(button);
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
        return ChartFactory.createXYLineChart(
                "Ao100 Over Session", "Solve #", "Time(s)", dataset
        );
    }

    private JPanel buildStatsPanel(){
        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(BorderFactory.createTitledBorder("Session Stats"));
        panel.setBackground(Color.DARK_GRAY);
        panel.setForeground(Color.DARK_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 12, 4, 12);
        gbc.anchor = GridBagConstraints.WEST;

        String[][] statsReadout = {
                {"Session Mean", stats[0]},
                {"Session Median", stats[1]},
                {"Best solve", stats[2]},
                {"Standard Deviation", stats[3]},
                {"5th Percentile Time", stats[4]},
                {"95th Percentile Time", stats[5]},
                {"Mean Ao5", stats[6]},
                {"Mean Ao100", stats[7]},
                {"95% Confidence Interval (Mean)", stats[8]},
                {"95% Confidence Interval (Solve)", stats[9]},
                {"Skewness coefficient", stats[10]},
                {"Outliers",  stats[11]},
                {"DNF's", stats[12]}
        };

        for (int i = 0; i < statsReadout.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel key = new JLabel(statsReadout[i][0]);
            key.setForeground(Color.LIGHT_GRAY);
            key.setFont(new Font("Monospaced", Font.ITALIC, 13));
            panel.add(key, gbc);

            gbc.gridx = 1;
            JLabel val = new JLabel(statsReadout[i][1]);
            val.setForeground(i == 2 ? new Color(255, 255, 0) : Color.WHITE); // index 2 = PB solve
            val.setFont(new Font("Monospaced", Font.BOLD, 13));
            panel.add(val, gbc);
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
