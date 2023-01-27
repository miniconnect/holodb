package hu.webarticum.holodb.core.lab.permutation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutationBitmapMain {
    
    private static final LargeInteger PERMUTATION_SIZE = LargeInteger.of(1000000L);

    private static final LargeInteger PERMUTATION_LARGEST_BYTE_PARTITION_SIZE =
            PERMUTATION_SIZE.divide(LargeInteger.of(0xFF));
    
    private static final LargeInteger SAMPLE_START = LargeInteger.of(100000L);
    
    private static final int ROWS = 419;

    private static final int COLUMNS = 701;
    
    private static final int PIXEL_SIZE = 2;
    

    public static void main(String[] args) {
        Map<String, Function<LargeInteger, Permutation>> permutationFactories =
                PermutationFactorySource.createFactories();
        
        JFrame frame = new JFrame("Permutations");
        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.setContentPane(mainPanel);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        for (Map.Entry<String, Function<LargeInteger, Permutation>> entry : permutationFactories.entrySet()) {
            String name = entry.getKey();
            Function<LargeInteger, Permutation> factory = entry.getValue();
            BufferedImage image = render(factory);
            ImagePanel imagePanel = new ImagePanel(image);
            tabbedPane.addTab(name, imagePanel);
        }
        
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.setVisible(true);
    }
    
    private static BufferedImage render(Function<LargeInteger, Permutation> factory) {
        Permutation permutation = factory.apply(PERMUTATION_SIZE);
        BufferedImage image = new BufferedImage(COLUMNS * PIXEL_SIZE, ROWS * PIXEL_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, COLUMNS * PIXEL_SIZE, ROWS * PIXEL_SIZE);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                int i = (c * ROWS) + r;
                LargeInteger n = LargeInteger.of(i).multiply(LargeInteger.of(PIXEL_SIZE));
                LargeInteger value = permutation.at(SAMPLE_START.add(n));
                Color color = colorForValue(value);
                g.setColor(color);
                g.fillRect(c * PIXEL_SIZE, r * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
            }
        }
        return image;
    }
    
    private static Color colorForValue(LargeInteger value) {
        byte largeByte = value.divide(PERMUTATION_LARGEST_BYTE_PARTITION_SIZE).byteValue();
        byte smallByte = value.byteValue();
        int sum = 0;
        for (byte b : value.toByteArray()) {
            sum += b;
        }
        int byteCount = ((PERMUTATION_SIZE.bitCount() + 7) / 8);
        byte avgByte = (byte) (sum / byteCount);
        return Color.getHSBColor(
                normalize(largeByte, 0.0f, 1.0f),
                normalize(smallByte, 0.2f, 0.8f),
                normalize(avgByte, 0.2f, 0.6f));
    }
    
    private static float normalize(byte b, float start, float size) {
        float v = (float) Byte.toUnsignedInt(b);
        return ((v / 0xFF) * size) + start;
    }
    
}
