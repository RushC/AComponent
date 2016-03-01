package animator.interpolator;

/**
 * A QuadraticInterpolator is a simple implementation of an Interpolator.
 * 
 * A QuadraticInterpolator simply returns the square of the given time input.
 * The effect is an animation where a Property's value moves slowly and speeds
 * up as it reaches its destination.
 * 
 * @author Caleb Rush
 */
public class AcceleratingInterpolator implements Interpolator {    
    @Override
    public double interpolate(double time) {
        return time * time;
    }
}
