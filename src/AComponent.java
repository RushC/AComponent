import animator.AnimationEndListener;
import animator.interpolator.Interpolator;
import animator.interpolator.LinearInterpolator;
import animator.Property;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.stream.Stream;

import javax.swing.JComponent;

/**
 * An AComponent is a type of JPanel that utilizes the Animator to animate
 * many of its properties, allowing it to animate changes in its size and
 * location, among other things.
 * 
 * @author Caleb Rush
 */
public class AComponent extends JComponent {
    // The list of Properties currently being animated.
    private final ArrayList<Property>       properties;
    // The default amount of time each animation should last for.
    private long                            animationDuration;
    // The default Interpolator to use for each animation.
    private Interpolator                    interpolator;
    // The AnimationEndListener used by all properties to remove themselves
    // from the properties list when their animations complete.
    private final AnimationEndListener      propertyRemover;
    // The color of the background (overriden from JPanel due to needing to
    // keep the component transparent.
    private Color                           background;
    // The highlight is drawn between the background and the content.
    private Shape                           highlight;
    private Color                           highlightColor;
    // The number of radians to rotate the panel.
    private double                          rotation;
    // The shape of the entire panel.
    private RectangularShape                shape;
    
    
    /**
     * Constructs a new AComponent instance that uses a LinearInterpolator
     * and performs animations for 400 seconds by default.
     */
    public AComponent() {
        properties = new ArrayList<>();
        animationDuration = 400;
        interpolator = new LinearInterpolator();
        propertyRemover = property -> {
            // Ensure concurrent modifications don't happen.
            synchronized(properties) {
                properties.remove(property);
            }
        };
        highlightColor = Color.WHITE;
        shape = new Rectangle();
        
        setOpaque(false);
    }
    
    /**
     * Blocks the current thread until all of the AComponent's animations
     * complete.
     * 
     * If the AComponent is not currently animating anything, this method will
     * return immediately.
     * 
     * This method should never be called from the event thread, as stopping
     * the event thread will prevent all events from being processed until
     * the animation is complete. In most applications of an animation, this
     * will mean that any modifications to the GUI will not appear until after
     * the animation, meaning the animation will not work.
     * 
     * @return this AComponent instance for chaining method calls.
     */
    public AComponent await() {
        await(0);
        return this;
    }
    
    /**
     * Blocks the current thread until all of the AComponent's animations
     * complete.
     * 
     * If the AComponent is not currently animating anything, this method will
     * return immediately.
     * 
     * This method should never be called from the event thread, as stopping
     * the event thread will prevent all events from being processed until
     * the animation is complete. In most applications of an animation, this
     * will mean that any modifications to the GUI will not appear until after
     * the animation, meaning the animation will not work.
     * 
     * @param milliseconds the amount of extra time to delay after the
     *                     animations are finished.
     * @return this AComponent instance for chaining method calls.
     */
    public AComponent await(long milliseconds) {
        // Only leave the loop when it is broken out of.
        while (true) {
            // Check if there are still Properties being animated.
            synchronized (properties) {
                if (properties.isEmpty())
                    break;
            }
        }
        
        // Sleep for the specified amount of extra time.
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return this;
    }
    
