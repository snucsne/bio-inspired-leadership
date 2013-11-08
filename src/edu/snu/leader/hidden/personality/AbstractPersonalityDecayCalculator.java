/* 
 * COPYRIGHT
 */
package edu.snu.leader.hidden.personality;

import java.util.Properties;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;

/**
 * AbstractPersonalityDecayCalculator
 * 
 * @author Jeremy Acre
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractPersonalityDecayCalculator implements
        PersonalityDecayCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( AbstractPersonalityDecayCalculator.class.getName() );

    /** Key for the decay time */
    private static final String _DECAY_TIME_KEY = "decay-time";

    /** Key for the difference threshold */
    private static final String _DIFFERENCE_THRESHOLD_KEY = "difference-threshold";

    /** The update rule's decay time */
    protected static float _decayTime = 0.0f;

    /** The update rule's difference threshold */
    protected static float _differenceThreshold = 0.0f;

    /** The update rule's sim state */
    protected static SimulationState _simState = null;

    /**
     * Initialize the decay calculator
     * 
     * @param simState The simulation's state
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Store the simulation state
        _simState = simState;

        // Get the properties
        Properties props = simState.getProps();

        // Get the decay time
        String decayTimeStr = props.getProperty( _DECAY_TIME_KEY );
        Validate.notEmpty( decayTimeStr, "Decay time (key=" + _DECAY_TIME_KEY
                + ") may not be empty" );
        _decayTime = Float.parseFloat( decayTimeStr );
        _LOG.info( "Using _decayTime=[" + _decayTime + "]" );

        // Get the difference threshold
        String differenceThresholdStr = props.getProperty( _DIFFERENCE_THRESHOLD_KEY );
        Validate.notEmpty( differenceThresholdStr, "Difference threshold (key="
                + _DIFFERENCE_THRESHOLD_KEY + ") may not be empty" );
        _differenceThreshold = Float.parseFloat( differenceThresholdStr );
        _LOG.info( "Using _differenceThreshold=[" + _differenceThreshold + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Determines if the delay calculations will result in any decaying of
     * the specified individual's personality.
     * 
     * @param ind The individual
     * @return <code>true</code> if the personality will decay, otherwise,
     *         <code>false</code>
     */
    @Override
    public boolean isDecaying( SpatialIndividual ind )
    {
        // Default to false
        boolean result = false;

        // If the current personality is within the difference threshold
        if( Math.abs( ind.getPersonality() - ind.getInitialPersonality() ) > _differenceThreshold )
            ;
        {

            // Then true
            result = true;
        }

        // Return the result
        return result;
    }

}
