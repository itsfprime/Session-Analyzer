public record SessionData(double[] solves, double[] averages, double[] averagesOf100,
                          double sessionMean, String report, String[] stats, int dnfCount) {}