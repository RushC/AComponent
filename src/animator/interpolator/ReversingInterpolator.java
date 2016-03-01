package animator.interpolator;

/**
 * A ReversedInterpolator can be used to reverse the animation created by a
 * base Interpolator. A ReversedInterpolator will result in an animation that
 * creates the exact same intermediate values as its base, but in the opposite
 * order.
 * 
 * @author Caleb Rush
 */
public class ReversingInterpolator extends CompositeInterpolator {
    /**
     * Constructs a new ReversedInterpolator that reverses the interpolation
     * of the specified Interpolator.
     * 
     * @param base the Interpolator to reverse
     */
    public ReversingInterpolator(Interpolator base) {
        super(base);
    }
    
    @Override
    public double interpolate(double time) {
        return base.interpolate(1 - time);
    }
}
