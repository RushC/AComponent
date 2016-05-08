package animator;

import animator.interpolator.Interpolator;
import animator.interpolator.LinearInterpolator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * An AnimatedObject contains an Object instance and can animate its properties.
 * In appropriate cases, the Object's getter and setter methods can be found
 * using reflection to simplify the animation process.
 * 
 * @author Caleb Rush
 */
public class AnimatedObject {
    // The object instance whose properties are being animated.
    private final Object                    object;
    // This list of properties being animated.
    private final ArrayList<Property>       properties;
    // The AnimationEndListener used by all properties to remove themselves
    // from the properties list when their animations complete.
    protected final AnimationEndListener    propertyRemover;
    // The default amount of time each animation should last for.
    private long                            animationDuration;
    // The default Interpolator to use for each animation.
    private Interpolator                    interpolator;
    
    /**
     * Constructs a new AnimatedObject that can animate the specified Object
     * instance.
     * 
     * @param object the Object instance that can be animated by the constructed
     *               AnimatedObject.
     */
    public AnimatedObject(Object object) {
        this.object = object;
        properties = new ArrayList<>();
        propertyRemover = property -> {
            // Ensure concurrent modifications don't happen.
            synchronized(properties) {
                // Remove the propertyb from the list.
                properties.remove(property);
                
                // Notify all threads waiting on the properties that the list
                // has changed.
                properties.notifyAll();
            }
        };
        animationDuration = 400;
        interpolator = new LinearInterpolator();
    }
    
    /**
     * Animates a value in the contained Object instance to a specified value.
     * 
     * In order to automate the animation process, the Object must contain
     * a getter and setter method for the specified property name. The methods
     * must have the following naming conventions if the property name is
     * "value":
     * 
     * <ul>
     * <li>getter: getValue</li>
     * <li>setter: setValue</li>
     * </ul>
     * 
     * The setter method must also take a single argument of a primitive type.
     * 
     * The getter method is used to retrieve the object's current value for
     * the property being animated. The setter method is called after each 
     * animation frame.
     * 
     * @param propertyName the name of the property to animate. This is used to
     *                     find the getter and setter methods using reflection.
     * @param newValue the value to animate the property to.
     */
    public void animateValue(String propertyName, double newValue) {
        // Create the string for the accessor method for the property.
        String accessorName = "get"
                + Character.toUpperCase(propertyName.charAt(0))
                + (propertyName.length() > 1 ? propertyName.substring(1) : "");
                
        // Attempt to retrieve the current value using the object's accessor
        // method.
        try {
            Number oldValue = (Number)object.getClass().getMethod(accessorName)
                    .invoke(object);
            
            // Animate the value with retrieved old value.
            animateValue(propertyName, oldValue.doubleValue(), newValue);
            
        // If the method cannot be found, throw an exception.
        } catch (NoSuchMethodException | IllegalAccessException 
                | InvocationTargetException e) {
            throw new IllegalArgumentException("Invalid or nonexisting getter "
                    + "method for " + propertyName);
        }
    }
    
