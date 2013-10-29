/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.trait;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.ConflictTrait;
import edu.snu.leader.spatial.Decision;
import edu.snu.leader.spatial.SimulationState;

/**
 * VoidConflictTrait
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class VoidConflictTrait implements ConflictTrait
{

    /**
     * Initializes the trait
     *
     * @param simState The simulation's state
     * @param agent The agent to whom the trait belongs
     * @see edu.snu.leader.spatial.ConflictTrait#initialize(edu.snu.leader.spatial.SimulationState, edu.snu.leader.spatial.Agent)
     */
    @Override
    public void initialize( SimulationState simState, Agent agent )
    {
        // Do nothing
    }

    /**
     * Calculates and returns the conflict for a given decision
     *
     * @param decision A potential decision made by the agent
     * @return The conflict for a given decision
     * @see edu.snu.leader.spatial.ConflictTrait#getConflict(edu.snu.leader.spatial.Decision)
     */
    @Override
    public float getConflict( Decision decision )
    {
        return 0;
    }

    /**
     * Updates this conflict trait
     *
     * @see edu.snu.leader.spatial.ConflictTrait#update()
     */
    @Override
    public void update()
    {
        // Do nothing
    }

}
