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
package edu.snu.leader.hidden;

// Imports
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * MetricSpatialIndividual
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class MetricSpatialIndividual extends SpatialIndividual
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            MetricSpatialIndividual.class.getName() );

    /** The distance within which are neighbors */
    private float _nearestNeighborDistance = 0.0f;


    /**
     * Builds this MetricSpatialIndividual object
     *
     * @param id
     * @param location
     * @param personality
     * @param assertiveness
     * @param preferredDirection
     * @param conflict
     * @param describeInitiationHistory
     */
    public MetricSpatialIndividual( Object id,
            Vector2D location,
            float personality,
            float assertiveness,
            float preferredDirection,
            float conflict,
            boolean describeInitiationHistory )
    {
        // Just call the super-class constructor
        super( id, location, personality, assertiveness, preferredDirection, conflict,
                describeInitiationHistory );
    }

    /**
     * TODO Method description
     *
     * @param simState
     * @see edu.snu.leader.hidden.SpatialIndividual#findNearestNeighbors(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void findNearestNeighbors( SimulationState simState )
    {
        _LOG.trace( "Entering findNearestNeighbors( simState )" );

        // Get the metric distance to calculate the nearest neighbors
        _nearestNeighborDistance = simState.getNearestNeighborDistance();

        // Build a priority queue to sort things for us
        PriorityQueue<Neighbor> sortedNeighbors =
                new PriorityQueue<Neighbor>();

        // Iterate through all the individuals
        Iterator<SpatialIndividual> indIter = simState.getAllIndividuals().iterator();
        while( indIter.hasNext() )
        {
            // Get the individual
            SpatialIndividual ind = indIter.next();

            // If it is us, continue on
            if( _id.equals( ind._id ) )
            {
                continue;
            }

            // Build a neighbor out of it and put it in the queue
            Neighbor neighbor = new Neighbor(
                    (float) _location.distance( ind._location ),
                    ind );
            sortedNeighbors.add( neighbor );
        }

        // Get all the neighbors within the specified distance
        Iterator<Neighbor> neighborIter = sortedNeighbors.iterator();
        while( neighborIter.hasNext() )
        {
            Neighbor neighbor = neighborIter.next();

            // Is it within the distance?
            if( neighbor.getDistance() <= _nearestNeighborDistance )
            {
                // Yup
                _nearestNeighbors.add( neighbor );
                neighbor.getIndividual().signalNearestNeighborStatus( this );
            }
//            else
//            {
//                // We can bail because the neighbors are sorted by distance
//                // from closest to farthest
//                break;
//            }
        }

        _LOG.trace( "Leaving findNearestNeighbors( simState )" );

    }

}
