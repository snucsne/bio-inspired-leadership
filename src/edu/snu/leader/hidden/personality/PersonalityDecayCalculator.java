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

// Imports
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;


/**
 * PersonalityDecayCalculator
 *
 * In some situations, we want personalities to "decay" to their initial value.
 * The intent in this mechanism is to allow individuals, and groups in general,
 * to adapt to changes in individuals and the environment.  Just because
 * an individual was not good once, does not mean this will be the case
 * forever.
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface PersonalityDecayCalculator
{
    /**
     * Initializes this calculator
     *
     * @param simState The simulation's state
     */
    public void initialize( SimulationState simState );

    /**
     * Determines if the delay calculations will result in any decaying of
     * the specified individual's personality.
     *
     * @param ind The individual
     * @return <code>true</code> if the personality will decay, otherwise,
     *         <code>false</code>
     */
    public boolean isDecaying( SpatialIndividual ind );

    /**
     * Calculates the decayed personality of the specified individual
     *
     * @param ind The individual whose personality will decay
     * @return The decayed personality
     */
    public float calculateDecayedPersonality( SpatialIndividual ind );
}
