import java.io.File;
import com.formdev.flatlaf.FlatDarkLaf;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {
    public static final int BUCKETS = 30;
    static final double ZSCORE = 1.96;

    public static void main(String[] args) throws IOException {
        FlatDarkLaf.setup();
        String filepath = findFile();
        SessionData data = computeStats(filepath);
        GUIBuilder gui = new GUIBuilder(data);
        SwingUtilities.invokeLater(gui::buildGUI);
    }

    public static SessionData computeStats(String filepath) throws IOException {
        Parser.ParseResult solves = Parser.parseSolves(filepath);
        Sorter sorter = new Sorter(solves.solves());

        double sessionMean = Calculator.calculateMean(solves.solves());
        double[] sortedSessionData = sorter.getSorted();
        double sessionMedian = Calculator.calculateMedian(sortedSessionData);
        double standardDeviation = Calculator.calculateStandardDeviation(solves.solves());
        double[] averages = Calculator.calculateAo5s(solves.solves());
        double meanOfAverages = Calculator.calculateMean(averages);
        double[] averagesOf100 = Calculator.calculateRollingAverage(solves.solves(), 100);
        double meanOfAo100 = Calculator.calculateMean(averagesOf100);
        double error = Calculator.calculateMOE(standardDeviation, solves.solves());
        double skewnessCoefficient = Calculator.calculateSkewnessCoefficient(sessionMean, sessionMedian, standardDeviation);
        int outliers = Calculator.countOutliers(solves.solves(), sessionMean, standardDeviation);
        int dnfCount = solves.dnfCount();
        double negativeTwoZScoreValue = sessionMean - (2 * standardDeviation);
        double positiveTwoZScoreValue = sessionMean + (2 * standardDeviation);
        double[] confidentMeanRange = { sessionMean - error, sessionMean + error };
        double[] confidentSolveRange = { sessionMean - 2 * standardDeviation, sessionMean + 2 * standardDeviation };
        double percentOutliers = ((double) outliers / (double) solves.solves().length) * 100;

        LinkedHashMap<String, String> stats = new LinkedHashMap<>();
        stats.put("Session Mean", String.format("%.3fs", sessionMean));
        stats.put("Session Median", String.format("%.3fs", sessionMedian));
        stats.put("Best Solve", String.format("%.3fs", sortedSessionData[0]));
        stats.put("Standard Deviation", String.format("%.3fs", standardDeviation));
        stats.put("5th Percentile Time", String.format("%.3fs", negativeTwoZScoreValue));
        stats.put("95th Percentile Time", String.format("%.3fs", positiveTwoZScoreValue));
        stats.put("Mean Ao5", String.format("%.3fs", meanOfAverages));
        stats.put("Mean Ao100", String.format("%.3fs", meanOfAo100));
        stats.put("95% Confidence Interval (Mean)", String.format("%.3fs - %.3fs", confidentMeanRange[0], confidentMeanRange[1]));
        stats.put("95% Confidence Interval (Solve)", String.format("%.3fs - %.3fs", confidentSolveRange[0], confidentSolveRange[1]));
        stats.put("Skewness Coefficient", String.format("%.3f", skewnessCoefficient));
        stats.put("Outliers", String.format("%d (%.3f%%)", outliers, percentOutliers));
        stats.put("DNF's", String.format("%d", dnfCount));

        String report = stats.values().stream().collect(Collectors.joining("\n", "", "\n"));

        return new SessionData(solves.solves(), averages, averagesOf100, sessionMean, report, stats, dnfCount);
    }

    /** Always shows the chooser (no data.txt shortcut) and persists the new path. Returns null if cancelled. */
    public static String selectNewFile() throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select your session.csv file");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        chooser.setApproveButtonText("Load Session");
        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        String selectedPath = chooser.getSelectedFile().getAbsolutePath();
        File dir = new File(System.getenv("APPDATA"), "SessionAnalyzer");
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new File(dir, "data.txt"))) {
            writer.println(selectedPath);
        }
        return selectedPath;
    }

    private static String findFile() throws IOException {
        File dir = new File(System.getenv("APPDATA"), "SessionAnalyzer");
        File file = new File(dir, "data.txt");

        if (file.createNewFile()) {
            System.out.println("data.txt not found, created");
        } else {
            System.out.println("data.txt found");
        }

        Scanner reader = new Scanner(file);
        String path = reader.hasNextLine() ? reader.nextLine().trim() : "";
        reader.close();

        if (!path.isEmpty() && new File(path).exists()) {
            return path;
        }

        String selected = selectNewFile();
        if (selected == null) {
            throw new IOException("No file selected");
        }
        return selected;
    }
}
