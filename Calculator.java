import java.util.Arrays;

public class Calculator {
    /**
     * @param sorted A sorted array of doubles
     * @return The median of the array
     */
    public static double calculateMedian(double[] sorted) {
        int mid = sorted.length / 2;
        if (sorted.length % 2 == 0)
            return (sorted[mid - 1] + sorted[mid]) / 2.0;
        return sorted[mid];
    }

    public static int countOutliers(double[] solves, double sessionMean, double standardDeviation){
        int outliers = 0;
        for(int i = 0; i < solves.length; i++){
            if(solves[i] > sessionMean + (2 * standardDeviation) || solves[i] < sessionMean - (2 * standardDeviation)) outliers++;
        }
        return outliers;
    }

    public static double calculateSkewnessCoefficient(double mean, double median, double standardDeviation){
        return (3*(mean - median))/standardDeviation;
    }

    public static double calculateMOE(double standardDeviation, double[] solves){
        int length = solves.length;
        return Main.ZSCORE * (standardDeviation / Math.sqrt(length));
    }

    public static double[] calculateAo5s(double[] solves){
        double[] ao5s = new double[solves.length - 4];
        for (int i = 4; i < solves.length; i++) {
            double[] window = {solves[i], solves[i-1], solves[i-2], solves[i-3], solves[i-4]};
            double min = Arrays.stream(window).min().getAsDouble();
            double max = Arrays.stream(window).max().getAsDouble();
            double avg = (Arrays.stream(window).sum() - min - max) / 3.0;
            ao5s[i-4] = avg;
        }
        return ao5s;
    }

    public static double[] calculateRollingAverage(double[] solves, int window) {
        double[] avgs = new double[solves.length - (window - 1)];
        for (int i = window - 1; i < solves.length; i++) {
            double[] w = Arrays.copyOfRange(solves, i - (window - 1), i + 1);
            double min = Arrays.stream(w).min().getAsDouble();
            double max = Arrays.stream(w).max().getAsDouble();
            avgs[i - (window - 1)] = (Arrays.stream(w).sum() - min - max) / (window - 2);
        }
        return avgs;
    }

    public static double calculateStandardDeviation(double[] array) {
        // get the sum of array
        double sum = 0.0;
        for (double i : array) {
            sum += i;
        }

        // get the mean of array
        int length = array.length;
        double mean = sum / length;

        // calculate the standard deviation
        double standardDeviation = 0.0;
        for (double num : array) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

    public static double calculateMean(double[] solves){
        double sum = 0.0;
        for(double d : solves){
            sum += d;
        }
        return sum / solves.length;
    }
}
