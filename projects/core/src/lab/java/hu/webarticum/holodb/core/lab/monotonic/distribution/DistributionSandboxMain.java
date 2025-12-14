package hu.webarticum.holodb.core.lab.monotonic.distribution;

import java.util.Random;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import hu.webarticum.holodb.core.lab.util.CommandLineUtil;

public class DistributionSandboxMain {

    public static final String TITLE = "Distribution sandbox";


    public static void main(String[] args) {

        Random random = new Random(0);
        for (int i = 0; i < 100; i++) {
            double u = random.nextDouble();
            double v = random.nextDouble();
            double s = (u * u) + (v * v);
            double x = u * Math.sqrt(-2 * Math.log(s) / s);
            double y = v * Math.sqrt(-2 * Math.log(s) / s);
            System.out.println(String.format("%.5f   %.5f", x, y)); // NOSONAR
        }

        System.exit(0);

        CommandLineUtil.printTitle(TITLE);

        int n = 100;
        int k = 20;

        int nn = 35;

        double p = ((double) k) / n;

        System.out.println("Binomial:"); // NOSONAR

        int[] binomialSamples = new int[nn];
        BinomialDistribution binomialDistribution = new BinomialDistribution(n, p);
        for (int i = 0; i < 2000; i++) {
            int sample = binomialDistribution.sample();
            if (sample >= 0 && sample < nn) {
                binomialSamples[sample]++;
            }
        }

        for (int sample : binomialSamples) {
            CommandLineUtil.printRepeated('#', sample);
            System.out.println(); // NOSONAR
        }

        CommandLineUtil.printSeparator();

        System.out.println("Normal approximation:"); // NOSONAR

        int[] normalSamples = new int[nn];
        NormalDistribution normalDistribution = new NormalDistribution(n * p, Math.sqrt(n * p * (1 - p)));
        for (int i = 0; i < 2000; i++) {
            int sample = (int) Math.round(normalDistribution.sample());
            if (sample >= 0 && sample < nn) {
                normalSamples[sample]++;
            }
        }

        for (int sample : normalSamples) {
            CommandLineUtil.printRepeated('#', sample);
            System.out.println(); // NOSONAR
        }

    }

}