    /**
     * Animates a value in the contained Object instance from a specified value
     * to a specified new value.
     * 
     * In order to automate the animation process, the Object must contain
     * a setter method for the specified property name. The method must have 
     * the following naming conventions if the property name is "value":
     * 
     * <ul>
     * <li>setter: setValue</li>
     * </ul>
     * 
     * The setter method must also take a single argument of a primitive type.
     * 
     * The setter method is called after each animation frame.
     * 
     * @param propertyName the name of the property to animate. This is used to
     *                     find the getter and setter methods using reflection.
     * @param oldValue the value to animate the property from. This will be set
     *                 before the animation begins.
     * @param newValue the value to animate the property to.
     */
    public void animateValue(String propertyName, double oldValue, 
            double newValue) {
        // Create the string for the mutator method for the property.
        final String mutatorName = "set"
                + Character.toUpperCase(propertyName.charAt(0))
                + (propertyName.length() > 1 ? propertyName.substring(1) : "");
        
        // Check if the method is valid for the object.
        try {
            // Search through the list of methods in the object's class.
            final Method mutator = Arrays.stream(object.getClass().getMethods())
                    // Find methods with the matching name.
                    .filter(m -> mutatorName.equals(m.getName()))
                    // Find methods that take only one argument.
                    .filter(m -> m.getParameterCount() == 1)
                    // Find methods that take a numeric value.
                    .filter(m -> Number.class.isAssignableFrom(m.getParameterTypes()[0])
                                    || m.getParameterTypes()[0] == int.class
                                    || m.getParameterTypes()[0] == float.class
                                    || m.getParameterTypes()[0] == long.class
                                    || m.getParameterTypes()[0] == double.class)
                    // Retrieve the first method that matches the criteria.
                    .findFirst().get();
            
            // Animate the value with an IncrementListener that calls the mutator
            // method.
            animateValue(oldValue, newValue, p -> {
                try {
                    mutator.invoke(object, 
                            // Cast the value to its appropriate type.
                            castValue(p.value(), mutator.getParameterTypes()[0]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Invalid or nonexisting setter "
                    + "method for " + propertyName);
        }
    }
    
    /**
     * Animates a Property from the specified old value to the specified new
     * value, calling the specified IncrementListener every frame.
     * 
     * @param oldValue the value to start the animation from.
     * @param newValue the value to animate to.
     * @param incrementListener the IncrementListener to call during every
     *                          animation frame.
     */
    public void animateValue(double oldValue, double newValue, 
            IncrementListener incrementListener) {
        // Create a property to represent the value.
        Property property = new Property(
                // Animate from the old value to the new value.
                (double)oldValue, (double)newValue,
                // Use the specified duration and interpolator.
                animationDuration, interpolator,
                // Use the specified IncrementListener.
                incrementListener,
                // Have the property remove itself from the property list when
                // it's finished animating.
                propertyRemover
        );
        
        // Add the property to the properties list.
        synchronized (properties) {
            properties.add(property);
        }
        
        // Start animating the property.
        property.animate();
    }
    
    /**
     * Blocks the current thread until all of the currently running animations
     * are finished.
     */
    public void await() {
        await(0);
    }
    
    /**
     * Blocks the current thread until all of the currently running animations
     * are finished and then sleeps for the specified amount of time afterwards.
     * 
     * @param milliseconds the number of milliseconds to sleep for after all of 
     *                     the animations are completed.
     */
    public void await(long milliseconds) {
        // Aquire a lock on the properties.
        synchronized (properties) {
            // Continue waiting until the properties list is empty.
            while (!properties.isEmpty())
                try {
                    properties.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        
        // Sleep for the specified number of milliseconds.
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Casts the specified double value to the specified Number subclass.
     * 
     * @param value the value to cast.
     * @param cast the class to cast the number to. Must be a subclass of Number,
     *             and should be a primitive class.
     * @return the value cast to the specified type. If the specified type was
     *         not a primitive, the original value will be returned.
     */
    private Number castValue(double value, Class cast) {
        // Determine what type of number the class is, and cast the value to it.
        if (cast == byte.class)
            return (byte)value;
        else if (cast == short.class)
            return (short)value;
        else if (cast == int.class)
            return (int)value;
        else if (cast == long.class)
            return (long)value;
        else if (cast == float.class)
            return (float)value;
        return value;
    }
    
    /**
     * Gets the duration that each animation started by this AnimatedObject
     * instance lasts. 
     * 
     * @return the number of milliseconds each animation lasts.
     */
    public long getAnimationDuration() {
        return animationDuration;
    }
    
    /**
     * Gets the interpolator used for each animation started by this
     * AnimatedObject instance.
     * 
     * @return the Interpolator instance used by this AnimatedObject instance.
     */
    public Interpolator getInterpolator() {
        return interpolator;
    }
    
    /**
     * Sets the duration that each animation started by this AnimatedObject
     * should last.
     * 
     * @param animationDuration the number of milliseconds all future animations
     *                          should last.
     * @throws IllegalArgumentException if animationDuration is not positive.
     */
    public void setAnimationDuration(long animationDuration) {
        // Ensure the animationDuration is positive.
        if (animationDuration <= 0)
            throw new IllegalArgumentException("The animation duration must be "
                    + "positive!");
        
        this.animationDuration = animationDuration;
    }
    
    /**
     * Sets the interpolator to be used for each animation started by this
     * AnimatedObject instance.
     * 
     * @param interpolator an Interpolator to be used for each animation.
     */
    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }
    
    /**
     * Runs the specified callback runnable in a new thread after all of the 
     * currently running animations are finished.
     * 
     * This method is asynchronous and therefore returns immediately.
     * 
     * @param callback a Runnable whose run method will be called when all
     *                 animations finish running.
     * @see AnimatedObject#await() 
     */
    public void then(Runnable callback) {
        then(callback, 0);
    }
    
    /**
     * Runs the specified callback runnable in a new thread after a specified
     * amount of time after the most recently started animation has finished.
     * 
     * This method is asynchronous and therefore returns immediately.
     * 
     * @param callback a Runnable whose run method will be called when all
     *                 animations finish running.
     * @param milliseconds the number of milliseconds to wait until all of the
     *                      animations are finished running to run the Runnable.
     * @see AnimatedObject#await() 
     */
    public void then(Runnable callback, long milliseconds) {
        // Add an AnimationEndListener to the most recently started property.
        synchronized (properties) {
            Property last = properties.get(properties.size() - 1);
            last.addAnimationEndListener(p -> {
                // Wait for the specified number of milliseconds.
                try {
                    Thread.sleep(milliseconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Call the callback function.
                callback.run();
            });
        }
    }
}
