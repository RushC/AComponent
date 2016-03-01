package animator.interpolator;

/**
 * A RepeatedInterpolator can be used to repeat another Interpolator's animation
 * rapidly.
 * 
 * @author Caleb Rush
 */
public class RepeatingInterpolator extends CompositeInterpolator {
    private final int repetitions;
    
    /**
     * Constructs a new RepeatedInterpolator instance that repeats the specified
     * Interpolator's animation 3 times.
     * 
     * Keep in mind that the base Interpolator's animation must be sped up to
     * triple speed to accomplish these repetitions.
     * 
     * @param base the Interpolator to repeat.
     */
    public RepeatingInterpolator(Interpolator base) {
        this(base, 3);
    }
    
    /**
     * Constructs a new RepeatedInterpolator instance that repeats the specified
     * Interpolator's animation the specified number of times.
     * 
     * Keep in mind that the base Interpolator's animation must be sped up in
     * order to accomplish these repetitions.
     * 
     * @param base the Interpolator to repeat.
     * @param repetitions the number of times to repeat the base's interpolation
     */
    public RepeatingInterpolator(Interpolator base, int repetitions) {
        super(base);
        this.repetitions = repetitions;
    }
    
    @Override
    public double interpolate(double time) {
        return time == 1.0?
                base.interpolate(time):
                base.interpolate((time % (1.0 / repetitions)) * repetitions);
    }
}
