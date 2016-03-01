package animator.interpolator;

/**
 * A CompositeInterpolator is an Interpolator that utilizes another Interpolator.
 * A CompositeInterpolator may modify the base Interpolator's behavior, or it
 * could delegate Interpolation to the base while offering other features.
 * 
 * @author Caleb Rush
 */
public abstract class CompositeInterpolator implements Interpolator {
    protected Interpolator base;
    
    /**
     * Creates a new instance of a CompositeInterpolator that utilizes a default
     * Interpolator.
     */
    public CompositeInterpolator() {
        this(new LinearInterpolator());
    }
    
    /**
     * Creates a new instance of a CompositeInterpolator that utilizes the
     * given Interpolator.
     * 
     * @param base the Interpolator to be utilized by the CompositeInterpolator.
     */
    public CompositeInterpolator(Interpolator base) {
        this.base = base;
    }
}
