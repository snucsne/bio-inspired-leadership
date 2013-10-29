/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.decision;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionProbabilityCalculator;
import edu.snu.leader.spatial.DecisionType;
import edu.snu.leader.spatial.Group;
import edu.snu.leader.spatial.MovementBehavior;
import edu.snu.leader.spatial.SimulationState;

import org.apache.commons.lang.Validate;

/**
 * InitiateDecision
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class InitiateDecision extends AbstractDecision
{
    /** The current simulation state */
    protected SimulationState _simState = null;


    /**
     * Builds this InitiateDecision object
     *
     * @param agent
     * @param movementBehavior
     * @param calculator
     * @param simState
     */
    public InitiateDecision( Agent agent,
            MovementBehavior movementBehavior,
            DecisionProbabilityCalculator calculator,
            SimulationState simState )
    {
        // Call the superclass constructor
        super( DecisionType.INITIATE,
                agent,
                movementBehavior,
                calculator,
                simState.getCurrentSimulationStep() );

        // Validate and store the simulation state
        Validate.notNull( simState,
                "Simulation state may not be null" );
        _simState = simState;

        // Pre-calculate the probability
        _probability = _calculator.calcInitiateProbability( _agent );
    }

    /**
     * Makes this decision
     *
     * @see edu.snu.leader.spatial.Decision#make()
     */
    @Override
    public void make()
    {
        // Put the agent in a new group and unset the leader
        Group currentGroup = _agent.getGroup();
        currentGroup.leave( _agent, _time );
        Group newGroup = Group.buildNewGroup( _simState );
        newGroup.join( _agent, _time );
        _agent.setLeader( null );

        // Set their movement behavior
        _agent.setMovementBehavior( _movementBehavior );
    }

}
