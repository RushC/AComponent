package animator.interpolator;

/**
 * A FlashingInterpolator can be used to flash another Interpolator's animation
 * multiple times.
 * 
 * Flashing an animation means that the animation is performed normally, and
 * then performed in reverse.
 * 
 * After flashing the animation the requested number of times, the Interpolator
 * is then performed normally one last time.
 * 
 * Keep in mind that the base Interpolator's animation must be sped up 
 * significantly to achieve these flashes.
 * 
 * @author Caleb Rush
 */
public class FlashingInterpolator extends CompositeInterpolator {
    private final int flashes;
    
    /**
     * Constructs a new FlashingInterpolator that flashes the specified
     * Interpolator twice.
     * 
     * @param base the Interpolator to flash.
     */
    public FlashingInterpolator(Interpolator base) {
        this(base, 2);
    }
    
    /**
     * Constructs a new FlashingInterpolator that flashes the specified 
     * Interpolator the specified number of times.
     * 
     * @param base the Interpolator to flash.
     * @param flashes the number of times to flash the base's animation.
     */
    public FlashingInterpolator(Interpolator base, int flashes) {
        super(base);
        this.flashes = flashes;
    }
    
    @Override
    public double interpolate(double time) {
        // Calculate the amount of time spent in a flashing cycle.
        double flashCycle = 1.0 / (flashes + 0.5);
        // Calculate the amount of time spent in an any particular animation.
        double animationCycle = flashCycle / 2.0;
        // Calculate the sped up time value to pass to the interpolator.
        double scaledTime = (1.0 / animationCycle) * (time % animationCycle);
        
               // Check if the time is in the second half of a flash cycle.
        return time % flashCycle > animationCycle?
                // If so, reverse the base animation.
                new ReversingInterpolator(base).interpolate(scaledTime):
                // Otherwise, perform the base animation.
                base.interpolate(scaledTime);
    }
}
