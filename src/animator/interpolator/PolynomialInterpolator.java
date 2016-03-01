package animator.interpolator;

/**
 * A PolynomialInterpolator is a simple implementation of an Interpolator.
 * 
 * A PolynomialInterpolator simply returns the given time input raised to a
 * specified power. The effect is an animation where a Property's value moves 
 * slowly and speeds up as it reaches its destination. Using higher powers
 * will result in a more sudden shift.
 * 
 * @author Caleb Rush
 */
public class PolynomialInterpolator implements Interpolator {
    // The power to raise the the time input to.
    private int power;
    
    /**
     * Constructs a new PolynomialInterpolator instance that raises the time
     * input to the specified power.
     * 
     * @param power the power to raise the time input to.
     */
    public PolynomialInterpolator(int power) {
        this.power = power;
    }
    
    @Override
    public double interpolate(double time) {
        return Math.pow(time, power);
    }
}
