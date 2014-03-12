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

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * FollowMostFollowedNeighborStrategy
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class FollowMostFollowedNeighborStrategy implements FollowStrategy
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            FollowMostFollowedNeighborStrategy.class.getName() );


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

        // Find the one with the most followers
        Neighbor mostFollowed = null;
        Iterator<Neighbor> neighborIter = nearestNeighbors.iterator();
        while( neighborIter.hasNext() )
        {
            Neighbor current = neighborIter.next();

            // Is the neighbor active?
            if( current.getIndividual().isActive( ) )
            {
                // Do we have one to compare it to?
                if( null == mostFollowed )
                {
                    // Nope, use it
                    mostFollowed = current;
                }
                else
                {
                    /* Does it have the most followers, or has the same
                     * number of followers, but is closer? */
                    int currentFollowers = current.getIndividual().getImmediateFollowerCount();
                    int mostFollowedFollowers = mostFollowed.getIndividual().getImmediateFollowerCount();

                    if( (currentFollowers > mostFollowedFollowers)
                            || ((currentFollowers == mostFollowedFollowers)
                                    && (current.getDistance() < mostFollowed.getDistance())) )
                    {
                        // Yup
                        mostFollowed = current;
                    }

                }
            }
        }

        // Did we find a viable leader?
        if( null != mostFollowed )
        {
            // Yup!  Follow that leader.
            ind.follow( mostFollowed, simState );
        }
        else
        {
            // Nope.  Have the individual act on its own.
            ind.initiateAction( simState );
        }

    }

}
