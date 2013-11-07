/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.personality;

import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;

/**
 * ConstantPersonalityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ConstantPersonalityCalculator implements PersonalityCalculator
{

    /**
     * Initializes the updater
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.personality.PersonalityCalculator#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        // Do nothing
    }

    /**
     * Calculate the new personality value
     *
     * @param individual The individual whose personality will be calculated
     * @param updateType The type of update being applied
     * @param followers The number of followers in the initiation
     * @return The updated personality value
     * @see edu.snu.leader.hidden.personality.PersonalityCalculator#calculatePersonality(SpatialIndividual, boolean, int)
     */
    @Override
    public float calculatePersonality( SpatialIndividual individual,
            PersonalityUpdateType updateType,
            int followers )
    {
        // Just return the original value
        return individual.getPersonality();
    }

}
