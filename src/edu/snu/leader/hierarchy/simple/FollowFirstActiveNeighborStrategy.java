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
package edu.snu.leader.hierarchy.simple;

// Imports
import java.util.Iterator;
import java.util.List;


/**
 * FollowFirstActiveNeighborStrategy
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class FollowFirstActiveNeighborStrategy implements FollowStrategy
{

    /**
     * Initializes this follow strategy
     *
     * @param simState The simulation state
     * @see edu.snu.leader.hierarchy.simple.FollowStrategy#initialize(edu.snu.leader.hierarchy.simple.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        // Do nothing
    }

    /**
     * Initiates following in the specified individual.  This strategy
     * determines which individual will be followed.
     *
     * @param ind
     * @param simState
     * @see edu.snu.leader.hierarchy.simple.FollowStrategy#initiateFollowing(edu.snu.leader.hierarchy.simple.Individual, edu.snu.leader.hierarchy.simple.SimulationState)
     */
    @Override
    public void initiateFollowing( Individual ind, SimulationState simState )
    {
        // Get the individual's nearest neighbors
        List<Neighbor> nearestNeighbors = ind.getNearestNeighbors();

        // Find the one that acted first
        long firstActionTimestep = Long.MAX_VALUE;
        Neighbor actedFirst = null;
        Iterator<Neighbor> neighborIter = nearestNeighbors.iterator();
        while( neighborIter.hasNext() )
        {
            Neighbor current = neighborIter.next();

            // Is the neighbor active?
            if( current.getIndividual().isActive() )
            {
                // Yup. Did it act first?
                if( (null == actedFirst)
                        || (current.getIndividual().getActiveTimestep()
                                < actedFirst.getIndividual().getActiveTimestep()) )
                {
                    // Yup
                    actedFirst = current;
                }
            }
        }

        // Did we find a viable leader?
        if( null != actedFirst )
        {
            // Yup!  Follow that leader.
            ind.follow( actedFirst, simState );
        }
        else
        {
            // Nope.  Have the individual act on its own.
            ind.initiateAction( simState );
        }
    }

}
