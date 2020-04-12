package hu.webarticum.holodb.testing;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigInteger;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class QuantityDistributionDisplayer implements Runnable {

    @Override
    public void run() {
        JFrame frame = new JFrame("Chart");
        
        JPanel gridPanel = new JPanel(new GridLayout(0, 3));

        gridPanel.add(createQuantityChartPanel(10, 3));
        gridPanel.add(createQuantityChartPanel(50, 7));
        gridPanel.add(createQuantityChartPanel(100, 10));
        gridPanel.add(createQuantityChartPanel(100, 20));
        gridPanel.add(createQuantityChartPanel(350, 47));
        gridPanel.add(createQuantityChartPanel(420, 73));
        gridPanel.add(createQuantityChartPanel(540, 150));
        gridPanel.add(createQuantityChartPanel(1200, 200));
        gridPanel.add(createQuantityChartPanel(3200, 1100));
        gridPanel.add(createQuantityChartPanel(7800, 1200));
        gridPanel.add(createQuantityChartPanel(12000, 2100));
        gridPanel.add(createQuantityChartPanel(120000, 12000));
        gridPanel.add(createQuantityChartPanel(500000, 42000));
        gridPanel.add(createQuantityChartPanel(1000000, 33000));
        gridPanel.add(createQuantityChartPanel(2500000, 79000));
        
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setPreferredSize(new Dimension(gridPanel.getPreferredSize().width + 30, 700));
        
        frame.setContentPane(scrollPane);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    private static ChartPanel createQuantityChartPanel(int n, int k) {
        ChartPanel chartPanel = new ChartPanel(createQuantityChart(n, k));
        chartPanel.setPreferredSize(new Dimension(500, 320));
        return chartPanel;
    }
    
    private static JFreeChart createQuantityChart(int n, int k) {
        int m = Math.min(n, (int) Math.ceil(2d * n / k) + 1);
        
        DefaultCategoryDataset lineChartDataset = new DefaultCategoryDataset();

        double[] countsFromMonotonic = getCountsFromMonotonic(n, k, m);
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
    
    private static double[] getCountsFromMonotonic(int n, int k, int m) {
        
        // TODO
        return new double[m];
        
    }

    private static double[] getCountsFromMeasure(int n, int k, int m) {
        int[] seeds = new int[] { 0, 15, 432, 7362, 11260, 734652, 6473821, 27348245 };
        double[][] measures = new double[seeds.length][];
        for (int i = 0; i < seeds.length; i++) {
            measures[i] = getCountsFromMeasure(n, k, m, seeds[i]);
        }
        return getAvgs(measures);
    }
    
    private static double[] getAvgs(double[][] numbers) {
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
    
    private static double[] getCountsFromMeasure(int n, int k, int m, int seed) {
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

    private static double[] getCountsFromFormula(int n, int k, int m) {
        BinomialDistribution binomialDistribution = new BinomialDistribution(n, 1d / k);
        double[] quantityCounts = new double[m];
        for (int i = 0; i < m; i++) {
            quantityCounts[i] = binomialDistribution.probability(i) * k;
        }
        binomialDistribution.reseedRandomGenerator(1);
        return quantityCounts;
    }

}
