/*
 * COPYRIGHT
 */
package edu.snu.leader.hierarchy.simple;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Iterator;
import java.util.Properties;


/**
 * SimpleDistanceUpdateStrategy
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SimpleDistanceUpdateStrategy implements UpdateStrategy
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SimpleDistanceUpdateStrategy.class.getName() );


    /** Parameter key for the motivation increase due to time */
    private static final String _MOTIVATION_TIME_INCREASE_KEY =
            "motivation-time-increase";

    /** Parameter key for the motivation increase due to the activity of a neighbor */
    private static final String _MOTIVATION_NEIGHBOR_INCREASE_KEY =
            "motivation-neighbor-increase";


    /** The increase in motivation due to time */
    private float _motivationTimeIncrease = 0.0f;

    /** The increase in motivation due to the activity of a neighbor */
    private float _motivationNeighborIncrease = 0.0f;


    /**
     * Initializes this update strategy
     *
     * @param simState The simulation state
     * @see edu.snu.leader.hierarchy.simple.UpdateStrategy#initialize(edu.snu.leader.hierarchy.simple.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Get the simulation properties
        Properties props = simState.getProps();

        // Get the motivation increase due to time
        String motivationTimeIncrease = props.getProperty(
                _MOTIVATION_TIME_INCREASE_KEY );
        Validate.notEmpty( motivationTimeIncrease,
                "Motivation increase due to time is required (key="
                + _MOTIVATION_TIME_INCREASE_KEY
                + ")" );
        _motivationTimeIncrease = Float.parseFloat( motivationTimeIncrease );

        // Get the motivation increase due to neighbor activity
        String motivationNeighborIncrease = props.getProperty(
                _MOTIVATION_NEIGHBOR_INCREASE_KEY );
        Validate.notEmpty( motivationNeighborIncrease,
                "Motivation increase due to activity of a neighbor is required (key="
                + _MOTIVATION_NEIGHBOR_INCREASE_KEY
                + ")" );
        _motivationNeighborIncrease = Float.parseFloat( motivationNeighborIncrease );

        // Log the values
        _LOG.debug( "_motivationTimeIncrease=["
                + _motivationTimeIncrease
                + "] _motivationNeighborIncrease=["
                + _motivationNeighborIncrease
                + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Updates the state of the given individual.  This strategy specifies
     * how exactly the update will be done.
     *
     * @param ind
     * @param simState
     * @see edu.snu.leader.hierarchy.simple.UpdateStrategy#update(edu.snu.leader.hierarchy.simple.Individual, edu.snu.leader.hierarchy.simple.SimulationState)
     */
    @Override
    public void update( Individual ind, SimulationState simState )
    {
        // If the individual is active, bail
        if( ind.isActive( ) )
        {
            return;
        }

        // Get the individual's motivation
        float motivation = ind.getMotivation();

        // Add the increase in motivation due to time
        motivation += _motivationTimeIncrease;

        // Iterate through the individual's nearest neighbors
        Iterator<Neighbor> neighborIter = ind.getNearestNeighbors().iterator();
        while( neighborIter.hasNext() )
        {
            Neighbor neighbor = neighborIter.next();

            // Is the neighbor active?
            if( neighbor.getIndividual().isActive( ) )
            {
                // Yup, get their contribution
                float distance = neighbor.getDistance() + 1;
                motivation += _motivationNeighborIncrease
                        / (distance * distance);
            }
        }

        // Store the updated motivation
        ind.setMotivation( motivation );
    }

}
