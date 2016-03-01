package animator.interpolator;

/**
 * An Interpolator is used to determine a Property's path as it is being 
 * animated.
 * 
 * An Interpolator's role is to convert a fraction of an animation's duration
 * to a fraction of the Property's change in value. 
 * 
 * @author Caleb Rush
 */
public interface Interpolator {
    /**
     * Converts a fraction of an animation's duration to a fraction of the
     * Property's change in value.
     * 
     * Generally, the return value should be 0 when the input is 0 and 1 when the
     * input is 1. Every input in between can return varying results depending
     * on the implementation of the Interpolator.
     * 
     * @param time the fraction of the total duration that has passed in the
     *             current animation. This should be a value between 0.0 and
     *             1.0 where 0.0 implies that no time has passed and 1.0
     *             represents the end of the animation.
     * @return a fraction of the Property's change in value. This should be in
     *         a similar range as the time input where 0.0 implies that the 
     *         Property should remain at its starting position and 1.0 implies
     *         it should move to its end position. There are some cases where
     *         it may go a little beyond those boundaries, such as when an
     *         Interpolator wants to create an elastic effect where the value
     *         goes past its final value and snaps back as it nears the end.
     */
    double interpolate(double time);
}
