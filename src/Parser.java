import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {
    public record ParseResult(double[] solves, int dnfCount) {}

    public static ParseResult parseSolves(String filepath) throws FileNotFoundException {
        Scanner fileReader = new Scanner(new File(filepath));
        fileReader.useDelimiter("\\Z");
        String content = fileReader.next();
        fileReader.close();
        Scanner reader = new Scanner(content);
        String line = reader.nextLine();
        if (line.equalsIgnoreCase("No.;Time;Comment;Scramble;Date;P.1")) {
            content = content.substring(line.length()).replaceFirst("^\\R", "");
        }

        String[] entries = content.split("(?m)(?=^\\d+;)");
        List<Double> solves = new ArrayList<>();
        int dnfCount = 0;

        for (String entry : entries) {
            int firstSemi = entry.indexOf(';');
            int secondSemi = entry.indexOf(';', firstSemi + 1);
            String timeStr = entry.substring(firstSemi + 1, secondSemi);

            if (timeStr.startsWith("DNF")) {
                dnfCount++;
            } else if (timeStr.endsWith("+")) {
                timeStr = timeStr.substring(0, timeStr.length() - 1);
                solves.add(Double.parseDouble(timeStr) + 2);
            } else {
                solves.add(Double.parseDouble(timeStr));
            }
        }

        return new ParseResult(solves.stream().mapToDouble(Double::doubleValue).toArray(), dnfCount);
    }
}