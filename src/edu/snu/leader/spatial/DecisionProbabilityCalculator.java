/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;

/**
 * DecisionProbabilityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface DecisionProbabilityCalculator
{
    /**
     * Initializes the calculator
     *
     * @param simState The simulation's state
     */
    public void initialize( SimulationState simState );

    /**
     * Calculates the initiation probability for a given agent
     *
     * @param agent The agent
     * @return The initiation probability
     */
    public float calcInitiateProbability( Agent agent );

    /**
     * Calculates the follow probability for a given agent
     *
     * @param agent The agent
     * @param group The potential group to join when following
     * @return The following probability
     */
    public float calcFollowProbability( Agent agent, Group group );

    /**
     * Calculates the cancel probability for a given agent
     *
     * @param agent The agent
     * @return The cancel probability
     */
    public float calcCancelProbability( Agent agent );
}
