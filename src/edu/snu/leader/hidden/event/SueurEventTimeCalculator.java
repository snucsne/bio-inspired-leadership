/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

// Imports
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.util.MathUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Properties;


/**
 * SueurEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SueurEventTimeCalculator implements EventTimeCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SueurEventTimeCalculator.class.getName() );


    /** Key for modifying the initiation rate flag */
    protected static final String _MODIFY_INITIATION_RATE_KEY = "modify-initiation-rate";

    /** Key for modifying the following rate flag */
    protected static final String _MODIFY_FOLLOWING_RATE_KEY = "modify-following-rate";

    /** Key for modifying the cancellation rate flag */
    protected static final String _MODIFY_CANCELLATION_RATE_KEY = "modify-cancellation-rate";

    /** Key for the intrinsic movement probability flag */
    protected static final String _ALPHA_MOVE_KEY = "alpha-move";

    /** Key for the mimetic movement coefficient key */
    protected static final String _BETA_MOVE_KEY = "beta-move";

    /** Key for the type of movement follower threshold key */
    protected static final String _S_MOVE_TYPE_KEY = "s-move-type";

    /** Key for the movement follower threshold key */
    protected static final String _S_MOVE_KEY = "s-move";

    /** Key for the movement degree of system sensitivity key */
    protected static final String _Q_MOVE_KEY = "q-move";

    /** Key for the intrinsic canceling probability flag */
    protected static final String _ALPHA_CANCEL_KEY = "alpha-cancel";

    /** Key for the mimetic canceling coefficient key */
    protected static final String _BETA_CANCEL_KEY = "beta-cancel";

    /** Key for the canceling follower threshold key */
    protected static final String _S_CANCEL_KEY = "s-cancel";

    /** Key for the canceling degree of system sensitivity key */
    protected static final String _Q_CANCEL_KEY = "q-cancel";

    /** Newline string */
    protected static final String _NEWLINE = System.lineSeparator();


    /** The simulation state */
    protected SimulationState _simState = null;

    /** Flag for modifying the initiation rate */
    protected boolean _modifyInitiationRate = false;

    /** Flag for modifying the following rate */
    protected boolean _modifyFollowingRate = false;

    /** Flag for modifying the cancellation rate */
    protected boolean _modifyCancellationRate = false;



    /** Intrinsic probability that an individual starts moving */
    protected float _alphaMove = 0.0f;

    /** Mimetic coefficient for joining movement */
    protected float _betaMove = 0.0f;

    /** Threshold number of followers for moving */
    protected SueurSMoveValue _sMove = null;

    /** Degree of sensitivity of individuals to the system */
    protected float _qMove = 0.0f;

    /** Intrinsic probability of canceling */
    protected float _alphaCancel = 0.0f;

    /** Inverse mimetic coefficient for canceling */
    protected float _betaCancel = 0.0f;

    /** Threshold number of followers for canceling */
    protected float _sCancel = 0.0f;

    /** Degree of sensitivity of individuals to the system */
    protected float _qCancel = 0.0f;


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

        // Get the intrinsic probability that an individual starts moving
        String alphaMoveStr = props.getProperty( _ALPHA_MOVE_KEY );
        Validate.notEmpty( alphaMoveStr,
                "Intrinsic move probability (key=["
                + _ALPHA_MOVE_KEY
                + "]) may not be empty" );
        _alphaMove = Float.parseFloat( alphaMoveStr );
        _LOG.info( "Using _alphaMove=[" + _alphaMove + "]" );

        // Get the mimetic coefficient for joining movement
        String betaMoveStr = props.getProperty( _BETA_MOVE_KEY );
        Validate.notEmpty( betaMoveStr,
                "Mimetic movement coefficient (key=["
                + _BETA_MOVE_KEY
                + "]) may not be empty" );
        _betaMove = Float.parseFloat( betaMoveStr );
        _LOG.info( "Using _betaMove=[" + _betaMove + "]" );

        // Get the type of threshold for movement
        String sMoveTypeStr = props.getProperty( _S_MOVE_TYPE_KEY );
        Validate.notEmpty( sMoveTypeStr,
                "Type of threshold (key=["
                + _S_MOVE_TYPE_KEY
                + "]) may not be empty" );
        _LOG.info( "Using sMoveTypeStr=[" + sMoveTypeStr + "]" );

        // Get the threshold number of followers for moving
        String sMoveStr = props.getProperty( _S_MOVE_KEY );
        Validate.notEmpty( sMoveStr,
                "Threshold number of followers for moving (key=["
                + _BETA_MOVE_KEY
                + "]) may not be empty" );
        if( sMoveTypeStr.matches( "(?i:constant)") )
        {
            float sMoveValue = Float.parseFloat( sMoveStr );
            _sMove = new ConstantSueurSMoveValue( sMoveValue );
        }
        else if( sMoveTypeStr.matches( "(?i:function)") )
        {
            _sMove = new FunctionSueurSMoveValue( sMoveStr );
        }
        else
        {
            throw new IllegalArgumentException( "Unknown s-move type ["
                    + sMoveTypeStr
                    + "]" );
        }
        _LOG.info( "Using sMoveStr=[" + sMoveStr + "]" );

        // Degree of sensitivity of individuals to the system
        String qMoveStr = props.getProperty( _Q_MOVE_KEY );
        Validate.notEmpty( qMoveStr,
                "Degree of sensitivity of individuals to the system (key=["
                + _Q_MOVE_KEY
                + "]) may not be empty" );
        _qMove = Float.parseFloat( qMoveStr );
        _LOG.info( "Using _qMove=[" + _qMove + "]" );

        // Intrinsic probability of canceling
        String alphaCancelStr = props.getProperty( _ALPHA_CANCEL_KEY );
        Validate.notEmpty( alphaCancelStr,
                "Intrinsic probability of canceling (key=["
                + _ALPHA_CANCEL_KEY
                + "]) may not be empty" );
        _alphaCancel = Float.parseFloat( alphaCancelStr );
        _LOG.info( "Using _alphaCancel=[" + _alphaCancel + "]" );

        // Inverse mimetic coefficient for canceling
        String betaCancelStr = props.getProperty( _BETA_CANCEL_KEY );
        Validate.notEmpty( betaCancelStr,
                "Inverse mimetic coefficient for canceling (key=["
                + _BETA_CANCEL_KEY
                + "]) may not be empty" );
        _betaCancel = Float.parseFloat( betaCancelStr );
        _LOG.info( "Using _betaCancel=[" + _betaCancel + "]" );

        // Threshold number of followers for canceling
        String sCancelStr = props.getProperty( _S_CANCEL_KEY );
        Validate.notEmpty( sCancelStr,
                "Threshold number of followers for canceling (key=["
                + _S_CANCEL_KEY
                + "]) may not be empty" );
        _sCancel = Float.parseFloat( sCancelStr );
        _LOG.info( "Using _sCancel=[" + _sCancel + "]" );

        // Degree of sensitivity of individuals to the system
        String qCancelStr = props.getProperty( _Q_CANCEL_KEY );
        Validate.notEmpty( qCancelStr,
                "Degree of sensitivity of individuals to the system for canceling (key=["
                + _Q_CANCEL_KEY
                + "]) may not be empty" );
        _qCancel = Float.parseFloat( qCancelStr );
        _LOG.info( "Using _alphaMove=[" + _qCancel + "]" );

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
        // Calculate the probability (which is simply alpha)
        float lambda = calculateInitiationProbability();

        // Do we modify it?
        if( _modifyInitiationRate )
        {
            // Yup
            // TODO
        }

        // Use the inverse to generate a random exponential
        float time = MathUtils.generateRandomExponential(
                lambda,
                _simState.getRandom() );

        return time;
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
        // Calculate the probability
        float lambda = calculateFollowProbability( departed, groupSize );

        // Do we modify it?
        if( _modifyFollowingRate )
        {
            // Yup
            // TODO
        }

        // Use the inverse to generate a random exponential
        float time = MathUtils.generateRandomExponential(
                lambda,
                _simState.getRandom() );

        return time;
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
        // Calculate the probability
        float lambda = calculateCancelProbability( departed, departed );

        // Do we modify it?
        if( _modifyCancellationRate )
        {
            // Yup
            // TODO
        }

        // Use the inverse to generate a random exponential
        float time = MathUtils.generateRandomExponential(
                lambda,
                _simState.getRandom() );

        return time;
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
        StringBuilder builder = new StringBuilder();
        builder.append( "initiate-probability = " );
        builder.append( calculateInitiationProbability() );
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
        StringBuilder builder = new StringBuilder();

        // Get the group size
        int groupSize = _simState.getInitiatorsGroupSize(
                _simState.getAllIndividuals().get( 0 ) );

        // Describe the follow rate for all the potential follower counts
        for( int i = 1; i <= groupSize; i++ )
        {
            builder.append( "follow-probability." );
            builder.append( String.format( "%02d", i ) );
            builder.append( " = " );

            float lambda = calculateFollowProbability( i, groupSize );

            builder.append( lambda );
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
        StringBuilder builder = new StringBuilder();

        // Get the group size
        int groupSize = _simState.getInitiatorsGroupSize(
                _simState.getAllIndividuals().get( 0 ) );

        // Describe the follow rate for all the potential follower counts
        for( int i = 1; i <= groupSize; i++ )
        {
            builder.append( "cancel-probability." );
            builder.append( String.format( "%02d", i ) );
            builder.append( " = " );

            float lambda = calculateCancelProbability( i, groupSize );

            builder.append( lambda );
            builder.append( _NEWLINE );
        }

        return builder.toString();
    }

    private float calculateInitiationProbability()
    {
        return _alphaMove;
    }

    private float calculateFollowProbability( int departed, int groupSize )
    {
        float sMoveValue = _sMove.getValue(
                _simState,
                departed,
                groupSize,
                _alphaMove,
                _betaMove );
        float departedQPow = (float) Math.pow( departed, _qMove );
        float lambda = _alphaMove +
                ( (_betaMove * departedQPow)
                / ((float) Math.pow( sMoveValue, _qMove ) + departedQPow) );

        return lambda;
    }

    private float calculateCancelProbability( int departed, int groupSize )
    {
        float departedQPow = (float) Math.pow( departed, _qCancel );
        float lambda = _alphaCancel +
                ( (_betaCancel * departedQPow)
                / ((float) Math.pow( _sCancel, _qCancel ) + departedQPow) );

        return lambda;
    }
}
