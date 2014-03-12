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
