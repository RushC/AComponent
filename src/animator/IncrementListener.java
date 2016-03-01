package animator;

/**
 * An IncrementListener can be attached to a Property and will be called whenever
 * the Property has its value incremented during the animation.
 * 
 * An IncrementListener should be used to update actual values to reflect the 
 * changes made in a Property when it is being animated. In the case of
 * animating a Component's location, for example, an IncremenListener should be
 * used to call the setLocation method and repaint the Component to show the
 * progress being made in the animation.
 * 
 * @author Caleb Rush
 */
public interface IncrementListener {
    /**
     * Called whenever the Property's value is incremented during the animation.
     * 
     * @param value the Property that was just incremented.
     */
    void increment(Property property);
}
