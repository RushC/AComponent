package animator.interpolator;

/**
 * A QuadraticInterpolator is a simple implementation of an Interpolator.
 * 
 * A QuadraticInterpolator simply returns the square of the given time input.
 * The effect is an animation where a Property's value moves quickly and slows
 * down as it reaches its destination.
 * 
 * @author Caleb Rush
 */
public class DeceleratingInterpolator implements Interpolator {    
    @Override
    public double interpolate(double time) {
        // Use a an inversed AcceleratedInterpolator to achieve the effect
        // of a decelerated interpolation.
        return new InversingInterpolator(new AcceleratingInterpolator())
                .interpolate(time);
    }
}