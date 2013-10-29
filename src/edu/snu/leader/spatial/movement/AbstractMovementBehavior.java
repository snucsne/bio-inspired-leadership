/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.movement;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.MovementBehavior;
import edu.snu.leader.spatial.SimulationState;
import org.apache.commons.lang.Validate;


/**
 * AbstractMovementBehavior
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractMovementBehavior implements MovementBehavior
{
    /** The current simulation state */
    protected SimulationState _simState = null;

    /** The agent associated with this behavior */
    protected Agent _agent = null;


    /**
     * Initializes the movement behavior
     *
     * @param simState The simulation's state
     * @param agent The agent to whom the movement behavior belongs
     * @see edu.snu.leader.spatial.MovementBehavior#initialize(edu.snu.leader.spatial.SimulationState, edu.snu.leader.spatial.Agent)
     */
    @Override
    public void initialize( SimulationState simState, Agent agent )
    {
        // Validate and store the simulation state
        Validate.notNull( simState, "Simulation state may not be null" );
        _simState = simState;

        // Validate and store the agent
        Validate.notNull( agent, "The agent may not be null" );
        _agent = agent;
    }

    /**
     * Executes this movement behavior
     *
     * @see edu.snu.leader.spatial.MovementBehavior#execute()
     */
    @Override
    public void execute()
    {
        // Do nothing
    }

}
