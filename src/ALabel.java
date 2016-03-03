import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

/**
 * An ALabel is an AComponent that can display text and images.
 * 
 * @author Caleb Rush
 */
public class ALabel extends AComponent {
    // The image to be displayed in the background of the label.
    private BufferedImage                   image;
    // The internal JLabel to delegate to.
    private final JLabel                    label;
    // Whether or not the image should be scaled to the size of the label.
    private boolean                         scalingImage;
    
    /**
     * Constructs a new ALabel instance with default text and no image.
     */
    public ALabel() {
        this("Label");        
    }
    
    /**
     * Constructs a new ALabel instance with the specified text.
     * 
     * The ALabel's default width and height will be determined by the size of
     * the text (drawn with the default font).
     * 
     * @param text the text to be displayed in the ALabel.
     */
    public ALabel(String text) {
        label = new JLabel(text);
        setLayout(new BorderLayout());
        add(label);
    }
    
    /**
     * Constructs a new ALabel instance with no text and the specified image.
     * 
     * @param imagePath the path to the image to be displayed by the label. If
     *                  the path does not point to a valid image file, the label
     *                  will display nothing.
     * @param scalingImage true if the image should be scaled to the size of the
     *                     size of the image or false if it shouldn't.
     */
    public ALabel(String imagePath, boolean scalingImage) {
        this("");
        setImage(imagePath);
        setScalingImage(scalingImage);
    }
    
    @Override
    public Font getFont() {
        return label.getFont();
    }
    
    /**
     * Returns the image displayed by the label.
     * 
     * @return the image displayed by the label.
     */
    public BufferedImage getImage() {
        return image;
    }
    
    /**
     * Returns the text displayed by the label.
     * 
     * @return the label's text.
     */
    public String getText() {
        return label.getText();
    }
    
    /**
     * Returns whether or not the image displayed by the label is scaled to the
     * size of the label.
     * 
     * @return true if the image is scaled, false otherwise.
     */
    public boolean isScalingImage() {
        return scalingImage;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Paint the background image.
        if (image != null)
            if (scalingImage)
                g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            else
                g.drawImage(image, 0, 0, null);
    }
    
    @Override
    public void setFont(Font font) {
        label.setFont(font);
    }
    
    /**
     * Sets the image displayed by the label.
     * 
     * @param imagePath the path to the image file to display. If the path is
     *                  not a valid image file, the image will remain unchanged.
     */
    public void setImage(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the image displayed by the label.
     * 
     * @param image the image to be displayed by the label.
     */
    public void setImage(BufferedImage image) {
        this.image = image;
    }
    
    /**
     * Sets whether or not the image displayed by the label is scaled to the
     * size of the label.
     * 
     * @param scalingImage true if the image should be scaled, false otherwise.
     */
    public void setScalingImage(boolean scalingImage) {
        this.scalingImage = scalingImage;
        repaint();
    }
    
    /**
     * Sets the text displayed by the label.
     * 
     * @param text the text to be displayed by the label.
     */
    public void setText(String text) {
        label.setText(text);
        repaint();
    }
}
