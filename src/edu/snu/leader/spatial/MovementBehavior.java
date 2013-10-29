/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;


/**
 * MovementBehavior
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface MovementBehavior
{
    /**
     * Initializes the movement behavior
     *
     * @param simState The simulation's state
     * @param agent The agent to whom the movement behavior belongs
     */
    public void initialize( SimulationState simState, Agent agent );

    /**
     * Executes this movement behavior
     */
    public void execute();

    /**
     * Returns a copy of this movement behavior
     *
     * @return A copy of this movement behavior
     */
    public MovementBehavior copy();
}
