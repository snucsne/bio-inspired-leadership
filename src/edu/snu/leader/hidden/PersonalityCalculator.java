/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

/**
 * ExperienceUpdator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface PersonalityCalculator
{
    /**
     * Initializes the updater
     *
     * @param simState The simulation's state
     */
    public void initialize( SimulationState simState );

    /**
     * Calculate the new personality value
     *
     * @param individual The individual's current personality
     * @param updateType The type of update being applied
     * @param followers The number of followers in the initiation
     * @return The updated personality value
     */
    public float calculatePersonality( SpatialIndividual individual,
            PersonalityUpdateType updateType,
            int followers );
}
