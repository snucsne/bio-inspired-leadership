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
package edu.snu.leader.hidden.personality;

import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;

/**
 * ConstantPersonalityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ConstantPersonalityCalculator implements PersonalityCalculator
{

    /**
     * Initializes the updater
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.personality.PersonalityCalculator#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        // Do nothing
    }

    /**
     * Calculate the new personality value
     *
     * @param individual The individual whose personality will be calculated
     * @param updateType The type of update being applied
     * @param followers The number of followers in the initiation
     * @return The updated personality value
     * @see edu.snu.leader.hidden.personality.PersonalityCalculator#calculatePersonality(SpatialIndividual, boolean, int)
     */
    @Override
    public float calculatePersonality( SpatialIndividual individual,
            PersonalityUpdateType updateType,
            int followers )
    {
        // Just return the original value
        return individual.getPersonality();
    }

}
