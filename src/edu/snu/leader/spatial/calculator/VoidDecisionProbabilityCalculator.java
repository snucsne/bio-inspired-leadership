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
