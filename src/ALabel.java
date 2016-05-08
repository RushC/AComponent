import animator.Property;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
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
    private JLabel                          label;
    // Whether or not the image should be scaled to the size of the label.
    private boolean                         scalingImage;
    // The color of the text when the label is highlighted.
    private Color                           highlightTextColor;
    
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
        add(label);
        highlightTextColor = Color.WHITE;
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
    public ALabel enter(int x, int y, int width, int height) {
        setBounds(x, y, 0, height);
        scaleWidth(width);
        return this;
    }
    
    @Override
    public ALabel exit() {
        scaleWidth(0);
        return this;
    }
    
    @Override
    public Font getFont() {
        return label.getFont();
    }
    
    /**
     * Returns the text's highlight color.
     * 
     * @return the color of the text displayed by this label when the highlight
     *         is not empty.
     */
    public Color getHighlightTextColor() {
        return highlightTextColor;
    }
    
    /**
     * Returns the horizontal alignment of the label.
     * 
     * @return the horizontal alignment of the label
     */
    public int getHorizontalAlignment() {
        return label.getHorizontalAlignment();
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
     * Returns the vertical text position of the label.
     * 
     * @return the vertical text position of the label.
     */
    public int getVerticalTextPosition() {
        return label.getVerticalTextPosition();
    }
    
    @Override
    public void highlight() {
        label.setForeground(highlightTextColor);
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
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        label.setBounds(0, 0, width, height);
    }
    
    @Override
    public void setBounds(Rectangle r) {
        super.setBounds(r);
        label.setBounds(0, 0, r.width, r.height);
    }
    
    @Override
    public void setFont(Font font) {
        label.setFont(font);
    }
    
    /**
     * Sets the text's highlight color.
     * 
     * @param highlightTextColor the color the text displayed by the label should
     *                           be when the highlight is not empty.
     */
    public void setHighlightTextColor(Color highlightTextColor) {
        this.highlightTextColor = highlightTextColor;
    }
    
    /**
     * Sets the horizontal alignment of the label.
     * 
     * @param alignment One of the alignment constants defined in SwingConstants.
     */
    public void setHorizontalAlignment(int alignment) {
        label.setHorizontalAlignment(alignment);
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
    
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        label.setSize(width, height);
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
    
    /**
     * Sets the vertical position of the label's text.
     * 
     * @param textPostion one of the vertical alignment constants from 
     *                    SwingConstants.
     */
    public void setVerticalTextPosition(int textPostion) {
        label.setVerticalTextPosition(textPostion);
    }
    
    public ALabel swapText(String text) {
        // Save the label's current Y position.
        int y = label.getY();
        
        // Add a new JLabel with the new text underneath the bottom.
        JLabel newLabel = new JLabel(text);
        newLabel.setLocation(label.getX(), getHeight());
        newLabel.setSize(label.getSize());
        newLabel.setHorizontalAlignment(label.getHorizontalAlignment());
        newLabel.setVerticalAlignment(label.getVerticalAlignment());
        add(newLabel);
        
        // Animate the current JLabel out.
        getAnimatedObject().animateValue(
                y, 
                -label.getHeight(),
                outY -> label.setLocation(label.getX(), (int)outY.value())
        );
        
        // Animate the new JLabel in.
        getAnimatedObject().animateValue(
                getHeight(), 
                y, 
                inY -> newLabel.setLocation(newLabel.getX(), (int)inY.value())
        );
        
        // Once the animations are finished...
        getAnimatedObject().then(() -> {
            // Remove the old label.
            remove(label);
            // Set the new label as the current label.
            label = newLabel;
        });
        
        return this;
    }
    
    @Override
    public void unhighlight() {
        label.setForeground(getForeground());
    }
}
