package hu.webarticum.holodb.testing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigInteger;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import hu.webarticum.holodb.data.binrel.monotonic.BinomialDistributedMonotonic;
import hu.webarticum.holodb.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.data.random.HasherTreeRandom;
import hu.webarticum.holodb.data.random.TreeRandom;

public class QuantityDistributionDisplayer implements Runnable {
    
    @Override
    public void run() {
        JFrame frame = new JFrame("Chart");
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        JPanel gridPanel = new JPanel(new GridLayout(0, 3));

        long millis1 = System.currentTimeMillis();
        gridPanel.add(createQuantityChartPanel(10, 3));
        gridPanel.add(createQuantityChartPanel(50, 7));
        gridPanel.add(createQuantityChartPanel(100, 10));
        gridPanel.add(createQuantityChartPanel(100, 20));
        gridPanel.add(createQuantityChartPanel(350, 47));
        gridPanel.add(createQuantityChartPanel(420, 73));
        gridPanel.add(createQuantityChartPanel(540, 150));
        gridPanel.add(createQuantityChartPanel(740, 215));
        gridPanel.add(createQuantityChartPanel(1200, 200));
        gridPanel.add(createQuantityChartPanel(3200, 1100));
        gridPanel.add(createQuantityChartPanel(7800, 1200));
        gridPanel.add(createQuantityChartPanel(12000, 2100));
        gridPanel.add(createQuantityChartPanel(35000, 8900));
        gridPanel.add(createQuantityChartPanel(112000, 18300));
        gridPanel.add(createQuantityChartPanel(503000, 79200));
        gridPanel.add(createQuantityChartPanel(1080000, 115000));
        gridPanel.add(createQuantityChartPanel(1567000, 232100));
        gridPanel.add(createQuantityChartPanel(69500000, 9327000));
        long millis2 = System.currentTimeMillis();

        JLabel infoLabel = new JLabel(String.format("Collected in: %d milliseconds", millis2 - millis1));
        mainPanel.add(infoLabel, BorderLayout.PAGE_START);
        
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setPreferredSize(new Dimension(gridPanel.getPreferredSize().width + 30, 700));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    private ChartPanel createQuantityChartPanel(int n, int k) {
        ChartPanel chartPanel = new ChartPanel(createQuantityChart(n, k));
        chartPanel.setPreferredSize(new Dimension(500, 320));
        return chartPanel;
    }
    
    private JFreeChart createQuantityChart(int n, int k) {
        int m = Math.min(n, (int) Math.ceil(2d * n / k) + 1);
        
        DefaultCategoryDataset lineChartDataset = new DefaultCategoryDataset();

        double[] countsFromMonotonic = getCountsFromMonotonics(n, k, m);
        for (int i = 0; i < m; i++) {
            lineChartDataset.addValue(countsFromMonotonic[i], "monotonic", Integer.valueOf(i));
        }

        double[] countsFromMeasure = getCountsFromMeasure(n, k, m);
        for (int i = 0; i < m; i++) {
            lineChartDataset.addValue(countsFromMeasure[i], "measure", Integer.valueOf(i));
        }

        double[] countsFromFormula = getCountsFromFormula(n, k, m);
        for (int i = 0; i < m; i++) {
            lineChartDataset.addValue(countsFromFormula[i], "formula", Integer.valueOf(i));
        }
        
        return ChartFactory.createLineChart(
            String.format("Distribution (n=%d, k=%d)", n, k),
            "Quantity", "Count",
            lineChartDataset,
            PlotOrientation.VERTICAL,
            true, false, false
        );
    }

    private double[] getCountsFromMonotonics(int n, int k, int m) {
        return getAvgs(new double[][] {
            getCountsFromMonotonic(n, k, m, 0),
            getCountsFromMonotonic(n, k, m, 12),
            getCountsFromMonotonic(n, k, m, 273481),
        });
    }
    
    private double[] getCountsFromMonotonic(int n, int k, int m, long seed) {
        return getCountsFromMonotonic(new BinomialDistributedMonotonic(buildTreeRandom(seed), n, k), m);
    }
    
    private TreeRandom buildTreeRandom(long seed) {
        return new HasherTreeRandom(seed);
    }
    
    private double[] getCountsFromMonotonic(Monotonic monotonic, int m) {
        BigInteger imageSize = monotonic.imageSize();
        
        double[] result = new double[m];
        
        BigInteger step = imageSize.compareTo(BigInteger.valueOf(2000)) > 0 ? imageSize.divide(BigInteger.valueOf(2000)): BigInteger.ONE;
        for (BigInteger value = BigInteger.ZERO; value.compareTo(imageSize) < 0; value = value.add(step)) {
            int count = monotonic.indicesOf(value).getCount().intValue();
            if (count < m) {
                result[count] += step.intValue();
            }
        }
        return result;
    }

    private double[] getCountsFromMeasure(int n, int k, int m) {
        if (n > 5000) {
            return new double[m];
        }
        
        int[] seeds = new int[] { 0, 15, 432, 7362, 11260, 734652, 6473821, 27348245 };
        double[][] measures = new double[seeds.length][];
        for (int i = 0; i < seeds.length; i++) {
            measures[i] = getCountsFromMeasure(n, k, m, seeds[i]);
        }
        return getAvgs(measures);
    }
    
    private double[] getCountsFromMeasure(int n, int k, int m, int seed) {
        Random random = new Random(seed);
        int[] quantities = new int[k];
        for (int i = 0; i < n; i++) {
            int value = random.nextInt(k);
            quantities[value]++;
        }
        double[] quantityCounts = new double[m];
        for (int quantity : quantities) {
            if (quantity < m) {
                quantityCounts[quantity]++;
            }
        }
        return quantityCounts;
    }

    private double[] getCountsFromFormula(int n, int k, int m) {
        BinomialDistribution binomialDistribution = new BinomialDistribution(n, 1d / k);
        double[] quantityCounts = new double[m];
        for (int i = 0; i < m; i++) {
            quantityCounts[i] = binomialDistribution.probability(i) * k;
        }
        binomialDistribution.reseedRandomGenerator(1);
        return quantityCounts;
    }

    private double[] getAvgs(double[][] numbers) {
        int length = numbers[0].length;
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
            double sum = 0;
            for (int j = 0; j < numbers.length; j++) {
                sum += numbers[j][i];
            }
            result[i] = sum / numbers.length;
        }
        return result;
    }
    
}
