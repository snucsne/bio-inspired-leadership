/*
 * COPYRIGHT
 */
package edu.snu.leader.hierarchy.simple;

// Imports
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;


/**
 * FollowNearestNeighborStrategy
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class FollowNearestActiveNeighborStrategy implements FollowStrategy
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            FollowNearestActiveNeighborStrategy.class.getName() );


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

        // Find the closest one
        Neighbor closest = null;
        Iterator<Neighbor> neighborIter = nearestNeighbors.iterator();
        while( neighborIter.hasNext() )
        {
            Neighbor current = neighborIter.next();

            // Is the neighbor active?
            if( current.getIndividual().isActive( ) )
            {
                // Yup. Is it closer than the closest?
                if( (null == closest)
                        || (current.getDistance() < closest.getDistance()) )
                {
                    // Yup
                    closest = current;
                }

                // Was their activation timestep after this one?
                if( current.getIndividual().getActiveTimestep() > simState.getCurrentTime() )
                {
                    _LOG.error( "Current time=["
                            + simState.getCurrentTime()
                            + "] ind=["
                            + current.getIndividual().getID()
                            + "] activeTimestep=["
                            + current.getIndividual().getActiveTimestep()
                            + "]" );
                    System.exit( 1 );
                }
            }
        }

        // Did we find a viable leader?
        if( null != closest )
        {
            // Yup!  Follow that leader.
            ind.follow( closest, simState );
        }
        else
        {
            // Nope.  Have the individual act on its own.
            ind.initiateAction( simState );
        }
    }

}
