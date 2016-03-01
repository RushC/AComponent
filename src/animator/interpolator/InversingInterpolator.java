package animator.interpolator;

/**
 * A InverseInterpolator can be used to inverse another Interpolator's
 * interpolation, essentially allowing for an inverted animation of
 * any given Interpolator.
 * 
 * @author Caleb Rush
 */
public class InversingInterpolator extends CompositeInterpolator {
    /**
     * Constructs a new InverseInterpolator instance that can perform the
     * reverse animation of the given Interpolator. For example, if a
     * LinearInterpolator is supplied as a base, the InverseInterpolator
     * will perform an animation where the Property moves at a constant speed
     * from its ending position to its starting position.
     * 
     * @param base the Interpolator whose calculations should be reversed.
     */
    public InversingInterpolator(Interpolator base) {
        super(base);
    }
    
    @Override
    public double interpolate(double time) {
        return 1 - new ReversingInterpolator(base).interpolate(time);
    }
}
