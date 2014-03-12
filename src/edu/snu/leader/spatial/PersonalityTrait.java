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
 * PersonalityTrait
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface PersonalityTrait
{
    /**
     * Initializes the trait
     *
     * @param simState The simulation's state
     * @param agent The agent to whom the trait belongs
     */
    public void initialize( SimulationState simState,
            Agent agent );

    /**
     * Returns the personality
     *
     * @return The personality
     */
    public float getPersonality();

    /**
     * Updates this personality trait
     */
    public void update();

    /**
     * Resets any state information in the personality trait.  This does NOT
     * affect any updates to the personality value itself.
     */
    public void reset();

//    /**
//     * Returns a copy of this personality trait
//     *
//     * @return A copy of this personality trait
//     */
//    public PersonalityTrait copy();

}
