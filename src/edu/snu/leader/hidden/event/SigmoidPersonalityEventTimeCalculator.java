/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

// Imports
import edu.snu.leader.hidden.SimulationState;
//import edu.snu.leader.hidden.SpatialIndividual;
//import edu.snu.leader.util.MathUtils;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * SigmoidPersonalityEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SigmoidPersonalityEventTimeCalculator
        extends PersonalityEventTimeCalculator
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SigmoidPersonalityEventTimeCalculator.class.getName() );

    /** Key for modifying the sigmoid slope value */
    private final String _SIGMOID_SLOPE_VALUE_KEY = "sigmoid-slope-value";



    /** Sigmoid slope value */
    private float _sigmoidSlopeValue = 0.0f;

   /**
     * Builds this SigmoidPersonalityEventTimeCalculator object
     *
     */
    public SigmoidPersonalityEventTimeCalculator()
    {
        _description = "k = 2.0f  * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * _sigmoidSlopeValue) ) )";
    }


    /**
     * Initializes this calculator
     *
     * @param simState
     * @see edu.snu.leader.hidden.event.PersonalityEventTimeCalculator#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize(SimulationState simState)
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

        // Get the sigmoid slope value
        String sigmoidSlopeValueStr = props.getProperty(_SIGMOID_SLOPE_VALUE_KEY);
        Validate.notEmpty(sigmoidSlopeValueStr,
                "Sigmoid slope value (Key ="
                + _SIGMOID_SLOPE_VALUE_KEY
                + ") may not be empty " );
        _sigmoidSlopeValue = Float.parseFloat(sigmoidSlopeValueStr);
        _LOG.info( "Using _sigmoidSlopeValue = [" + _sigmoidSlopeValue + "]" );

        _LOG.trace( "Leaving initialize( simState )" );

    }


    /**
     * Calculates k coefficient for the collective movement equations
     *
     * @param value
     * @return The k coefficient
     */
    @Override
    protected float calculateK( float value )
    {
        return 2.0f * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * _sigmoidSlopeValue) ) );
    }

}
