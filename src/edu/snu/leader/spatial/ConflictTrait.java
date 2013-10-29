/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;


/**
 * ConflictTrait
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface ConflictTrait
{
    /**
     * Initializes the trait
     *
     * @param simState The simulation's state
     * @param agent The agent to whom the trait belongs
     */
    public void initialize( SimulationState simState, Agent agent );

    /**
     * Calculates and returns the conflict for a given decision
     *
     * @param decision A potential decision made by the agent
     * @return The conflict for a given decision
     */
    public float getConflict( Decision decision );

    /**
     * Updates this conflict trait
     */
    public void update();
}
