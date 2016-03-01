package animator;

import java.awt.Toolkit;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

/**
 * An Animator is used to change a value slowly over time at a consistent
 * interval.
 * 
 * An Animator is useful particularly to animate graphical elements easily and
 * in a synchronized manner. This Animator implements the Singleton design pattern,
 * meaning that only one Animator instance is used to animate all Properties. Since 
 * all animated values use the same Animator, only one thread will ever be needed 
 * to animate multiple different values, regardless of their relation and when 
 * they are animated.
 * 
 * @author Caleb Rush
 */
public class Animator {
    // The static Animator instance that acts as the sole reference available.
    private static Animator             animator;
    // The number of times the Animator should increment each second.
    private int                         framesPerSecond;
    // Whether or not the Animator is running.
    private boolean                     running;
    // Whether or not the Animator is waiting for an animation to finish.
    private boolean                     waiting;
    // The list of properties being animated by this Animator.
    private final ArrayList<Property>   properties;
    // Whether or not the Animator syncs the graphics state after every frame.
    private boolean                     syncing;
    
    /**
     * Creates a new Animator instance with a framerate of 60 frames per second.
     * 
     * This constructor is private to ensure there is only one instance of an
     * Animator created at any point in time.
     */
    private Animator() {
        framesPerSecond = 60;
        properties = new ArrayList<>();
    }
    
    /**
     * Adds the specified Property to the Animator's list of Property. The
     * Property will begin to be animated starting with the next frame.
     * 
     * If the Animator was previously stopped, it will be started again.
     * 
     * @param property the Property to begin animating
     */
    public void animate(Property property) {
        // Ensure the ArrayList is not read from while modifying it.
        synchronized (properties) {
            // Ensure the Property is not already being animated.
            if (properties.contains(property))
                return;
            
            // Add the Property to the list of Properties.
            properties.add(property);

            // If the Animator isn't running, start it.
            if (!running)
                start();
        }
    }
    
    /**
     * Blocks the current thread until the specified Property finishes its
     * animation.
     * 
     * If the Property is not actually being animated, this method will return
     * immediately.
     * 
     * This method should never be called from the event thread, as stopping
     * the event thread will prevent all events from being processed until
     * the animation is complete. In most applications of an animation, this
     * will mean that any modifications to the GUI will not appear until after
     * the animation, meaning the animation will not work.
     * 
     * @param property the Property to await
     */
    public void await(Property property) {
        await(property, 0);
    }
    
