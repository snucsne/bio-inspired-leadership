/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

// Imports
import edu.snu.leader.hidden.PersonalityTrait;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.hidden.Task;
import edu.snu.leader.util.MathUtils;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * MultiplePersonalityTraitsEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class MultiplePersonalityTraitsEventTimeCalculator extends
        AbstractEventTimeCalculator
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            MultiplePersonalityTraitsEventTimeCalculator.class.getName() );

    /** Key for modifying the sigmoid slope value */
    private final String _SIGMOID_SLOPE_VALUE_KEY = "sigmoid-slope-value";

    /** Key for the initiating individual's ID */
    protected static final String _INITIATOR_ID_KEY = "initiator-id";

    /** Key for the flag to restrict initiating */
    protected static final String _RESTRICT_INITIATING_KEY = "restrict-initiating";

    /** Key for the flag to restrict canceling */
    protected static final String _RESTRICT_CANCELING_KEY = "restrict-canceling";


    /** Sigmoid slope value */
    private float _sigmoidSlopeValue = 0.0f;

    /** The ID of the initiating individual */
    protected Object _initiatorID = null;

    /** Flag to restrict initiating */
    protected boolean _restrictInitiating = false;

    /** Flag to restrict canceling */
    protected boolean _restrictCanceling = false;



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

        // Get the sigmoid slope value
        String sigmoidSlopeValueStr = props.getProperty(_SIGMOID_SLOPE_VALUE_KEY);
        Validate.notEmpty(sigmoidSlopeValueStr,
                "Sigmoid slope value (Key ="
                + _SIGMOID_SLOPE_VALUE_KEY
                + ") may not be empty " );
        _sigmoidSlopeValue = Float.parseFloat(sigmoidSlopeValueStr);
        _LOG.info( "Using _sigmoidSlopeValue = [" + _sigmoidSlopeValue + "]" );

        // Get the ID of the initiating individual
        String initiatorIDStr = props.getProperty( _INITIATOR_ID_KEY );
        Validate.notEmpty( initiatorIDStr,
                "Initiating ID (key="
                + _INITIATOR_ID_KEY
                + ") may not be empty" );
        _initiatorID = initiatorIDStr;
        _LOG.info( "Using _initiatorID=[" + _initiatorID + "]" );

        // Get the flag to restrict initiating
        String restrictInitiatingStr = props.getProperty(
                _RESTRICT_INITIATING_KEY );
        Validate.notEmpty( restrictInitiatingStr,
                "Flag to restrict initiating (key="
                + _RESTRICT_INITIATING_KEY
                + ") may not be empty" );
        _restrictInitiating = Boolean.parseBoolean(
                restrictInitiatingStr );
        _LOG.info( "Using _restrictInitiating=["
                + _restrictInitiating
                + "]" );

        // Get the flag to restrict canceling
        String restrictCancelingStr = props.getProperty(
                _RESTRICT_CANCELING_KEY );
        Validate.notEmpty( restrictCancelingStr,
                "Flag to restrict canceling (key="
                + _RESTRICT_CANCELING_KEY
                + ") may not be empty" );
        _restrictCanceling = Boolean.parseBoolean(
                restrictCancelingStr );
        _LOG.info( "Using _restrictCanceling=["
                + _restrictCanceling
                + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Calculates the time at the specified individual will initiate movement
     *
     * @param ind The individual
     * @return The initiation time
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#calculateInitiationTime(edu.snu.leader.hidden.SpatialIndividual)
     */
    @Override
    public float calculateInitiationTime( SpatialIndividual ind )
    {
        // Default to no initiation
        float initiationTime = Float.POSITIVE_INFINITY;

        // Do we restrict initiating?
        if( _restrictInitiating )
        {
            // Is this the initiator?
            if( ind.getID().equals( _initiatorID ) )
            {
                // Yup, initiate now
                initiationTime = 0.0f;
            }
        }
        else
        {
            // Get the initation rate
            float tau = calculateInitiationRate(
                    _simState.getIndividualCount() );

            // Do we modify it?
            if( _modifyInitiationRate )
            {
                // Yup, get the current task
                Task task = _simState.getCurrentTask();

                // Get the appropriate personality trait value
                float personalityTraitValue = 0.0f;
                // Is the task navigation?
                if( Task.NAVIGATE.equals( task ) )
                {
                    personalityTraitValue = ind.getInitialPersonalityTrait(
                            PersonalityTrait.BOLDNESS_SHYNESS );
                }
                else if( Task.EXPLORE.equals( task ) )
                {
                    personalityTraitValue = ind.getInitialPersonalityTrait(
                            PersonalityTrait.EXPLORATION );
                }
                else if( Task.ESCAPE.equals( task ) )
                {
                    personalityTraitValue = ind.getInitialPersonalityTrait(
                            PersonalityTrait.BOLDNESS_SHYNESS );
                }
                else
                {
                    throw new RuntimeException( "Unknown task ["
                            + task
                            + "]" );
                }

                // Modify the rate
                float k = calculateK( personalityTraitValue );
                tau = tau / k;
            }

            initiationTime = MathUtils.generateRandomExponential(
                    1.0f / tau,
                    _simState.getRandom() );
        }

        return initiationTime;
    }

    /**
     * Calculates the time at the specified individual will follow an
     * initiator
     *
     * @param ind The individual
     * @param initiator The initiator
     * @param departed The number of individuals who have already departed
     * @param groupSize The size of the group
     * @return The follow time
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#calculateFollowTime(edu.snu.leader.hidden.SpatialIndividual, edu.snu.leader.hidden.SpatialIndividual, int, int)
     */
    @Override
    public float calculateFollowTime( SpatialIndividual ind,
            SpatialIndividual initiator,
            int departed,
            int groupSize )
    {
        float tau = calculateFollowRate( groupSize, departed );

        // Do we modify it?
        if( _modifyFollowingRate )
        {
            // Yup, get the current task
            Task task = _simState.getCurrentTask();

            // Get the appropriate personality trait value
            float personalityTraitValue = 0.0f;
            // Is the task navigation?
            if( Task.NAVIGATE.equals( task ) )
            {
                personalityTraitValue = ind.getInitialPersonalityTrait(
                        PersonalityTrait.SOCIABILITY );
            }
            else if( Task.EXPLORE.equals( task ) )
            {
                personalityTraitValue = ind.getInitialPersonalityTrait(
                        PersonalityTrait.SOCIABILITY );
            }
            else if( Task.ESCAPE.equals( task ) )
            {
                personalityTraitValue = ind.getInitialPersonalityTrait(
                        PersonalityTrait.ESCAPE );
            }
            else
            {
                throw new RuntimeException( "Unknown task ["
                        + task
                        + "]" );
            }

            // Modify the rate
            float k = calculateK( personalityTraitValue );
            tau = tau / k;
        }

        return MathUtils.generateRandomExponential( 1.0f / tau,
                _simState.getRandom());
    }

    /**
     * Calculates the time at the specified individual will cancel an initiation
     *
     * @param ind The individual
     * @param departed The number of individuals who have already departed
     * @return The cancellation time
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#calculateCancelTime(edu.snu.leader.hidden.SpatialIndividual, int)
     */
    @Override
    public float calculateCancelTime( SpatialIndividual ind, int departed )
    {
        // Default to restricting since it involves no calculations
        float cancelTime = Float.POSITIVE_INFINITY;

        // Do we restrict canceling?
        if( !_restrictCanceling )
        {
            float cRate = calculateCancelRate( departed );

            // Do we modify it?
            if( _modifyCancellationRate )
            {
                // Yup, get the current task
                Task task = _simState.getCurrentTask();

                // Get the appropriate personality trait value
                float personalityTraitValue = 0.0f;
                // Is the task navigation?
                if( Task.NAVIGATE.equals( task ) )
                {
                    personalityTraitValue = ind.getInitialPersonalityTrait(
                            PersonalityTrait.BOLDNESS_SHYNESS );
                }
                else if( Task.EXPLORE.equals( task ) )
                {
                    personalityTraitValue = ind.getInitialPersonalityTrait(
                            PersonalityTrait.EXPLORATION );
                }
                else if( Task.ESCAPE.equals( task ) )
                {
                    personalityTraitValue = ind.getInitialPersonalityTrait(
                            PersonalityTrait.ESCAPE );
                }
                else
                {
                    throw new RuntimeException( "Unknown task ["
                            + task
                            + "]" );
                }

                // Modify the rate
                float k = calculateK( 1.0f - personalityTraitValue );
                cRate = cRate * k;
            }

            cancelTime = MathUtils.generateRandomExponential( cRate,
                    _simState.getRandom() );
        }

        return cancelTime;
    }

    /**
     * Calculates k coefficient for the collective movement equations
     *
     * @param value
     * @return The k coefficient
     */
    protected float calculateK( float value )
    {
        return 2.0f * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * _sigmoidSlopeValue) ) );
    }

}
