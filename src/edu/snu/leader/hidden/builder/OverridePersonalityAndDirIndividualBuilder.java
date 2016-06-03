/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.builder;

// Imports
import edu.snu.leader.hidden.MetricSpatialIndividual;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * OverridePersonalityAndDirIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class OverridePersonalityAndDirIndividualBuilder
        extends AbstractIndividualBuilder
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            OverridePersonalityAndDirIndividualBuilder.class.getName() );

    /** Key for the default personality value */
    private static final String _DEFAULT_PERSONALITY = "default-personality";

    /** Key for the default direction */
    private static final String _DEFAULT_DIRECTION = "default-direction";

    /** Key for the number of personality overrides */
    private static final String _OVERRIDE_PERSONALITY_COUNT = "override-personality-count";

    /** Key prefix for the override personality value */
    private static final String _OVERRIDE_PERSONALITY_PREFIX = "override-personality";

    /** Key for the number of direction overrides */
    private static final String _OVERRIDE_DIRECTION_COUNT = "override-direction-count";

    /** Key prefix for the override direction value */
    private static final String _OVERRIDE_DIRECTION_PREFIX = "override-direction";

    /** Key for the type of local communication */
    private static final String _LOCAL_COMMUNICATION_TYPE = "local-communication-type";


    /** The default personality */
    private float _defaultPersonality = 0.0f;

    /** The default direction */
    private float _defaultDirection = 0.0f;

    /** Map of personality overrides */
    private Map<Object, Float> _personalityOverrides = new HashMap<Object, Float>();

    /** Map of direction overrides */
    private Map<Object, Float> _directionOverrides = new HashMap<Object, Float>();

    /** Flag denoting whether or not metric local communication is used */
    private boolean _useMetric = false;


    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.builder.AbstractIndividualBuilder#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

        // Get the default personality
        String defaultPersonalityStr = props.getProperty( _DEFAULT_PERSONALITY );
        Validate.notEmpty( defaultPersonalityStr,
                "Default personality value (key="
                + _DEFAULT_PERSONALITY
                + ") may not be empty" );
        _defaultPersonality = Float.parseFloat( defaultPersonalityStr );
        _LOG.info( "Using _defaultPersonality=[" + _defaultPersonality + "]" );

        // Get the default direction
        String defaultDirectionStr = props.getProperty( _DEFAULT_DIRECTION );
        Validate.notEmpty( defaultDirectionStr,
                "Default direction value (key="
                + _DEFAULT_DIRECTION
                + ") may not be empty" );
        _defaultDirection = Float.parseFloat( defaultDirectionStr );
        _LOG.info( "Using _defaultDirection=[" + _defaultDirection + "]" );

        // Get the number of personality overrides
        int personalityOverrideCount = 0;
        String personalityOverrideCountStr = props.getProperty(
                _OVERRIDE_PERSONALITY_COUNT );
        if( null != personalityOverrideCountStr )
        {
            personalityOverrideCount = Integer.parseInt( personalityOverrideCountStr );
        }

        // Get the personality overrides
        for( int i = 0; i < personalityOverrideCount; i++ )
        {
            // Get the individual id
            String indIDKey = _OVERRIDE_PERSONALITY_PREFIX
                    + "."
                    + String.format( "%02d", i )
                    + ".id";
            String indID = props.getProperty( indIDKey );
            Validate.notEmpty( indID,
                    "Override personality individual ID (key=["
                    + indIDKey
                    + "] may not be empty" );

            // Get the personality value
            String indPersonalityKey = _OVERRIDE_PERSONALITY_PREFIX
                    + "."
                    + String.format( "%02d", i )
                    + ".value";
            String indPersonalityStr = props.getProperty( indPersonalityKey );
            Validate.notEmpty( indID,
                    "Override personality individual value (key=["
                    + indPersonalityStr
                    + "] may not be empty" );
            Float indPersonality = Float.valueOf( indPersonalityStr );

            // Add it to the map
            _personalityOverrides.put( indID, indPersonality );
        }

        // Get the number of direction overrides
        int directionOverrideCount = 0;
        String directionOverrideCountStr = props.getProperty(
                _OVERRIDE_DIRECTION_COUNT );
        if( null != directionOverrideCountStr )
        {
            directionOverrideCount = Integer.parseInt( directionOverrideCountStr );
        }

        // Get the direction overrides
        for( int i = 0; i < directionOverrideCount; i++ )
        {
            // Get the individual id
            String indIDKey = _OVERRIDE_DIRECTION_PREFIX
                    + "."
                    + String.format( "%02d", i )
                    + ".id";
            String indID = props.getProperty( indIDKey );
            Validate.notEmpty( indID,
                    "Override direction individual ID (key=["
                    + indIDKey
                    + "] may not be empty" );

            // Get the direction value
            String indDirectionKey = _OVERRIDE_DIRECTION_PREFIX
                    + "."
                    + String.format( "%02d", i )
                    + ".value";
            String indDirectionStr = props.getProperty( indDirectionKey );
            Validate.notEmpty( indID,
                    "Override direction individual value (key=["
                    + indDirectionStr
                    + "] may not be empty" );
            Float indDirection = Float.valueOf( indDirectionStr );

            // Add it to the map
            _directionOverrides.put( indID, indDirection );
        }

        // Get the type of local communication
        String localCommunicationType = props.getProperty( _LOCAL_COMMUNICATION_TYPE );
        if( "metric".equals( localCommunicationType ) )
        {
            _useMetric = true;
        }
        _LOG.info( "Using _useMetric=[" + _useMetric + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }


    /**
     * Builds an individual
     *
     * @param index The index of the individual to build
     * @return The individual
     * @see edu.snu.leader.hidden.builder.IndividualBuilder#build(int)
     */
    @Override
    public SpatialIndividual build( int index )
    {
        // Create a valid location
        Vector2D location = createValidLocation( index );

        // Create the individual's ID
        Object id = generateUniqueIndividualID( index );

        // Create the individual's personality
        float personality = _defaultPersonality;
        Float overridePersonality = _personalityOverrides.get( id );
        if( null != overridePersonality )
        {
            personality = overridePersonality.floatValue();
        }

        // Create the individual's direction
        float direction = _defaultDirection;
        Float overrideDirection = _directionOverrides.get( id );
        if( null != overrideDirection )
        {
            direction = overrideDirection.floatValue();
        }

        // Create the individual
        SpatialIndividual ind = null;

        if( _useMetric )
        {
            ind = new MetricSpatialIndividual( id,
                    location,
                    personality,
                    DEFAULT_ASSERTIVENESS,
                    direction,
                    DEFAULT_RAW_CONFLICT,
                    true );
        }
        else
        {
            ind = new SpatialIndividual( id,
                    location,
                    personality,
                    DEFAULT_ASSERTIVENESS,
                    direction,
                    DEFAULT_RAW_CONFLICT,
                    true );
        }


        return ind;
    }

}
