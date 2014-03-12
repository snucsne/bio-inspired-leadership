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
