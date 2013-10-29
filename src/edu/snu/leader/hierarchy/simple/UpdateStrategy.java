/*
 * COPYRIGHT
 */
package edu.snu.leader.hierarchy.simple;

/**
 * UpdateStrategy
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface UpdateStrategy
{
    /**
     * Initializes this update strategy
     *
     * @param simState The simulation state
     */
    public void initialize( SimulationState simState );

    /**
     * Updates the state of the given individual.  This strategy specifies
     * how exactly the update will be done.
     *
     * @param ind
     * @param simState
     */
    public void update( Individual ind, SimulationState simState );
}
