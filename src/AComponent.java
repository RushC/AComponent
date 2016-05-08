import animator.AnimatedObject;
import animator.AnimationEndListener;
import animator.interpolator.Interpolator;
import animator.interpolator.LinearInterpolator;
import animator.Property;

import java.awt.Color;
import java.awt.Container;
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
    // The animated object that is used to animate this object.
    private final AnimatedObject            animatedObject;
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
        animatedObject = new AnimatedObject(this);
        setAnimationDuration(400);
        setInterpolator(new LinearInterpolator());
        highlightColor = Color.WHITE;
        shape = new Rectangle();
        
        setOpaque(false);
    }
    
    /**
     * Animates the AComponent's corners towards the specified corner radius.
     * 
     * @param cornerRadius the corner radius that the AComponent should animate
     *                     towards. Keep in mind that if this value ever reaches
     *                     half the component's width and height it will keep
     *                     the shape as an ellipse.
     * @return this AComponent instance for chaining method calls.
     */
    public AComponent adjust(int cornerRadius) {
        // Retrieve the current corner radius if there is one.
        double currentRadius = shape instanceof RoundRectangle2D?
                ((RoundRectangle2D)shape).getArcHeight():
                0;

        animatedObject.animateValue("cornerRadius", currentRadius, cornerRadius);
        
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
        animatedObject.await(milliseconds);
        return this;
    }
    
    /**
     * Performs an entrance animation by animating the component from its center
     * point with no size to its full bounds.
     * 
     * @return this AComponent instance for method chaining.
     */
    public AComponent enter() {
        return enter(getBounds());
    }
    
    /**
     * Performs an entrance animation by animating the component from the center
     * point of the specified bounds with no size to the specified bounds.
     * 
     * If a subclass wants to specify a custom entrance animation, this is the
     * method it should override, as every other enter overload calls this
     * method.
     * 
     * @param x the X position the component should enter on.
     * @param y the Y position the component should enter on.
     * @param width the width the component should enter to.
     * @param height the height the component should enter to.
     * @return this AComponent instance for method chaining.
     */
    public AComponent enter(int x, int y, int width, int height) {
        // Make the component initially invisible.
        setBounds(
                x + width / 2 + width % 2,
                y + height / 2 + height % 2,
                0,
                0
        );
        
        // Animate the component to the specified size.
        return scaleCentered(width, height);
    }
    
    /**
     * Performs an entrance animation by animating the component from the center
     * point of the specified bounds with no size to the specified bounds.
     * 
     * @param bounds the bounds the component should enter on.
     * @return this AComponent instance for method chaining.
     */
    public AComponent enter(Rectangle bounds) {
        return enter(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    /**
     * Performs an entrance animation after adding the component to the specified
     * parent.
     * 
     * @param parent the component this component should be added to.
     * @return this AComponent instance for chaining method calls.
     */
    public AComponent enter(Container parent) {
        parent.add(this);
        return enter();
    }
    
    /**
     * Performs an entrance animation after adding the component to the specified
     * parent.
     * 
     * @param parent the component this component should be added to.
     * @param x the X position the component should enter on.
     * @param y the Y position the component should enter on.
     * @param width the width the component should enter to.
     * @param height the height the component should enter to.
     * @return this AComponent instance for chaining method calls.
     */
    public AComponent enter(Container parent, int x, int y, int width, int height) {
        parent.add(this);
        return enter(x, y, width, height);
    }
    
    /**
     * Performs an entrance animation after adding the component to the specified
     * parent.
     * 
     * @param parent the component this component should be added to.
     * @param bounds the bounds the component should enter on.
     * @return this AComponent instance for chaining method calls.
     */
    public AComponent enter(Container parent, Rectangle bounds) {
        parent.add(this);
        return enter(bounds);
    }
    
    /**
     * Performs an exit animation by shrinking the component towards its center
     * with zero size. This animation will not actually remove the component
     * from its parent.
     * 
     * If a subclass wishes to implement its own exit animation, this is the
     * method it should override.
     * 
     * @return this AComponent instance for chaining method calls.
     */
    public AComponent exit() {
        return scaleCentered(0, 0);
    }
    
    /**
     * Performs an exit animation and then removes the component from the 
     * specified parent.
     * 
     * @param parent the parent container that contains this component.
     * @return this AComponent instance for chaining method calls.
     */
    public AComponent exit(Container parent) {
        exit();
        then(() -> parent.remove(AComponent.this));
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
        // Animate the red value of the background color to the highlight color.
        animatedObject.animateValue(
                background.getRed(), 
                highlightColor.getRed(),
                r -> {
                    highlightColor = new Color(
                            (int)r.value(),
                            highlightColor.getGreen(),
                            highlightColor.getBlue()
                    );
                    highlight = new Rectangle(0, 0, getWidth(), getHeight());
                    repaint();
                }
        );
        
        // Animate the blue value of the background color to the highlight color.
        animatedObject.animateValue(
                background.getBlue(), 
                highlightColor.getBlue(),
                g -> {
                    highlightColor = new Color(
                            highlightColor.getRed(),
                            (int)g.value(),
                            highlightColor.getBlue()
                    );
                    highlight = new Rectangle(0, 0, getWidth(), getHeight());
                    repaint();
                }
        );
        
        // Animate the green value of the background color to the highlight color.
        animatedObject.animateValue(
                background.getGreen(), 
                highlightColor.getGreen(),
                b -> {
                    highlightColor = new Color(
                            highlightColor.getRed(),
                            highlightColor.getGreen(),
                            (int)b.value()
                    );
                    highlight = new Rectangle(0, 0, getWidth(), getHeight());
                    repaint();
                }
        );
        
        // Notify subclasses that the component is being highlighted.
        highlight();
        
        return this;
    }
    
    /**
     * Returns the AnimatedObject instance used to animate this object.
     * 
     * This should be used by subclasses to easily create new animations that
     * will run in perfect parallel with the already defined animations.
     * 
     * @return an AnimatedObject instance.
     */
    protected AnimatedObject getAnimatedObject() {
        return animatedObject;
    }
    
    /**
     * Returns the duration that new animations will last for.
     * 
     * @return the animation duration in milliseconds
     */
    public long getAnimationDuration() {
        return animatedObject.getAnimationDuration();
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
        return animatedObject.getInterpolator();
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
     * Called when the component's highlight is being filled.
     */
    protected void highlight() {}
    
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
        // Set the clip to the rotation adjusted bounds.
        shape.setFrame(
                rotatedX() - getX(),
                rotatedY() - getY(),
                rotatedWidth(),
                rotatedHeight()
        );
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
        
        // Animate the highlight's radius from the startRadius to the calculated
        // end radius.
        animatedObject.animateValue(
                startRadius, 
                endRadius, 
                r -> {
                    // Create a new circle with the radius at every increment.
                    highlight = new Ellipse2D.Double(
                            x - r.value(),
                            y - r.value(),
                            2 * r.value(),
                            2 * r.value()
                    );
                    repaint();
                }
        );
        
        // Notify subclasses that the component is being highlighted.
        highlight();
        
        return this;
    }
    
    @Override
    public void repaint() {
        if (getParent() != null) {
            getParent().repaint();
            return;
        }
        
        super.repaint();
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
        animatedObject.setAnimationDuration(animationDuration);
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
     *                     value is greater than or equal to the 
     *                     component's width and height, the component will
     *                     be drawn as a circle.
     */
    public void setCornerRadius(double cornerRadius) {
        shape = getWidth() == getHeight() && cornerRadius >= getWidth()?
                new Ellipse2D.Double():
                new RoundRectangle2D.Double(0, 0, 0, 0, cornerRadius, cornerRadius);
        
        repaint();
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
        animatedObject.setInterpolator(interpolator);
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
        animatedObject.animateValue("rotation", Math.toRadians(rotation));     
        return this;
    }
    
    /**
     * Calculates the bounds of the component adjusted for its rotation.
     * 
     * @return the calculated bounds.
     */
    public Rectangle rotatedBounds() {
        return new Rectangle(
                rotatedX(), 
                rotatedY(), 
                rotatedHeight(), 
                rotatedWidth()
        );
    }
    
    /**
     * Calculates the height of the component adjusted for its rotation.
     * 
     * @return the calculated height.
     */
    public int rotatedHeight() {
        double rotatedHeight = getHeight() * Math.abs(Math.cos(rotation))
                + getHeight() * Math.abs(Math.sin(rotation));
        
        return (int)rotatedHeight;
    }
    
    
    /**
     * Calculates the width of the component adjusted for its rotation.
     * 
     * @return the calculated width.
     */
    public int rotatedWidth() {
        double rotatedWidth = getWidth() * Math.abs(Math.cos(rotation))
                + getHeight() * Math.abs(Math.sin(rotation));
        
        return (int)rotatedWidth;
    }
    
    /**
     * Calculates the X position of the component adjusted for its rotation.
     * 
     * @return the calculated X position.
     */
    public int rotatedX() {
        return getX() + (getWidth() - rotatedWidth()) / 2;
    }
    
    /**
     * Calculates the Y position of the component adjusted for its rotation.
     * 
     * @return the calculated Y position.
     */
    public int rotatedY() {
        return getY() + (getHeight() - rotatedHeight()) / 2;
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
        animatedObject.animateValue(
                getHeight(), 
                height, 
                h -> setSize(getWidth(), (int)h.value())
        );

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
        animatedObject.animateValue(
                getWidth(), 
                width, 
                w -> setSize((int)w.value(), getHeight())
        );
        
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
     * Runs the specified callback runnable in a new thread after the most 
     * recently started animation has finished.
     * 
     * This method is asynchronous and therefore returns immediately.
     * 
     * @param callback a Runnable whose run method will be called when all
     *                 animations finish running.
     * @return this AComponent instance for method chaining.
     * @see AComponent#await() 
     */
    public AComponent then(Runnable callback) {
        return then(callback, 0);
    }
    
    /**
     * Runs the specified callback runnable in a new thread after a specified
     * amount of time after the most recently started animation has finished.
     * 
     * This method is asynchronous and therefore returns immediately.
     * 
     * @param callback a Runnable whose run method will be called when all
     *                 animations finish running.
     * @param milliseconds the number of milliseconds to wait until all of the
     *                      animations are finished running to run the Runnable.
     * @return this AComponent instance for method chaining.
     * @see AComponent#await() 
     */
    public AComponent then(Runnable callback, long milliseconds) {
        animatedObject.then(callback, milliseconds);
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
     * Moves the AComponent horizontally to the specified X position.
     * 
     * @param x the X position to move the AComponent towards
     * @return this AComponent instance for chaining method calls
     */
    public AComponent translateX(int x) {
        animatedObject.animateValue(
                getX(), 
                x,
                newX -> setLocation((int)newX.value(), getY())
        );
        
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
        animatedObject.animateValue(
                getY(), 
                y,
                newY -> setLocation(getX(), (int)newY.value())
        );
        
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
        // Animate the red value of the highlight color to the background color.
        animatedObject.animateValue(
                highlightColor.getRed(), 
                background.getRed(),
                r -> {
                    highlightColor = new Color(
                            (int)r.value(),
                            highlightColor.getGreen(),
                            highlightColor.getBlue()
                    );
                    highlight = new Rectangle(0, 0, getWidth(), getHeight());
                    repaint();
                }
        );
        
        // Animate the blue value of the highlight color to the background color.
        animatedObject.animateValue(
                highlightColor.getBlue(), 
                background.getBlue(),
                g -> {
                    highlightColor = new Color(
                            highlightColor.getRed(),
                            (int)g.value(),
                            highlightColor.getBlue()
                    );
                    highlight = new Rectangle(0, 0, getWidth(), getHeight());
                    repaint();
                }
        );
        
        // Animate the green value of the highlight color to the background color.
        animatedObject.animateValue(
                highlightColor.getGreen(), 
                background.getGreen(),
                b -> {
                    highlightColor = new Color(
                            highlightColor.getRed(),
                            highlightColor.getGreen(),
                            (int)b.value()
                    );
                    highlight = new Rectangle(0, 0, getWidth(), getHeight());
                    repaint();
                }
        );
        
        // Tell subclasses that the highlight is being emptied.
        unhighlight();
        
        return this;
    }
    
    /**
     * Called when the highlight is being emptied.
     */
    protected void unhighlight() { }
    
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
        
        // Animate from the starting radius to no radius.
        animatedObject.animateValue(
                startRadius,
                0,
                r -> {
                    highlight = new Ellipse2D.Double(
                        x - r.value(), 
                        y - r.value(), 
                        2 * r.value(), 
                        2 * r.value()
                    );
                    repaint();
                }
        );
        
        // Tell subclasses that the highlight is being emptied.
        unhighlight();
        
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
        // Animate the height of the highlight from the height of the component
        // to zero.
        animatedObject.animateValue(
                getHeight(), 
                0, 
                p -> {
                    highlight = new Rectangle(
                        0, 
                        getHeight() - (int)p.value(), 
                        getWidth(), 
                        (int)p.value()
                    );
                    repaint();
                }
        );
        
        // Tell subclasses that the highlight is being emptied.
        unhighlight();
        
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
        // Animate the highlight width from the component's width to zero.
        animatedObject.animateValue(
                getWidth(), 
                0,
                w -> {
                    highlight = new Rectangle(
                        0, 
                        0, 
                        (int)w.value(), 
                        getHeight()
                    );
                    repaint();
                }           
        );
        
        // Tell subclasses that the highlight is being emptied.
        unhighlight();
        
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
        // Animate the component width to zero.
        animatedObject.animateValue(
                getWidth(),
                0, 
                w -> {
                    highlight = new Rectangle(
                        getWidth() - (int)w.value(), 
                        0, 
                        (int)w.value(), 
                        getHeight()
                    );
                    repaint();
                }
        );
        
        // Tell subclasses that the highlight is being emptied.
        unhighlight();
        
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
        // Animate the highlight's height from the component's height to zero.
        animatedObject.animateValue(
                getHeight(), 
                0, 
                p -> {
                    highlight = new Rectangle(
                        0, 
                        0, 
                        getWidth(), 
                        (int)p.value()
                    );
                    repaint();
                }
        );
        
        // Tell subclasses that the highlight is being emptied.
        unhighlight();
        
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
        // Animate from 0 to the component height.
        animatedObject.animateValue(
                0, 
                getHeight(),
                h -> {
                    highlight = new Rectangle(
                        0, 
                        0, 
                        getWidth(), 
                        (int)h.value()
                    );
                    repaint();
                }
        );
        
        // Notify subclasses that the component is being highlighted.
        highlight();
        
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
        // Animate from 0 to the component width.
        animatedObject.animateValue(
                0, 
                getWidth(),
                w -> {
                    highlight = new Rectangle(
                        getWidth() - (int)w.value(), 
                        0, 
                        (int)w.value(), 
                        getHeight()
                    );
                    repaint();
                }
        );
        
        // Notify subclasses that the component is being highlighted.
        highlight();
        
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
        // Animate from 0 to the component width.
        animatedObject.animateValue(
                0, 
                getWidth(),
                w -> {
                    highlight = new Rectangle(
                        0, 
                        0, 
                        (int)w.value(), 
                        getHeight()
                    );
                    repaint();
                }
        );
        
        // Notify subclasses that the component is being highlighted.
        highlight();
        
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
        // Animate from 0 to the component height.
        animatedObject.animateValue(
                0, getHeight(),
                h -> {
                    highlight = new Rectangle(
                        0, 
                        getHeight() - (int)h.value(), 
                        getWidth(), 
                        (int)h.value()
                    );
                    repaint();
                }
        );
        
        // Notify subclasses that the component is being highlighted.
        highlight();
        
        return this;
    }
}
