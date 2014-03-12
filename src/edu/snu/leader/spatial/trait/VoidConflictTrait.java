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
package edu.snu.leader.spatial.trait;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.ConflictTrait;
import edu.snu.leader.spatial.Decision;
import edu.snu.leader.spatial.SimulationState;

/**
 * VoidConflictTrait
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class VoidConflictTrait implements ConflictTrait
{

    /**
     * Initializes the trait
     *
     * @param simState The simulation's state
     * @param agent The agent to whom the trait belongs
     * @see edu.snu.leader.spatial.ConflictTrait#initialize(edu.snu.leader.spatial.SimulationState, edu.snu.leader.spatial.Agent)
     */
    @Override
    public void initialize( SimulationState simState, Agent agent )
    {
        // Do nothing
    }

    /**
     * Calculates and returns the conflict for a given decision
     *
     * @param decision A potential decision made by the agent
     * @return The conflict for a given decision
     * @see edu.snu.leader.spatial.ConflictTrait#getConflict(edu.snu.leader.spatial.Decision)
     */
    @Override
    public float getConflict( Decision decision )
    {
        return 0;
    }

    /**
     * Updates this conflict trait
     *
     * @see edu.snu.leader.spatial.ConflictTrait#update()
     */
    @Override
    public void update()
    {
        // Do nothing
    }

}
