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
