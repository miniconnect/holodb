package hu.webarticum.holodb.core.lab.permutation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    
    private final BufferedImage image;
    

    public ImagePanel(BufferedImage image) {
       this.image = image;
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);          
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(), image.getHeight());
    }

}