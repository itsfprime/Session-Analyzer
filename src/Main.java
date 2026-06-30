import java.awt.*;
import java.io.File;
import com.formdev.flatlaf.FlatLightLaf;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {
    public static final int BUCKETS = 30;
    static final double ZSCORE = 1.96;

    public static void main(String[] args) throws IOException {
        FlatLightLaf.setup();
        String FILEPATH = findFile();

        File file = new File(FILEPATH);
        Scanner fileReader = new Scanner(file);
        fileReader.useDelimiter("\\Z"); // read entire file
        fileReader.close();

        double[] solves = Parser.parseSolves(FILEPATH);

        Sorter sorter = new Sorter(solves);

        double sessionMean =                    Calculator.calculateMean(solves);
        double[] sortedSessionData =            sorter.getSorted();
        double sessionMedian =                  Calculator.calculateMedian(sortedSessionData);
        double standardDeviation =              Calculator.calculateStandardDeviation(solves);
        double[] averages =                     Calculator.calculateAo5s(solves);
        double meanOfAverages =                 Calculator.calculateMean(averages);
        double[] averagesOf100 =                Calculator.calculateRollingAverage(solves, 100);
        double meanOfAo100 =                    Calculator.calculateMean(averagesOf100);
        double error =                          Calculator.calculateMOE(standardDeviation, solves);
        double skewnessCoefficient =            Calculator.calculateSkewnessCoefficient(sessionMean, sessionMedian, standardDeviation);
        int outliers =                          Calculator.countOutliers(solves, sessionMean, standardDeviation);
        int dnfCount =                          Parser.getDnfCount();

        double negativeTwoZScoreValue = sessionMean - (2 * standardDeviation);
        double positiveTwoZScoreValue = sessionMean + (2 * standardDeviation);
        double[] confidentMeanRange = new double[2];
        confidentMeanRange[0] = sessionMean - error;
        confidentMeanRange[1] = sessionMean + error;
        double[] confidentSolveRange = new double[2];
        confidentSolveRange[0] = sessionMean - 2 * standardDeviation;
        confidentSolveRange[1] = sessionMean + 2 * standardDeviation;
        double percentOutliers = ((double) outliers / (double) solves.length) * 100;

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
        String[] stats = {meanStr, medianStr, pbStr, sdStr, top5TimeStr, top95TimeStr, meanAo5Str, meanAo100Str, confIntStrMean, confIntStrSolve, skewnessStr, outlierStr, dnfStr};

        String report = meanStr + medianStr + pbStr + sdStr + top5TimeStr + top95TimeStr + meanAo5Str + meanAo100Str + confIntStrMean + confIntStrSolve + skewnessStr + outlierStr + dnfStr;

        System.out.println(report);

        GUIBuilder gui = new GUIBuilder(solves, averages, averagesOf100, sessionMean, report, stats, dnfCount);
        SwingUtilities.invokeLater(gui::buildGUI);
    }

    private static String findFile() throws IOException {
        File file = new File("src/data.txt");
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

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select your session.csv file");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        chooser.setApproveButtonText("Load Session");
        chooser.setBackground(new Color(144, 144, 144, 75));
        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            throw new IOException("No file selected");
        }

        String selectedPath = chooser.getSelectedFile().getAbsolutePath();

        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            writer.println(selectedPath);
        }

        return selectedPath;
    }
}
