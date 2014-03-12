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
package edu.snu.leader.spatial;

/**
 * DecisionProbabilityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface DecisionProbabilityCalculator
{
    /**
     * Initializes the calculator
     *
     * @param simState The simulation's state
     */
    public void initialize( SimulationState simState );

    /**
     * Calculates the initiation probability for a given agent
     *
     * @param agent The agent
     * @return The initiation probability
     */
    public float calcInitiateProbability( Agent agent );

    /**
     * Calculates the follow probability for a given agent
     *
     * @param agent The agent
     * @param group The potential group to join when following
     * @return The following probability
     */
    public float calcFollowProbability( Agent agent, Group group );

    /**
     * Calculates the cancel probability for a given agent
     *
     * @param agent The agent
     * @return The cancel probability
     */
    public float calcCancelProbability( Agent agent );
}
