import java.io.File;
import com.formdev.flatlaf.FlatDarkLaf;
import java.io.IOException;
import java.util.Scanner;
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
        File file = new File(filepath);
        Scanner fileReader = new Scanner(file);
        fileReader.useDelimiter("\\Z");
        fileReader.close();

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

        String meanStr = String.format("Session Mean: %.3fs\n", sessionMean);
        String medianStr = String.format("Session Median: %.3fs\n", sessionMedian);
        String pbStr = String.format("Best solve: %.3fs\n", sortedSessionData[0]);
        String sdStr = String.format("Standard Deviation: %.3fs\n", standardDeviation);
        String top5TimeStr = String.format("5th Percentile Time: %.3fs\n", negativeTwoZScoreValue);
        String top95TimeStr = String.format("95th Percentile Time: %.3fs\n", positiveTwoZScoreValue);
        String meanAo5Str = String.format("Mean Ao5: %.3fs\n", meanOfAverages);
        String meanAo100Str = String.format("Mean Ao100: %.3fs\n", meanOfAo100);
        String confIntStrMean = String.format("95%% Confidence Interval (Mean): %.3fs - %.3fs\n", confidentMeanRange[0], confidentMeanRange[1]);
        String confIntStrSolve = String.format("95%% Confidence Interval (Solve): %.3fs - %.3fs\n", confidentSolveRange[0], confidentSolveRange[1]);
        String skewnessStr = String.format("Skewness coefficient: %.3f\n", skewnessCoefficient);
        String outlierStr = String.format("Outliers: %d (%.3f%%)\n", outliers, percentOutliers);
        String dnfStr = String.format("DNFs: %d\n", dnfCount);
        String[] stats = {meanStr, medianStr, pbStr, sdStr, top5TimeStr, top95TimeStr, meanAo5Str,
                meanAo100Str, confIntStrMean, confIntStrSolve, skewnessStr, outlierStr, dnfStr};
        String report = meanStr + medianStr + pbStr + sdStr + top5TimeStr + top95TimeStr + meanAo5Str
                + meanAo100Str + confIntStrMean + confIntStrSolve + skewnessStr + outlierStr + dnfStr;
        System.out.println(report);

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
