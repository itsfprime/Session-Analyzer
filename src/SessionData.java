import java.util.LinkedHashMap;

public record SessionData(double[] solves, double[] averages, double[] averagesOf100,
                          double sessionMean, String report, LinkedHashMap<String, String> stats, int dnfCount) {}