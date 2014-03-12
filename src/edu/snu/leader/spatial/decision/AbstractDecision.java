/*
 *  The Bio-inspired Leadership Toolkit is a set of tools used to
 *  simulate the emergence of leaders in multi-agent systems.
 *  Copyright (C) 2014 Southern Nazarene University
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.snu.leader.spatial.decision;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.Decision;
import edu.snu.leader.spatial.DecisionProbabilityCalculator;
import edu.snu.leader.spatial.DecisionType;
import edu.snu.leader.spatial.MovementBehavior;
import org.apache.commons.lang.Validate;


/**
 * AbstractDecision
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractDecision implements Decision
{
    /** The type of the decision */
    protected DecisionType _type = DecisionType.NO_CHANGE;

    /** The corresponding agent */
    protected Agent _agent = null;

    /** The probability for this decision */
    protected float _probability = 0.0f;

    /** The movement behavior associated with this decision */
    protected MovementBehavior _movementBehavior = null;

    /** The calculator for this decision */
    protected DecisionProbabilityCalculator _calculator = null;

    /** The time of the decision */
    protected long _time = 0l;


    /**
     * Builds this AbstractDecision object
     *
     * @param type The decision's type
     * @param agent The associated agent
     * @param movementBehavior The associated behavior
     * @param calculator The probability calculator
     * @param time The time of the decision
     */
    public AbstractDecision( DecisionType type,
            Agent agent,
            MovementBehavior movementBehavior,
            DecisionProbabilityCalculator calculator,
            long time )
    {
        // Validate the type
        Validate.notNull( type, "Decision type may not be null" );
        _type = type;

        // Validate the agent
        Validate.notNull( type, "Agent may not be null" );
        _agent = agent;

        // Validate the movement behavior
        Validate.notNull( movementBehavior, "Movement behavior may not be null" );
        _movementBehavior = movementBehavior;

        // Validate the decision probability calculator
        Validate.notNull( calculator,
                "Decision probability calculator may not be null" );
        _calculator = calculator;

        _time = time;
    }

    /**
     * Returns the type of this decision
     *
     * @return The type of this decision
     * @see edu.snu.leader.spatial.Decision#getType()
     */
    @Override
    public DecisionType getType()
    {
        return _type;
    }

    /**
     * Calculates and returns the probability of this decision being made
     *
     * @return The probability that this decision is made
     * @see edu.snu.leader.spatial.Decision#calcProbability()
     */
    @Override
    public float calcProbability()
    {
        return _probability;
    }

    /**
     * Returns the time of the decision
     *
     * @return The time
     * @see edu.snu.leader.spatial.Decision#getTime()
     */
    @Override
    public long getTime()
    {
        return _time;
    }


}
