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


/**
 * CancelDecision
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class CancelDecision extends AbstractDecision
{

    /**
     * Builds this CancelDecision object
     *
     * @param agent
     * @param movementBehavior
     * @param calculator
     * @param time
     */
    public CancelDecision( Agent agent,
            MovementBehavior movementBehavior,
            DecisionProbabilityCalculator calculator,
            long time )
    {
        // Call the superclass constructor
        super( DecisionType.CANCEL,
                agent,
                movementBehavior,
                calculator,
                time );

        // Pre-calculate the probability
        _probability = _calculator.calcCancelProbability( _agent );
    }

    /**
     * Makes this decision
     *
     * @see edu.snu.leader.spatial.Decision#make()
     */
    @Override
    public void make()
    {
        // Return the agent to the default group and unset the leader
        Group currentGroup = _agent.getGroup();
        currentGroup.leave( _agent, _time );
        Group.NONE.join( _agent, _time );
        _agent.setLeader( null );

        // Set their movement behavior
        _agent.setMovementBehavior( _movementBehavior );
    }

}
