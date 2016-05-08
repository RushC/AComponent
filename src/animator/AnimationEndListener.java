package animator;

/**
 * An AnimationEndListener can be attached to a Property and will be called
 * once the Property's animation ends.
 * 
 * An AnimationEndListener should be used to make any changes that might have
 * been too expensive or unnecessary to make every time the Property's value 
 * changed. For example, you may wish to add a MouseListener back to a Component
 * after it is done animating so that the user can interact with it again.
 * 
 * An AnimationEndListener is also useful for performing sequences of animations.
 * A new animation can be started in an AnimationEndListener once the first
 * animation completes.
 * 
 * @author Caleb Rush
 */
public interface AnimationEndListener {
    /**
     * Called when a Property's animation ends.
     * 
     * @param property the Property whose animation just ended.
     */
    void animationEnded(Property property);
}
