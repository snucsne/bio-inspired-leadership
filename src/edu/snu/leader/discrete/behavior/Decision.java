/*
 * The Bio-inspired Leadership Toolkit is a set of tools used to simulate the
 * emergence of leaders in multi-agent systems. Copyright (C) 2014 Southern
 * Nazarene University This program is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or at your option) any later version. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package edu.snu.leader.discrete.behavior;

import edu.snu.leader.discrete.simulator.Agent;


/**
 * Decision All decisions will have this behavior, but it cannot be a decision
 * in and of itself.
 * 
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public abstract class Decision
{
    /** The probability of this decision happening */
    protected double _probability = 0.0;

    /** The conflict of making this decision */
    protected double _conflict = 0.0;

    /** The Agent that this decision is for */
    protected Agent _agent = null;

    /** The Agent leader corresponding to this decision */
    protected Agent _leader = null;

    protected DecisionType _decisionType = null;

    /**
     * Builds this Decision object
     * 
     * @param type The type of decision
     * @param agent The agent making this decision
     * @param leader The leader this agent is looking at (can be itself)
     */
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
     * Gets the conflict involved with this decision
     * 
     * @return
     */
    public double getConflict()
    {
        return _conflict;
    }

    /**
     * Sets the conflict involved with this decision
     * 
     * @param conflict
     */
    public void setConflict( double conflict )
    {
        _conflict = conflict;
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
    /**
     * DecisionType All the types of decisions that can be made.
     * 
     * @author Tim Solum
     * @version $Revision$ ($Author$)
     */
    public enum DecisionType {
        INITIATION, FOLLOW, CANCELLATION, DO_NOTHING, REACHED
    }
}
