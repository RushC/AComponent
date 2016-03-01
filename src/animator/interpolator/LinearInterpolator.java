package animator.interpolator;

/**
 * A LinearInterpolator is a simple implementation of an Interpolator.
 * 
 * A LinearInterpolator simply returns the given time input as the output. The
 * effect is an animation where a Property's value moves at a constant velocity
 * towards its destination.
 * 
 * @author Caleb Rush
 */
public class LinearInterpolator implements Interpolator {
    @Override
    public double interpolate(double time) {
        return time;
    }
}
