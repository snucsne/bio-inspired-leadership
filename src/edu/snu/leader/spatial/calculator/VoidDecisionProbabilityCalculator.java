/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.calculator;

//Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionProbabilityCalculator;
import edu.snu.leader.spatial.Group;
import edu.snu.leader.spatial.SimulationState;


/**
 * VoidDecisionProbabilityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class VoidDecisionProbabilityCalculator
        implements DecisionProbabilityCalculator
{

    /**
     * Initializes the calculator
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#initialize(edu.snu.leader.spatial.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        // Do nothing
    }

    /**
     * Calculates the initiation probability for a given agent
     *
     * @param agent The agent
     * @return The initiation probability
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#calcInitiateProbability(edu.snu.leader.spatial.Agent)
     */
    @Override
    public float calcInitiateProbability( Agent agent )
    {
        return 0.0f;
    }

    /**
     * Calculates the follow probability for a given agent
     *
     * @param agent The agent
     * @param group The potential group to join when following
     * @return The following probability
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#calcFollowProbability(edu.snu.leader.spatial.Agent, edu.snu.leader.spatial.Group)
     */
    @Override
    public float calcFollowProbability( Agent agent, Group group )
    {
        return 0.0f;
    }

    /**
     * Calculates the cancel probability for a given agent
     *
     * @param agent The agent
     * @return The cancel probability
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#calcCancelProbability(edu.snu.leader.spatial.Agent)
     */
    @Override
    public float calcCancelProbability( Agent agent )
    {
        return 0.0f;
    }

}
