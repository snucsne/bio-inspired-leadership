package edu.snu.leader.discrete.behavior;

import edu.snu.leader.discrete.simulator.Agent;


public abstract class Decision
{
    /** The probability of this decision happening */
    protected double _probability = 0.0;

    /** The Agent that this decision is for */
    protected Agent _agent = null;

    /** The Agent leader corresponding to this decision */
    protected Agent _leader = null;

    protected DecisionType _decisionType = null;

    Decision( DecisionType type, Agent agent, Agent leader )
    {
        _decisionType = type;
        _agent = agent;
        _leader = leader;
    }

    /**
     * Chooses this decision and sets the MovementBehavior of the Agent
     */
    public abstract void choose();

    /**
     * Returns the probability of this decision being made
     * 
     * @return The probability this decision will be made
     */
    public double getProbability()
    {
        return _probability;
    }

    /**
     * Sets the probability that this decision will happen
     * 
     * @param prob The new probability
     */
    public void setProbability( double prob )
    {
        _probability = prob;
    }

    /**
     * Returns the Agent that this decision is for
     * 
     * @return The Agent
     */
    public Agent getAgent()
    {
        return _agent;
    }

    /**
     * Returns the Agent leader that applies to this decision
     * 
     * @return The Agent leader
     */
    public Agent getLeader()
    {
        return _leader;
    }

    /**
     * Returns the type of decision
     * 
     * @return The decision type
     */
    public DecisionType getDecisionType()
    {
        return _decisionType;
    }

    // ////////Nested enum\\\\\\\\\\
    public enum DecisionType {
        INITIATION, FOLLOW, CANCELLATION, DO_NOTHING, STOP
    }
}
