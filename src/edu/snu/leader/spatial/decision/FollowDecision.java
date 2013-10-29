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
import org.apache.commons.lang.Validate;


/**
 * FollowDecision
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class FollowDecision extends AbstractDecision
{
    /** The leader to follow */
    protected Agent _leader = null;

    /** The leader's group */
    protected Group _group = null;

    /**
     * Builds this FollowDecision object
     *
     * @param agent
     * @param leader
     * @param group
     * @param movementBehavior
     * @param calculator
     * @param time
     */
    public FollowDecision( Agent agent,
            Agent leader,
            Group group,
            MovementBehavior movementBehavior,
            DecisionProbabilityCalculator calculator,
            long time )
    {
        // Call the superclass constructor
        super( DecisionType.FOLLOW,
                agent,
                movementBehavior,
                calculator,
                time );

        // Validate and store the group
        Validate.notNull( group,
                "Group may not be null" );
        _group = group;

        // Pre-calculate the probability
        _probability = _calculator.calcFollowProbability( _agent, _group );
    }

    /**
     * Makes this decision
     *
     * @see edu.snu.leader.spatial.Decision#make()
     */
    @Override
    public void make()
    {
        // Put the agent in the leader's group
        Group currentGroup = _agent.getGroup();
        currentGroup.leave( _agent, _time );
        _group.join( _agent, _time );
        _agent.setLeader( _leader );

        // Set their movement behavior
        _agent.setMovementBehavior( _movementBehavior );
    }
}
