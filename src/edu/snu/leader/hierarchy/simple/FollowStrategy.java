/*
 * COPYRIGHT
 */
package edu.snu.leader.hierarchy.simple;

/**
 * FollowStrategy
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface FollowStrategy
{
    /**
     * Initializes this follow strategy
     *
     * @param simState The simulation state
     */
    public void initialize( SimulationState simState );

    /**
     * Initiates following in the specified individual.  This strategy
     * determines which individual will be followed.
     *
     * @param ind
     * @param simState
     */
    public void initiateFollowing( Individual ind, SimulationState simState );
}
