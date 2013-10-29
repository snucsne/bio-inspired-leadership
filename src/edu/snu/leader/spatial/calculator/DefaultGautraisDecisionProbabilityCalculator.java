/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.calculator;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Properties;
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionProbabilityCalculator;
import edu.snu.leader.spatial.Group;
import edu.snu.leader.spatial.SimulationState;


/**
 * DefaultGautraisDecisionProbabilityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DefaultGautraisDecisionProbabilityCalculator
        implements DecisionProbabilityCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            DefaultGautraisDecisionProbabilityCalculator.class.getName() );

    /** Key for modifying the initiation rate flag */
    private static final String _MODIFY_INITIATION_RATE_KEY = "modify-initiation-rate";

    /** Key for modifying the following rate flag */
    private static final String _MODIFY_FOLLOWING_RATE_KEY = "modify-following-rate";

    /** Key for modifying the cancellation rate flag */
    private static final String _MODIFY_CANCELLATION_RATE_KEY = "modify-cancellation-rate";



    /** The simulation state */
    protected SimulationState _simState = null;

    /** Flag for modifying the initiation rate */
    private boolean _modifyInitiationRate = false;

    /** Flag for modifying the following rate */
    private boolean _modifyFollowingRate = false;

    /** Flag for modifying the cancellation rate */
    private boolean _modifyCancellationRate = false;

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
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#initialize(edu.snu.leader.spatial.SimulationState)
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
        Properties props = simState.getProperties();

        // Get the initiation rate modification flag
        String modifyInitiationRateStr = props.getProperty(
                _MODIFY_INITIATION_RATE_KEY );
        Validate.notEmpty( modifyInitiationRateStr,
                "Modify initation rate (key="
                + _MODIFY_INITIATION_RATE_KEY
                + ") may not be empty" );
        _modifyInitiationRate = Boolean.parseBoolean( modifyInitiationRateStr );
        _LOG.info( "Using _modifyInitiationRate=["
                + _modifyInitiationRate
                + "]" );

        // Get the following rate modification flag
        String modifyFollowingRateStr = props.getProperty(
                _MODIFY_FOLLOWING_RATE_KEY );
        Validate.notEmpty( modifyFollowingRateStr,
                "Modify following rate (key="
                + _MODIFY_FOLLOWING_RATE_KEY
                + ") may not be empty" );
        _modifyFollowingRate = Boolean.parseBoolean( modifyFollowingRateStr );
        _LOG.info( "Using _modifyFollowingRate=["
                + _modifyFollowingRate
                + "]" );

        // Get the cancellation rate modification flag
        String modifyCancellationRateStr = props.getProperty(
                _MODIFY_CANCELLATION_RATE_KEY );
        Validate.notEmpty( modifyCancellationRateStr,
                "Modify cancellation rate (key="
                + _MODIFY_CANCELLATION_RATE_KEY
                + ") may not be empty" );
        _modifyCancellationRate = Boolean.parseBoolean( modifyCancellationRateStr );
        _LOG.info( "Using _modifyCancellationRate=["
                + _modifyCancellationRate
                + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Calculates the initiation probability for a given agent
     *
     * @param agent The agent
     * @return The initiation probability
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#calcInitiateProbability(edu.snu.leader.spatial.Agent)
     */
    @Override
    public float calcInitiateProbability( Agent agent )
    {
//        float tau = _initRateBase * _simState.getAgentCount();
        float tau = _initRateBase;

        // Do we modify it?
        if( _modifyInitiationRate )
        {
            // Yup, get the individual's personality
            float personality = agent.getPersonalityTrait().getPersonality();

            // Calculate k and use it to change the probability
            float k = calculateK( personality );
            tau /= k;

//            _LOG.debug( "personality=["
//                    + personality
//                    + "] k=["
//                    + k
//                    + "] tau=["
//                    + tau
//                    + "] prob=["
//                    + (1.0f/tau)
//                    + "]" );
        }

        return 1.0f / tau;
    }

    /**
     * Calculates the follow probability for a given agent
     *
     * @param agent The agent
     * @param group The potential group to join when following
     * @return The following probability
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#calcFollowProbability(edu.snu.leader.spatial.Agent, edu.snu.leader.spatial.Group)
     */
    @Override
    public float calcFollowProbability( Agent agent, Group group )
    {
        // Get the number of observed neighbors and group neighbors
        int observedNeighborCount = agent.getCurrentNearestNeighborCount();
        int observedGroupNeighborCount = agent.getNeighborGroupCount( group );

        // Calculate the rate
        float tau = _followAlpha
                + ( ( _followBeta * (observedNeighborCount - observedGroupNeighborCount))
                        / observedGroupNeighborCount );

//        _LOG.debug( "observedNeighborCount=["
//                + observedNeighborCount
//                + "] observedGroupNeighborCount=["
//                + observedGroupNeighborCount
//                + "] tau=["
//                + tau
//                + "] prob=["
//                + (1.0f/tau)
//                + "]" );



        // Do we modify it?
        if( _modifyFollowingRate )
        {
            // Yup, get the individual's personality
            float personality = agent.getPersonalityTrait().getPersonality();

            // Calculate k and use it to change the tau
            float k = calculateK( 1.0f - personality );
            tau /= k;

//            _LOG.debug( "personality=["
//                    + personality
//                    + "] k=["
//                    + k
//                    + "] tau=["
//                    + tau
//                    + "] prob=["
//                    + (1.0f/tau)
//                    + "]" );
        }

        return 1.0f / tau;
    }

    /**
     * Calculates the cancel probability for a given agent
     *
     * @param agent The agent
     * @return The cancel probability
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#calcCancelProbability(edu.snu.leader.spatial.Agent)
     */
    @Override
    public float calcCancelProbability( Agent agent )
    {
        // Get the number of observed followers
        int observedFollowerCount = agent.getObservedFollowerCount();

        // Calculate the probability
        // Don't forget to include the leader in the "departed" count
        float probability = _cancelAlpha / (1.0f + (float) Math.pow(
                ((observedFollowerCount + 1) / _cancelGamma ), _cancelEpsilon ));

        // Do we modify it?
        if( _modifyCancellationRate )
        {
            // Yup, get the individual's personality
            float personality = agent.getPersonalityTrait().getPersonality();

            // Calculate k and use it to change the probability
            float k = calculateK( 1.0f - personality );
            probability *= k;

//            _LOG.debug( "personality=["
//                    + personality
//                    + "] k=["
//                    + k
//                    + "] prob=["
//                    + probability
//                    + "]" );

        }

        return probability;
    }

    /**
     * Calculates k coefficient for the collective movement equations
     *
     * @param value
     * @return The k coefficient
     */
    private float calculateK( float value )
    {
        return 2.0f * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * 10.0f) ) );
    }

}
