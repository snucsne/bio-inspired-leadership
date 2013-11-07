/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.SimulationState;

import java.util.Properties;


/**
 * AbstractEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractEventTimeCalculator implements EventTimeCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            AbstractEventTimeCalculator.class.getName() );

    /** Key for modifying the initiation rate flag */
    protected static final String _MODIFY_INITIATION_RATE_KEY = "modify-initiation-rate";

    /** Key for modifying the following rate flag */
    protected static final String _MODIFY_FOLLOWING_RATE_KEY = "modify-following-rate";

    /** Key for modifying the cancellation rate flag */
    protected static final String _MODIFY_CANCELLATION_RATE_KEY = "modify-cancellation-rate";

    /** Newline string.  NOTE: The old way is temporarily used
      * because a student can't use Java 1.7.  Once that is fixed, the new way will be used. */
    //protected static final String _NEWLINE = System.lineSeparator();
    protected static final String _NEWLINE = System.getProperty("line.separator");



    /** The simulation state */
    protected SimulationState _simState = null;

    /** Flag for modifying the initiation rate */
    protected boolean _modifyInitiationRate = false;

    /** Flag for modifying the following rate */
    protected boolean _modifyFollowingRate = false;

    /** Flag for modifying the cancellation rate */
    protected boolean _modifyCancellationRate = false;


    /** The base initiation rate */
    protected float _initRateBase = 0.0f;

    /** Follow alpha constant (see Eq. 1) */
    protected float _followAlpha = 0.0f;

    /** Follow beta constant (see Eq. 1) */
    protected float _followBeta = 0.0f;

    /** Cancel alpha constant (see Eq. 2) */
    protected float _cancelAlpha = 0.0f;

    /** Cancel gamma constant (see Eq. 2) */
    protected float _cancelGamma = 0.0f;

    /** Cancel epsilon constant (see Eq. 2) */
    protected float _cancelEpsilon = 0.0f;


    /**
     * Initializes the calculator
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Save the simulation state
        _simState = simState;

        // Use hard-coded values from the paper
        _initRateBase = 1290.0f;
        _followAlpha = 162.3f;
        _followBeta = 75.4f;
        _cancelAlpha = 0.009f;
        _cancelGamma = 2.0f;
        _cancelEpsilon = 2.3f;

        // Get the properties
        Properties props = simState.getProps();

        // Get the initiation rate modification flag
        String modifyInitiationRateStr = props.getProperty( _MODIFY_INITIATION_RATE_KEY );
        Validate.notEmpty( modifyInitiationRateStr,
                "Modify initation rate (key="
                + _MODIFY_INITIATION_RATE_KEY
                + ") may not be empty" );
        _modifyInitiationRate = Boolean.parseBoolean( modifyInitiationRateStr );
        _LOG.info( "Using _modifyInitiationRate=[" + _modifyInitiationRate + "]" );

        // Get the following rate modification flag
        String modifyFollowingRateStr = props.getProperty( _MODIFY_FOLLOWING_RATE_KEY );
        Validate.notEmpty( modifyFollowingRateStr,
                "Modify following rate (key="
                + _MODIFY_FOLLOWING_RATE_KEY
                + ") may not be empty" );
        _modifyFollowingRate = Boolean.parseBoolean( modifyFollowingRateStr );
        _LOG.info( "Using _modifyFollowingRate=[" + _modifyFollowingRate + "]" );

        // Get the cancellation rate modification flag
        String modifyCancellationRateStr = props.getProperty( _MODIFY_CANCELLATION_RATE_KEY );
        Validate.notEmpty( modifyCancellationRateStr,
                "Modify cancellation rate (key="
                + _MODIFY_CANCELLATION_RATE_KEY
                + ") may not be empty" );
        _modifyCancellationRate = Boolean.parseBoolean( modifyCancellationRateStr );
        _LOG.info( "Using _modifyCancellationRate=[" + _modifyCancellationRate + "]" );


        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Returns a string description of the initiation time calculations
     *
     * @return A string description of the initiation time calculations
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#describeInitiation()
     */
    @Override
    public String describeInitiation()
    {
        // Only need to display one
        StringBuilder builder = new StringBuilder();
        builder.append( "initiation-rate = " );
        builder.append( calculateInitiationRate( _simState.getIndividualCount() ) );
        builder.append( _NEWLINE );

        return builder.toString();
    }

    /**
     * Returns a string description of the following time calculations
     *
     * @return A string description of the following time calculations
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#describeFollow()
     */
    @Override
    public String describeFollow()
    {
        // Get the group size
        int groupSize = _simState.getInitiatorsGroupSize(
                _simState.getAllIndividuals().get( 0 ) );

        StringBuilder builder = new StringBuilder();

        // Display the follow rate for all the potential follower counts
        for( int i = 1; i < groupSize; i++ )
        {
            builder.append( "follow-rate." );
            builder.append( String.format( "%02d", i ) );
            builder.append( " = " );
            builder.append( calculateFollowRate( groupSize, i ) );
            builder.append( _NEWLINE );
        }

        return builder.toString();
    }

    /**
     * Returns a string description of the cancellation time calculations
     *
     * @return A string description of the cancellation time calculations
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#describeCancellation()
     */
    @Override
    public String describeCancellation()
    {
        // Get the group size
        int groupSize = _simState.getInitiatorsGroupSize(
                _simState.getAllIndividuals().get( 0 ) );

        StringBuilder builder = new StringBuilder();

        // Display the cancel rate for all the potential follower counts
        for( int i = 1; i < groupSize; i++ )
        {
            builder.append( "cancel-rate." );
            builder.append( String.format( "%02d", i ) );
            builder.append( " = " );
            builder.append( (1.0f / calculateCancelRate( i ) ) );
            builder.append( _NEWLINE );
        }

        return builder.toString();
    }


    /**
     * Calculate the initiation rate
     *
     * @param groupSize The group size
     * @return The initiation rate
     */
    protected float calculateInitiationRate( int groupSize )
    {
        return _initRateBase * groupSize;
    }

    /**
     * Calculate the following rate
     *
     * @param groupSize The group size
     * @param departed The number of individuals departed
     * @return The following rate
     */
    protected float calculateFollowRate( int groupSize, int departed )
    {
        return _followAlpha
                + ( ( _followBeta * (groupSize - departed) ) / departed );

    }

    /**
     * The cancellation rate
     *
     * @param departed The number of individuals departed
     * @return The cancellation rate
     */
    protected float calculateCancelRate( int departed )
    {
        return _cancelAlpha / (1.0f
                + (float) Math.pow( (departed / _cancelGamma), _cancelEpsilon ) );

    }
}