    /**
     * Fills the highlight by animating it towards the highlight color from
     * the background color.
     * 
     * This animation will ultimately fill the component's highlight. If the
     * highlight is already filled before calling this method, the initial
     * change to the highlight may appear jarring. For best results, ensure
     * the highlight is empty before calling a filling animation.
     * 
     * @return this AComponent instance for chaining method calls.
     */
    public AComponent fade() {        
        // Create a property to represent the color's red value.
        Property r= new Property(
                // Animate from the start color to the end color.
                background.getRed(), highlightColor.getRed(),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new color with the given red value every increment.
                p -> highlightColor = new Color(
                        (int)p.value(), 
                        highlightColor.getGreen(), 
                        highlightColor.getBlue()
                ),
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Create a property to represent the color's green value.
        Property g = new Property(
                // Animate from the start color to the end color.
                background.getGreen(), highlightColor.getGreen(),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new color with the given red value every increment.
                p -> highlightColor = new Color(
                        highlightColor.getRed(),
                        (int)p.value(), 
                        highlightColor.getBlue()
                ),
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Create a property to represent the color's blue value.
        Property b = new Property(
                // Animate from the start color to the end color.
                background.getBlue(), highlightColor.getBlue(),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new color with the given red value every increment.
                p -> {
                    highlightColor = new Color(
                        highlightColor.getRed(), 
                        highlightColor.getGreen(),
                        (int)p.value()
                    );
                    highlight = new Rectangle(0, 0, getWidth(), getHeight());
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the properties to the Properties list.
        synchronized(properties) {
            properties.add(r);
            properties.add(g);
            properties.add(b);
        }
        
        // Begin the Properties' animations.
        r.animate();
        g.animate();
        b.animate();
        
        return this;
    }
    
    /**
     * Returns the duration that new animations will last for.
     * 
     * @return the animation duration in milliseconds
     */
    public long getAnimationDuration() {
        return animationDuration;
    }
    
    @Override
    public Color getBackground() {
        return background;
    }
    
    /**
     * Retrieves the shape that the highlight is drawn as.
     * 
     * @return the highlight's shape.
     */
    public Shape getHighlight() {
        return highlight;
    }
    
    /**
     * Retrieves the color of the highlight.
     * 
     * @return the color of the highlight
     */
    public Color getHighlightColor() {
        return highlightColor;
    }
    
    /**
     * Returns the Interpolator that new animations will use.
     * 
     * @return the Interpolator that new animations will use.
     */
    public Interpolator getInterpolator() {
        return interpolator;
    }
    
    /**
     * Retrieves the rotation of the component.
     * 
     * @return the number of radians the component is rotated clockwise.
     */
    public double getRotation() {
        return rotation;
    }
    
    /**
     * Retrieves the shape that this component displays when it is drawn.
     * 
     * @return the component's shape.
     */
    public RectangularShape getShape() {
        return shape;
    }
    
    /**
     * A wrapper method for setAnimationDuration that allows method chaining.
     * 
     * @param animationDuration a positive number of milliseconds animations
     *                          should last for.
     * @return this AComponent instance for method chaining.
     * @see AComponent#setAnimationDuration
     */
    public AComponent lasting(long animationDuration) {
        setAnimationDuration(animationDuration);
        return this;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Set the clip to the appropriately scaled shape.
        shape.setFrame(0, 0, getWidth(), getHeight());
        g.setClip(shape);
        
        // Rotate the graphics on the center point.
        ((Graphics2D)g).rotate(
                rotation, 
                getWidth() / 2 + getWidth() % 2, 
                getHeight() / 2 + getHeight() % 2
        );
        
        // Draw the background.
        if (background != null) {
            g.setColor(background);
            g.fillRect(0, 0, getWidth(), getHeight());    
        }
        
        // Draw the highlight.
        if (highlight != null) {
            g.setColor(highlightColor);
            ((Graphics2D)g).fill(highlight);
        }
        
        // Draw the content.
        super.paintComponent(g);
    }
    
    /**
     * Animates the highlight as a circle expanding to cover the component's
     * area.
     * 
     * This animation will ultimately fill the component's highlight. If the
     * highlight is already filled before calling this method, the initial
     * change to the highlight may appear jarring. For best results, ensure
     * the highlight is empty before calling a filling animation.
     * 
     * @return this AComponent instance for chaining method calls.
     * @see AComponent#unpulse() 
     */
    public AComponent pulse() {
        pulse(getWidth() / 2 + getWidth() % 2, 
                getHeight() / 2 + getHeight() % 2);
        return this;
    }
    
    /**
     * Animates the highlight as a circle expanding to cover the component's
     * area.
     * 
     * This animation will ultimately fill the component's highlight. If the
     * highlight is already filled before calling this method, the initial
     * change to the highlight may appear jarring. For best results, ensure
     * the highlight is empty before calling a filling animation.
     * 
     * @param x the X position the highlight should start expanding from.
     * @param y the Y position the highlight should start expanding from.
     * @return this AComponent instance for chaining method calls.
     * @see AComponent#unpulse() 
     */
    public AComponent pulse(int x, int y) {
        pulse(x, y, 0);
        return this;
    }
    
    /**
     * Animates the highlight as a circle expanding to cover the component's
     * area.
     * 
     * This animation will ultimately fill the component's highlight. If the
     * highlight is already filled before calling this method, the initial
     * change to the highlight may appear jarring. For best results, ensure
     * the highlight is empty before calling a filling animation.
     * 
     * @param x the X position the highlight should start expanding from.
     * @param y the Y position the highlight should start expanding from.
     * @param startRadius the initial radius of the highlight.
     * @return this AComponent instance for chaining method calls.
     * @see AComponent#unpulse() 
     */
    public AComponent pulse(int x, int y, int startRadius) {
        // Calculate the final value of the radius by determining the distance
        // from the given starting position to the furthest corner.
        int endRadius = Stream.of(
                    // Top left corner
                    Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)),
                    // Bottom right corner
                    Math.sqrt(Math.pow(x, 2) + Math.pow(getHeight() - y, 2)),
                    // Top right corner
                    Math.sqrt(Math.pow(getWidth() - x, 2) + Math.pow(y, 2)),
                    // Bottom right corner
                    Math.sqrt(Math.pow(getWidth() - x, 2) 
                            + Math.pow(getHeight() - y, 2))
                ).max(Double::compare).get().intValue() + 1;
        
        // Create a property to represent the highlight's radius.
        Property property = new Property(
                // Animate from the start radius to the end radius.
                startRadius, endRadius,
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new circle with the given radius every increment.
                p -> {
                    highlight = new Ellipse2D.Double(
                        x - p.value(), 
                        y - p.value(), 
                        2 * p.value(), 
                        2 * p.value()
                    );
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Animates the AComponent towards the specified bounds.
     * 
     * @param x the X position to move the AComponent towards.
     * @param y the Y position to move the AComponent towards.
     * @param width the width to move the AComponent towards.
     * @param height the height to move the AComponent towards.
     * @return this AComponent instance for method chaining.
     */
    public AComponent transform(int x, int y, int width, int height) {
        translate(x, y);
        scale(width, height);
        return this;
    }
    
    /**
     * Animates the AComponent towards the specified bounds.
     * 
     * @param r the bounds to move the AComponent towards.
     * @return this AComponent instance for method chaining. 
     */
    public AComponent transform(Rectangle r) {
        AComponent.this.transform(r.x, r.y, r.width, r.height);
        return this;
    }
    
    /**
     * Sets the duration in milliseconds that all subsequently started
     * animations will last for.
     * 
     * @param animationDuration a positive number of milliseconds animations 
     *                          should last.
     * @throws IllegalArgumentException if animationDuration is not positive.
     */
    public void setAnimationDuration(long animationDuration) 
            throws IllegalArgumentException{
        // Ensure the animationDuration is positive.
        if (animationDuration <= 0)
            throw new IllegalArgumentException("The animation duration must be "
                    + "positive!");
        
        this.animationDuration = animationDuration;
    }
    
    @Override
    public void setBackground(Color background) {
        this.background = background;
        repaint();
    }
    
    /**
     * Sets radius of the component's corners which will determine how rounded
     * they are.
     * 
     * @param cornerRadius the radius to make the component's corners. If this
     *                     value is greater than or equal to half of the 
     *                     component's width and height, the component will
     *                     be drawn as a circle.
     */
    public void setCornerRadius(int cornerRadius) {
        shape = getWidth() == getHeight() && cornerRadius >= getWidth() / 2.0?
                new Ellipse2D.Double():
                new RoundRectangle2D.Double(0, 0, 0, 0,cornerRadius, cornerRadius);
    }
    
    /**
     * Sets the color of the highlight.
     * 
     * The highlight is the shape that is drawn between the background and the
     * content of the component. It is used in some animations to show feedback
     * to user interaction.
     * 
     * @param highlightColor the new color of the highlight
     */
    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
        repaint();
    }
    
    /**
     * Sets the Interpolator that all subsequently started animations will use.
     * 
     * @param interpolator the Interpolator animations should use
     * @see Interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }
    
    /**
     * Sets the rotation of the component.
     * 
     * @param rotation the number of radians the component should be rotated
     *                 clockwise.
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
        repaint();
    }
    
    /**
     * Sets the rotation of the component.
     * 
     * @param rotation the number of degrees the component should be rotated
     *                 clockwise.
     */
    public void setRotationDegrees(double rotation) {
        setRotation(Math.toRadians(rotation));
    }
    
    /**
     * Moves the AComponent the specified distance. The component will move
     * in a straight line regardless of the angle.
     * 
     * @param deltaX the distance to move the AComponent horizontally.
     *               A positive value will move the component right while a 
     *               negative value will move it left.
     * @param deltaY the distance to move the AComponent vertically.
     *               A positive value will move the component down while a 
     *               negative value will move it up.
     * @return 
     */
    public AComponent slide(int deltaX, int deltaY) {
        translate(getX() + deltaX, getY() + deltaY);
        return this;
    }
    
    /**
     * Moves the AComponent the specified horizontal distance.
     * 
     * @param deltaX the distance to move the AComponent horizontally.
     *               A positive value will move the component right while a 
     *               negative value will move it left.
     * @return this AComponent instance for method chaining.
     */
    public AComponent slideX(int deltaX) {
        translateX(getX() + deltaX);
        return this;
    }
    
    /**
     * Moves the AComponent the specified vertical distance.
     * 
     * @param deltaY the distance to move the AComponent vertically.
     *               A positive value will move the component down while a 
     *               negative value will move it up.
     * @return this AComponent instance for method chaining.
     */
    public AComponent slideY(int deltaY) {
        translateY(getY() + deltaY);
        return this;
    }
    
    /**
     * Rotates the AComponent to the specified angle.
     * 
     * @param rotation the number of degrees clockwise the AComponent should rotate
     *                 towards. Whether or not the component will rotate clockwise
     *                 or counterclockwise depends on if the new rotation is
     *                 larger or smaller than the old value respectively.
     * @return this AComponent instance for method chaining.
     */
    public AComponent rotate(double rotation) {
        // Create a new Property to represent the rotation.
        Property property = new Property(
                // Animate from the current rotation to the specified rotation.
                this.rotation, Math.toRadians(rotation),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Update the rotation on each increment.
                p -> setRotation(p.value()),
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Expands or condenses the AComponent towards the specified size.
     * 
     * @param width the width the AComponent should move towards.
     * @param height the height the AComponent should move towards.
     * @return this AComponent instance for method chaining.
     */
    public AComponent scale(int width, int height) {
        scaleWidth(width);
        scaleHeight(height);
        return this;
    }
    
    /**
     * Expands or condenses the AComponent towards the specified size without
     * changing the component's center position.
     * 
     * @param width the width the AComponent should move towards.
     * @param height the height the AComponent should move towards.
     * @return this AComponent instance for method chaining.
     */
    public AComponent scaleCentered(int width, int height) {
        scaleWidthCentered(width);
        scaleHeightCentered(height);
        return this;
    }
    
    /**
     * Expands or condenses the AComponent vertically towards the specified
     * height.
     * 
     * @param height the height the AComponent should move towards.
     * @return this AComponent instance for method chaining.
     */
    public AComponent scaleHeight(int height) {
        // Create a new Property to represent the height.
        Property property = new Property(
                // Animate from the current height to the specified height.
                getHeight(), height,
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Update the X position on each increment.
                p -> setSize(getWidth(), (int)p.value()),
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Expands or condenses the AComponent vertically towards the specified
     * height without changing the component's center position.
     * 
     * @param height the height the AComponent should move towards.
     * @return this AComponent instance for method chaining.
     */
    public AComponent scaleHeightCentered(int height) {
        scaleHeight(height);
        translateY((int)(getY() - (height - getHeight()) / 2.0));
        return this;
    }
    
    /**
     * Expands or condenses the AComponent horizontally towards the specified
     * width.
     * 
     * @param width the width the AComponent should move towards.
     * @return this AComponent instance for method chaining.
     */
    public AComponent scaleWidth(int width) {
        // Create a new Property to represent the width.
        Property property = new Property(
                // Animate from the current width to the specified width.
                getWidth(), width,
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Update the X position on each increment.
                p -> setSize((int)p.value(), getHeight()),
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Expands or condenses the AComponent horizontally towards the specified
     * width without changing the component's center position.
     * 
     * @param width the width the AComponent should move towards.
     * @return this AComponent instance for method chaining.
     */
    public AComponent scaleWidthCentered(int width) {
        scaleWidth(width);
        translateX((int)(getX() - (width - getWidth()) / 2.0));
        return this;
    }
    
    /**
     * Moves the AComponent horizontally to the specified X position.
     * 
     * @param x the X position to move the AComponent towards
     * @return this AComponent instance for chaining method calls
     */
    public AComponent translateX(int x) {
        // Create a new Property to represent the X value.
        Property property = new Property(
                // Animate from the current X position to the specified position.
                getX(), x,
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Update the X position on each increment.
                p -> setLocation((int)p.value(), getY()),
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Moves the AComponent in a straight line to the specified location.
     * 
     * @param x the X position to move the AComponent towards.
     * @param y the Y position to move the AComponent towards.
     * @return this AComponent instance for method chaining.
     */
    public AComponent translate(int x, int y) {
        translateX(x);
        translateY(y);
        
        return this;
    }
    
    /**
     * Moves the AComponent in a straight line to the specified location.
     * 
     * @param p the location to move the AComponent towards
     * @return this AComponent instance for method chaining.
     */
    public AComponent translate(Point p) {
        translate((int)p.getX(), (int)p.getY());
        return this;
    }
    
     /**
     * Moves the AComponent vertically to the specified Y position.
     * 
     * @param y the Y position to move the AComponent towards
     * @return this AComponent instance for chaining method calls
     */
    public AComponent translateY(int y) {
        // Create a new Property to represent the Y value.
        Property property = new Property(
                // Animate from the current X position to the specified position.
                getY(), y,
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Update the X position on each increment.
                p -> setLocation(getX(), (int)p.value()),
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Animates the highlight by slowly changing it to the background color.
     * 
     * This animation will ultimately empty the component's highlight. If the
     * highlight is empty prior to calling this method, the initial effect of
     * the animation will appear jarring. For best results, ensure that the 
     * highlight is filled before calling an emptying animation.
     * 
     * @return this AComponent instance for chaining method calls.
     */
    public AComponent unfade() {
        // Create a property to represent the color's red value.
        Property r= new Property(
                // Animate from the start color to the end color.
                highlightColor.getRed(), background.getRed(),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new color with the given red value every increment.
                p -> highlightColor = new Color(
                        (int)p.value(), 
                        highlightColor.getGreen(), 
                        highlightColor.getBlue()
                ),
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Create a property to represent the color's green value.
        Property g = new Property(
                // Animate from the start color to the end color.
                highlightColor.getGreen(), background.getGreen(),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new color with the given red value every increment.
                p -> highlightColor = new Color(
                        highlightColor.getRed(),
                        (int)p.value(), 
                        highlightColor.getBlue()
                ),
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Create a property to represent the color's blue value.
        Property b = new Property(
                // Animate from the start color to the end color.
                highlightColor.getBlue(), background.getBlue(),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new color with the given red value every increment.
                p -> {
                    highlightColor = new Color(
                        highlightColor.getRed(), 
                        highlightColor.getGreen(),
                        (int)p.value()
                    );
                    highlight = new Rectangle(0, 0, getWidth(), getHeight());
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the properties to the Properties list.
        synchronized(properties) {
            properties.add(r);
            properties.add(g);
            properties.add(b);
        }
        
        // Begin the Properties' animations.
        r.animate();
        g.animate();
        b.animate();
        
        return this;
    }
    
    /**
     * Animates the highlight by shrinking the filled highlight into a circle.
     * 
     * This animation will ultimately empty the component's highlight. If the
     * highlight is empty prior to calling this method, the initial effect of
     * the animation will appear jarring. For best results, ensure that the 
     * highlight is filled before calling an emptying animation.
     * 
     * @return this AComponent instance for chaining method calls.
     * @see AComponent#pulse() 
     */
    public AComponent unpulse() {
        unpulse(getWidth() / 2 + getWidth() % 2, 
                getHeight() / 2 + getHeight() % 2);
        return this;
    }
    
    /**
     * Animates the highlight by shrinking the filled highlight into a circle.
     * 
     * This animation will ultimately empty the component's highlight. If the
     * highlight is empty prior to calling this method, the initial effect of
     * the animation will appear jarring. For best results, ensure that the 
     * highlight is filled before calling an emptying animation.
     * 
     * @param x the X position to shrink the highlight towards.
     * @param y the Y position to shrink the highlight towards.
     * @return this AComponent instance for chaining method calls.
     * @see AComponent#pulse() 
     */
    public AComponent unpulse(int x, int y) {
        // Calculate the initial value of the radius by determining the distance
        // from the given ending position to the furthest corner.
        int startRadius = Stream.of(
                    // Top left corner
                    Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)),
                    // Bottom right corner
                    Math.sqrt(Math.pow(x, 2) + Math.pow(getHeight() - y, 2)),
                    // Top right corner
                    Math.sqrt(Math.pow(getWidth() - x, 2) + Math.pow(y, 2)),
                    // Bottom right corner
                    Math.sqrt(Math.pow(getWidth() - x, 2) 
                            + Math.pow(getHeight() - y, 2))
                ).max(Double::compare).get().intValue() + 1;
        
        // Create a property to represent the highlight's radius.
        Property property = new Property(
                // Animate from the start radius to 0.
                startRadius, 0,
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new circle with the given radius every increment.
                p -> {
                    highlight = new Ellipse2D.Double(
                        x - p.value(), 
                        y - p.value(), 
                        2 * p.value(), 
                        2 * p.value()
                    );
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Animates the highlight by condensing a rectangle downwards to empty
     * the highlight.
     * 
     * This animation will ultimately empty the component's highlight. If the
     * highlight is empty prior to calling this method, the initial effect of
     * the animation will appear jarring. For best results, ensure that the 
     * highlight is filled before calling an emptying animation.
     * 
     * @return this AComponent instance for method chaining.
     */
    public AComponent unwashDown() {
        // Create a property to represent the highlight's height.
        Property property = new Property(
                // Animate from 0 to the component height.
                getHeight(), 0,
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new rectangle every increment.
                p -> {
                    highlight = new Rectangle(
                        0, 
                        getHeight() - (int)p.value(), 
                        getWidth(), 
                        (int)p.value()
                    );
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Animates the highlight by condensing a rectangle to the left to empty
     * the highlight.
     * 
     * This animation will ultimately empty the component's highlight. If the
     * highlight is empty prior to calling this method, the initial effect of
     * the animation will appear jarring. For best results, ensure that the 
     * highlight is filled before calling an emptying animation.
     * 
     * @return this AComponent instance for method chaining.
     */
    public AComponent unwashLeft() {
        // Create a property to represent the highlight's width.
        Property property = new Property(
                // Animate from 0 to the component width.
                getWidth(), 0,
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new rectangle every increment.
                p -> {
                    highlight = new Rectangle(
                        0, 
                        0, 
                        (int)p.value(), 
                        getHeight()
                    );
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Animates the highlight by condensing a rectangle to the right to empty
     * the highlight.
     * 
     * This animation will ultimately empty the component's highlight. If the
     * highlight is empty prior to calling this method, the initial effect of
     * the animation will appear jarring. For best results, ensure that the 
     * highlight is filled before calling an emptying animation.
     * 
     * @return this AComponent instance for method chaining.
     */
    public AComponent unwashRight() {
        // Create a property to represent the highlight's width.
        Property property = new Property(
                // Animate from the component width to 0.
                getWidth(), 0,
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new rectangle every increment.
                p -> {
                    highlight = new Rectangle(
                        getWidth() - (int)p.value(), 
                        0, 
                        (int)p.value(), 
                        getHeight()
                    );
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Animates the highlight by condensing a rectangle upwards to empty the
     * highlight.
     * 
     * This animation will ultimately empty the component's highlight. If the
     * highlight is empty prior to calling this method, the initial effect of
     * the animation will appear jarring. For best results, ensure that the 
     * highlight is filled before calling an emptying animation.
     * 
     * @return this AComponent instance for method chaining.
     */
    public AComponent unwashUp() {
        // Create a property to represent the highlight's height.
        Property property = new Property(
                // Animate from 0 to the component height.
                getHeight(), 0,
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new rectangle every increment.
                p -> {
                    highlight = new Rectangle(
                        0, 
                        0, 
                        getWidth(), 
                        (int)p.value()
                    );
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Animates the highlight by expanding a rectangle to the right to fill
     * the component.
     * 
     * This animation will ultimately fill the component's highlight. If the
     * highlight is already filled before calling this method, the initial
     * change to the highlight may appear jarring. For best results, ensure
     * the highlight is empty before calling a filling animation.
     * 
     * @return this AComponent instance for method chaining.
     */
    public AComponent washDown() {
        // Create a property to represent the highlight's height.
        Property property = new Property(
                // Animate from 0 to the component height.
                0, getHeight(),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new rectangle every increment.
                p -> {
                    highlight = new Rectangle(
                        0, 
                        0, 
                        getWidth(), 
                        (int)p.value()
                    );
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Animates the highlight by expanding a rectangle to the left to fill
     * the component.
     * 
     * This animation will ultimately fill the component's highlight. If the
     * highlight is already filled before calling this method, the initial
     * change to the highlight may appear jarring. For best results, ensure
     * the highlight is empty before calling a filling animation.
     * 
     * @return this AComponent instance for method chaining.
     */
    public AComponent washLeft() {
        // Create a property to represent the highlight's width.
        Property property = new Property(
                // Animate from 0 to the component width.
                0, getWidth(),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new rectangle every increment.
                p -> {
                    highlight = new Rectangle(
                        getWidth() - (int)p.value(), 
                        0, 
                        (int)p.value(), 
                        getHeight()
                    );
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Animates the highlight by expanding a rectangle to the right to fill
     * the component.
     * 
     * This animation will ultimately fill the component's highlight. If the
     * highlight is already filled before calling this method, the initial
     * change to the highlight may appear jarring. For best results, ensure
     * the highlight is empty before calling a filling animation.
     * 
     * @return this AComponent instance for method chaining.
     */
    public AComponent washRight() {
        // Create a property to represent the highlight's width.
        Property property = new Property(
                // Animate from 0 to the component width.
                0, getWidth(),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new rectangle every increment.
                p -> {
                    highlight = new Rectangle(
                        0, 
                        0, 
                        (int)p.value(), 
                        getHeight()
                    );
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * Animates the highlight by expanding a rectangle upwards to fill
     * the component.
     * 
     * This animation will ultimately fill the component's highlight. If the
     * highlight is already filled before calling this method, the initial
     * change to the highlight may appear jarring. For best results, ensure
     * the highlight is empty before calling a filling animation.
     * 
     * @return this AComponent instance for method chaining.
     */
    public AComponent washUp() {
        // Create a property to represent the highlight's height.
        Property property = new Property(
                // Animate from 0 to the component height.
                0, getHeight(),
                // Use the current animation duration and interpolator.
                animationDuration, interpolator,
                // Create a new rectangle every increment.
                p -> {
                    highlight = new Rectangle(
                        0, 
                        getHeight() - (int)p.value(), 
                        getWidth(), 
                        (int)p.value()
                    );
                    repaint();
                },
                // Have the Property remove itself when it is finished.
                propertyRemover
        );
        
        // Add the Property to the Properties list.
        synchronized(properties) {
            properties.add(property);
        }
        
        // Begin the Property's animation.
        property.animate();
        
        return this;
    }
    
    /**
     * A wrapper method for setInterpolator that allows for method chaining.
     * 
     * @param interpolator an Interpolator that animations should use.
     * @return this AComponent method for method chaining.
     * @see AComponent#setInterpolator
     */
    public AComponent using(Interpolator interpolator) {
        setInterpolator(interpolator);
        return this;
    }
}
