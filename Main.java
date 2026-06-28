import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class Main {
    private static final String FILEPATH = "C:\\Users\\pwcub\\IdeaProjects\\SessionAnalyzer\\src\\session7.csv";
    static File file = new File(FILEPATH);
    public static final int BUCKETS = 30;
    static final double ZSCORE = 1.96;

    public static void main(String[] args) throws FileNotFoundException {
        Scanner fileReader = new Scanner(file);
        fileReader.useDelimiter("\\Z"); // read entire file
        String content = fileReader.next();
        fileReader.close();

        String[] entries = content.split("(?m)(?=^\\d+;)"); // split before each entry number
        double[] solves = new double[entries.length];

        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i];
            int firstSemi = entry.indexOf(';');
            int secondSemi = entry.indexOf(';', firstSemi + 1);

            String timeStr = entry.substring(firstSemi + 1, secondSemi);
            if (timeStr.startsWith("DNF")) {
                // extract the time inside DNF(x) or skip entirely
                timeStr = timeStr.substring(4, timeStr.length() - 1); // strips "DNF(" and ")"
            } else if (timeStr.endsWith("+")) {
            timeStr = timeStr.substring(0, timeStr.length() - 1);
            solves[i] = Double.parseDouble(timeStr) + 2;
            continue; // skip the parseDouble below
        }
            solves[i] = Double.parseDouble(timeStr);
        }

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
        String[] stats = {meanStr, medianStr, pbStr, sdStr, top5TimeStr, top95TimeStr, meanAo5Str, meanAo100Str, confIntStrMean, confIntStrSolve, skewnessStr, outlierStr};

        String report = meanStr + medianStr + pbStr + sdStr + top5TimeStr + top95TimeStr + meanAo5Str + meanAo100Str + confIntStrMean + confIntStrSolve + skewnessStr + outlierStr;

        System.out.println(report);

        GUIBuilder gui = new GUIBuilder(solves, averages, averagesOf100, sessionMean, report, stats);
        SwingUtilities.invokeLater(gui::buildGUI);
    }
}
