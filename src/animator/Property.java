package animator;

import animator.interpolator.Interpolator;
import animator.interpolator.LinearInterpolator;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

/**
 * A Property represents a value that can be animated by the Animator.
 * 
 * A Property contains a starting value, an ending value, and a time duration
 * for which the property can be animated.
 * 
 * A Property also must have an Interpolator which will determine the values
 * of the Property during the animation.
 * 
 * A Property may also have an IncrementListener which will be called whenever
 * the Property's value is changed during he animation and an 
 * AnimationEndListener that gets called when the Property finishes its 
 * animation.
 * 
 * @author Caleb Rush
 */
public class Property {
    public final double                             start;
    public final double                             end;
    public final long                               duration;
    public final Interpolator                       interpolator;
    public final ArrayList<IncrementListener>       incrementListeners;
    public final ArrayList<AnimationEndListener>    animationEndListeners;
    
    // The amount of time that has passed in the animation.
    public long                                     time;
    
    /**
     * Constructs a Property with the given start and end values that lasts for
     * 400 milliseconds and uses a LinearInterpolator.
     * 
     * @param start the Property's initial value
     * @param end the Property's ending value that it will animate towards
     * @see LinearInterpolator
     */
    public Property(double start, double end) {
        this(start, end, 400, new LinearInterpolator(), null, null);
    }
    
    /**
     * Constructs a Property with the given start and end values that lasts for
     * the specified duration and uses a LinearInterpolator.
     * 
     * @param start the Property's initial value
     * @param end the Property's ending value that it will animate towards
     * @param duration the duration (in milliseconds) the animation should last.
     * @see LinearInterpolator
     */
    public Property(double start, double end, long duration) {
        this(start, end, duration, new LinearInterpolator(), null, null);
    }
    
    /**
     * Constructs a Property with the given start and end values that lasts for
     * the specified duration and uses the specified Interpolator.
     * 
     * @param start the Property's initial value
     * @param end the Property's ending value that it will animate towards
     * @param duration the duration (in milliseconds) the animation should last.
     * @param interpolator the Interpolator that determines the path the Property's
     *                     value will follow when animating towards the end value.
     */
    public Property(double start, double end, long duration,
            Interpolator interpolator) {
        this(start, end, duration, interpolator, null, null);
    }
    
    /**
     * Constructs a Property with the given start and end values that lasts for
     * the specified duration and uses the specified Interpolator.
     * 
     * The Property is also given an IncrementListener immediately.
     * 
     * @param start the Property's initial value
     * @param end the Property's ending value that it will animate towards
     * @param duration the duration (in milliseconds) the animation should last.
     * @param interpolator the Interpolator that determines the path the Property's
     *                     value will follow when animating towards the end value.
     * @param incrementListener the listener to be called whenever the Property's
     *                          value is incremented.
     */
    public Property(double start, double end, long duration,
            Interpolator interpolator, IncrementListener incrementListener) {
        this(start, end, duration, interpolator, incrementListener, null);
    }
    
    /**
     * Constructs a Property with the given start and end values that lasts for
     * the specified duration and uses the specified Interpolator.
     * 
     * The Property is also given an IncrementListener and an AnimationEndListener
     * immediately.
     * 
     * @param start the Property's initial value
     * @param end the Property's ending value that it will animate towards
     * @param duration the duration (in milliseconds) the animation should last.
     * @param interpolator the Interpolator that determines the path the Property's
     *                     value will follow when animating towards the end value.
     * @param incrementListener the listener to be called whenever the Property's
     *                          value is incremented.
     * @param animationEndListener the listener to be called once the Property's
     *                             animation is completed.
     */
    public Property(double start, double end, long duration,
            Interpolator interpolator, IncrementListener incrementListener,
            AnimationEndListener animationEndListener) {
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.interpolator = interpolator;
        
        incrementListeners = new ArrayList<>();
        animationEndListeners = new ArrayList<>();
        
        if (incrementListener != null)
            incrementListeners.add(incrementListener);
        if (animationEndListener != null)
            animationEndListeners.add(animationEndListener);
    }
    
    /**
     * Adds the specified AnimationEndListener to the Property's list of
     * AnimationEndListeners.
     * 
     * @param ael the AnimationEndListener to attach to the Property.
     * @see AnimationEndListener
     */
    public void addAnimationEndListener(AnimationEndListener ael) {
        animationEndListeners.add(ael);
    }
    
    /**
     * Adds the specified IncrementListener to the Property's list of
     * IncrementListeners.
     * 
     * @param il the IncrementListener to attach to the Property.
     * @see IncrementListener
     */
    public void addIncrementListener(IncrementListener il) {
        incrementListeners.add(il);
    }
    