    /**
     * Blocks the current thread until the specified Property finishes its
     * animation.
     * 
     * If the Property is not actually being animated, this method will return
     * immediately.
     * 
     * This method should never be called from the event thread, as stopping
     * the event thread will prevent all events from being processed until
     * the animation is complete. In most applications of an animation, this
     * will mean that any modifications to the GUI will not appear until after
     * the animation, meaning the animation will not work.
     * 
     * @param property the Property to await
     * @param milliseconds the amount of extra time to delay after the
     *                     Property's animation is finished.
     */
    public void await(Property property, long milliseconds) {
        synchronized (properties) {
            // Ensure the Property is being animated.
            if (!properties.contains(property))
                return;
            
            // Start waiting.
            waiting = true;
            
            // Add an AnimationEndListener to the Property that will tell the
            // Animator to stop waiting.
            property.addAnimationEndListener(value -> waiting = false);
        }
        
        // Yield this Thread's processing time until the animation ends.
        while (waiting)
            Thread.yield();
        
        // Sleep for the specified amount of extra time.
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the frames per second for this Animator.
     * 
     * @return the frames per second, which represents the number of times the
     *         Animator will increment its Properties each second.
     */
    public int getFramesPerSecond() {
        return framesPerSecond;
    }
    
    /**
     * Retrieves the sole Animator instance. If the Animator instance has not
     * yet been created yet, it is created, then returned.
     * 
     * This method is the only way to retrieve an Animator instance. This is to
     * ensure that only one Animator is ever created at a time so that multiple
     * Properties don't use different Animators and waste system resources.
     * @return 
     */
    public static Animator getInstance() {
        // Initialize the Animator instance if it has not yet been initialized.
        if (animator == null)
            animator = new Animator();
        
        return animator;
    }
    
    /**
     * Returns all of the Properties currently being animated by this
     * Animator.
     * 
     * @return a read-only list of Properties
     */
    public Property[] getProperties() {
        return properties.toArray(new Property[0]);
    }
    
    /**
     * Whether or not the Animator is currently running.
     * 
     * @return true if the Animator is running, or false if the Animator is
     *         stopped or poised to stop.
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Whether or not the system's graphics are being synced after every frame
     * of animation.
     * 
     * @return true if the system's graphics are being synced, false otherwise.
     */
    public boolean isSyncing() {
        return syncing;
    }
    
    /**
     * Sets the number of times the Animator will increment its Properties every
     * second.
     * 
     * A higher number means that the Property's value will animate more smoothly
     * (with more intermediate values) and end more precisely. However, a higher
     * framerate takes more resources and will also result in all IncrementListeners
     * being called more often, which may have an undesirable effect on
     * performance. It is also possible that the processor will be able to keep
     * up with an extremely large framerate, particularly if IncrmentListeners
     * do some graphics processing.
     * 
     * The default framerate is 60 frames per second.
     * 
     * @param framesPerSecond the number of times the Animator will increment its
     *                        Properties every second. This must be a positive
     *                        value. It cannot be zero, since the Animator must
     *                        increment at least once every second.
     */
    public void setFramesPerSecond(int framesPerSecond) {
        if (framesPerSecond <= 0)
            throw new IllegalArgumentException("The Animator must increment at "
                    + "least once every second!");
        
        this.framesPerSecond = framesPerSecond;
    }
    
    /**
     * Sets whether or not the system's graphical state will be synced after
     * every frame.
     * 
     * Syncing the system's graphical state will ensure that any graphical
     * changes made during each increment are displayed. If animations appear
     * choppy, turning this on my solve the issue.
     * 
     * @param syncing true if the graphical state should be synced after every
     *                frame of animation, false otherwise.
     */
    public void setSyncing(boolean syncing) {
        this.syncing = syncing;
    }
    
    /**
     * Begins animating all of the contained Properties in a new thread.
     * 
     * The only time this method isn't being run is when there are no Properties
     * to animate. Otherwise, it should always be in this method.
     */
    private void start() {
        // Mark the Animator as running.
        running = true;
        
        // Create a new Thread to perform all of the animation.
        new Thread(() -> {
            while (running) {
                // Calculate the amount of time to wait for this frame.
                long waitTime = 1000 / framesPerSecond;
                
                // Pause before incrementing the Properties.
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Ensure the list of Properties cannot be unpredictably changed
                // until incrementing is finished.
                synchronized (properties) {
                    // Iterate through each of the Properties.
                    for (int i = 0; i < properties.size(); i++) {
                        Property property = properties.get(i);
                        
                        // Increment each Property by the amount of time that
                        // the Animator just slept for.
                        property.increment(waitTime);
                        
                        // Check if the property just completed its animation.
                        if (property.completed()) {
                            // Remove the Property from the list.
                            properties.remove(property);
                            // Decrement the loop index to reflect the 
                            // modification.
                            i--;
                        }
                        
                        // Synchronize the system's graphical state to ensure
                        // that any repaint calls made by the IncrementListeners
                        // are made in time.
                        if (syncing)
                            SwingUtilities.invokeLater(
                                    Toolkit.getDefaultToolkit()::sync);
                        
                        // Stop the Animator if there are no more Properties
                        // being animated.
                        if (properties.isEmpty())
                            running = false;
                    }
                }
            }
        // Start the Thread immediately.
        }).start();
    }
    
    /**
     * Stops animating the specified Property.
     * 
     * Note that if a Property's animation is stopped early, that Property's
     * value and time will not be reset, meaning that any future attempts to
     * animate th Property will continue where the stopped animation left off.
     * 
     * When a Property's animation is successfully stopped, its 
     * AnimationEndListener will not be called, since the animation did not
     * actually end. The Property's IncrementListener will not be called one
     * last time either. Ensure that the Property's value is usable when
     * stopping its animation early.
     * 
     * @param property the Property to stop animating
     * @return true if the Property's animation was successfully stopped, or
     *         false if the Property's animation has already ended or if the
     *         Property was never being animated in the first place.
     */
    public boolean stop(Property property) {
        // Ensure the ArrayList is not read while potentially being modified.
        synchronized (properties) {
            return properties.remove(property);
        }
    }
}
