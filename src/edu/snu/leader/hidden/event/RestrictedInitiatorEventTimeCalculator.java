/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

// Imports
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Properties;


/**
 * RestrictedInitiatorEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class RestrictedInitiatorEventTimeCalculator
        extends DefaultEventTimeCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            RestrictedInitiatorEventTimeCalculator.class.getName() );

    /** Key for the initiating individual's ID */
    protected static final String _INITIATOR_ID = "initiator-id";


    /** The ID of the initiating individual */
    private Object _initiatorID = null;


    /**
     * Initializes the calculator
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.event.AbstractEventTimeCalculator#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

        // Get the ID of the initiating individual
        String initiatorIDStr = props.getProperty( _INITIATOR_ID );
        Validate.notEmpty( initiatorIDStr,
                "Initiating ID (key="
                + _INITIATOR_ID
                + ") may not be empty" );
        _initiatorID = initiatorIDStr;
        _LOG.info( "Using _initiatorID=[" + _initiatorID + "]" );


        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Calculates the time at the specified individual will initiate movement
     *
     * @param ind The individual
     * @return The initiation time
     * @see edu.snu.leader.hidden.event.DefaultEventTimeCalculator#calculateInitiationTime(edu.snu.leader.hidden.SpatialIndividual)
     */
    @Override
    public float calculateInitiationTime( SpatialIndividual ind )
    {
        // Default to no initiation
        float initiationTime = Float.POSITIVE_INFINITY;

        // Is this the initiator?
        if( ind.getID().equals( _initiatorID ) )
        {
            // Yup, initiate now
            initiationTime = 0.0f;
        }

        return initiationTime;
    }

    /**
    * Calculates the time at the specified individual will cancel an initiation
    *
    * @param ind The individual
    * @param departed The number of individuals who have already departed
    * @return The cancellation time
     * @see edu.snu.leader.hidden.event.DefaultEventTimeCalculator#calculateCancelTime(edu.snu.leader.hidden.SpatialIndividual, int)
     */
    @Override
    public float calculateCancelTime( SpatialIndividual ind, int departed )
    {
        return Float.POSITIVE_INFINITY;
    }

}
