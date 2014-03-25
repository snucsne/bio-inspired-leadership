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

package edu.snu.leader.discrete.behavior;

import edu.snu.leader.discrete.simulator.Agent;


public interface PersonalityTrait
{
    /**
     * Returns the personality
     * 
     * @return The personality
     */
    public float getPersonality();

    /**
     * Updates the personality of the agent
     * 
     * @param agent Agent associated with this personality behavior
     */
    public void update();

    /**
     * Initializes this personality trait and links it to the Agent
     * 
     * @param agent Agent to be linked to
     */
    public void initialize( Agent agent );
}
