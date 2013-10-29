/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;

/**
 * AgentBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface AgentBuilder
{
    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     */
    public void initialize( SimulationState simState );

    /**
     * Builds an individual
     *
     * @param index The index of the individual to build
     * @return The agent
     */
    public Agent build( int index );

}