    /**
     * Alerts each of the AnimationEndListeners of the end of the animation and
     * passes the Property's current value to each.
     */
    protected void alertAnimationEndListeners() {
        animationEndListeners.stream().forEach(ael -> 
                SwingUtilities.invokeLater(() -> 
                        ael.animationEnded(this)));
    }
    
    /**
     * Alerts each of the IncrementListeners of an increment and passes the
     * Property's current value to each.
     */
    protected void alertIncrementListeners() {
        incrementListeners.stream().forEach(il -> 
                SwingUtilities.invokeLater(() -> 
                        il.increment(this)));
    }
    
    /**
     * Animates the Property by passing it as an argument to the 
     * Animator#animate method.
     * 
     * @see Animator#animate(animator.Property) 
     */
    public void animate() {
        Animator.getInstance().animate(this);
    }
    
    /**
     * Blocks the current thread until the Property's animation is completed.
     * 
     * If the Property is not being animated, this method returns immediately.
     * 
     * @see Animator#await(animator.Property) 
     */
    public void await() {
        await(0);
    }
    
    /**
     * Blocks the current thread until the Property's animation is completed.
     * 
     * If the Property is not being animated, this method returns immediately.
     * 
     * @param milliseconds the amount of additional time after the Property's 
     *                     animation ends to wait
     * @see Animator#await(animator.Property, long) 
     */
    public void await(long milliseconds) {
        Animator.getInstance().await(this, milliseconds);
    }
    
    /**
     * Whether or not the Property's animation has been completed or not.
     * 
     * @return true if the Property's animation has finished, or false if it
     *         it hasn't.
     */
    public boolean completed() {
        return time == duration;
    }
    
    /**
     * Returns all of the AnimationEndListeners currently attached to this
     * Property.
     * 
     * @return all of the AnimationEndListeners currently attached to this
     * Property.
     */
    public AnimationEndListener[] getAnimationEndListeners() {
        return animationEndListeners.toArray(new AnimationEndListener[0]);
    }
    
    /**
     * Returns all of the IncrementListeners currently attached to this
     * Property.
     * 
     * @return all of the IncrementListeners currently attached to this
     * Property.
     */
    public IncrementListener[] getIncrementListeners() {
        return incrementListeners.toArray(new IncrementListener[0]);
    }
    
    /**
     * Increments the amount of time in the Property's animation. This will
     * cause the Property's value to likely change and potentially even finish
     * the Property's animation.
     * 
     * Calling this method will cause the Property to alert all of its attached
     * IncrementListeners. If the Property's animations is finished as a result
     * of calling this method, all of its AnimationEndListeners will be alerted
     * as well.
     * 
     * @param milliseconds 
     */
    public void increment(long milliseconds) {
        // Increment the time by the specified number of milliseconds.
        time += milliseconds;
        
        // Ensure the time does not exceed the duration.
        if (time > duration)
            time = duration;
        
        // Alert all of the IncrementListeners of the change in the Property's
        // value.
        alertIncrementListeners();
        
        // Alert the AnimationEndListeners if the animation has completed.
        if (completed())
            alertAnimationEndListeners();
    }
    
    /**
     * Detaches the specified AnimationEndListener from the Property.
     * 
     * @param ael the AnimationEndListener to detach from the Property.
     * @return true if the AnimationEndListener was successfully removed or
     *         false if the AnimationEndListener was not already attached to
     *         the Property.
     */
    public boolean removeAnimationEndListener(AnimationEndListener ael) {
        return animationEndListeners.remove(ael);
    }
    
    /**
     * Detaches the specified IncrementListener from the Property.
     * 
     * @param il the IncrementListener to detach from the Property.
     * @return true if the IncrementListener was successfully removed or
     *         false if the IncrementListener was not already attached to
     *         the Property.
     */
    public boolean removeIncrmentListener(AnimationEndListener il) {
        return animationEndListeners.remove(il);
    }
    
    /**
     * Stops animating the Property by passing it as an argument to the
     * Animator#stop method.
     * 
     * @return true if the Property was successfully stopped, or false if the
     *         Property's animation already ended or if it was never being
     *         animated to begin with.
     * @see Animator#stop
     */
    public boolean stop() {
        return Animator.getInstance().stop(this);
    }
    
    /**
     * Returns the Property's current value based on its progress in its
     * animation.
     * 
     * @return the Property's current value which will likely be a value
     *         between its start and end values (with some exceptions depending
     *         on the Interpolator used).
     */
    public double value() {
        return start + (interpolator.interpolate((double)time / (double)duration) 
                * (end - start));
    }
}
