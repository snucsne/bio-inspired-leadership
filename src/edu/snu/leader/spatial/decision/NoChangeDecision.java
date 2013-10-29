/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.decision;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionType;
import edu.snu.leader.spatial.calculator.VoidDecisionProbabilityCalculator;
import edu.snu.leader.spatial.movement.VoidMovementBehavior;


/**
 * DoNothingDecision
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class NoChangeDecision extends AbstractDecision
{
    /**
     * Builds this DoNothingDecision object
     *
     * @param agent
     * @param time
     */
    public NoChangeDecision( Agent agent, long time )
    {
        super( DecisionType.NO_CHANGE,
                agent,
                new VoidMovementBehavior(),
                new VoidDecisionProbabilityCalculator(),
                time );
    }

    /**
     * Makes this decision
     *
     * @see edu.snu.leader.spatial.Decision#make()
     */
    @Override
    public void make()
    {
        // Do nothing
    }

}
