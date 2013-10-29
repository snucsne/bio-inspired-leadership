/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;


/**
 * PersonalityTrait
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface PersonalityTrait
{
    /**
     * Initializes the trait
     *
     * @param simState The simulation's state
     * @param agent The agent to whom the trait belongs
     */
    public void initialize( SimulationState simState,
            Agent agent );

    /**
     * Returns the personality
     *
     * @return The personality
     */
    public float getPersonality();

    /**
     * Updates this personality trait
     */
    public void update();

    /**
     * Resets any state information in the personality trait.  This does NOT
     * affect any updates to the personality value itself.
     */
    public void reset();

//    /**
//     * Returns a copy of this personality trait
//     *
//     * @return A copy of this personality trait
//     */
//    public PersonalityTrait copy();

}
