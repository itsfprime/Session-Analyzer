import java.util.Arrays;

public class Sorter {
    private double[] solves;
    private double[] sorted;

    public Sorter(double[] solves){
        this.solves = solves;
        sorted = sort(solves);
    }

    public double[] getSorted() { return sorted; }

    private double[] sort(double[] solves) {
        if (solves.length <= 1) return solves;

        int mid = solves.length / 2;
        double[] left = Arrays.copyOfRange(solves, 0, mid);
        double[] right = Arrays.copyOfRange(solves, mid, solves.length);

        return merge(sort(left), sort(right));
    }

    private double[] merge(double[] left, double[] right) {
        double[] sorted = new double[left.length + right.length];
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) sorted[k++] = left[i++];
            else sorted[k++] = right[j++];
        }
        while (i < left.length) sorted[k++] = left[i++];
        while (j < right.length) sorted[k++] = right[j++];
        return sorted;
    }
}
