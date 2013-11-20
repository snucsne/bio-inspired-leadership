/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.builder;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;

import java.awt.geom.Point2D;
import java.util.Properties;


/**
 * PersonalityDistributionIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PersonalityDistributionIndividualBuilder extends
        AbstractIndividualBuilder
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PersonalityDistributionIndividualBuilder.class.getName() );

    /** Key for the mean personality value */
    private static final String _PERSONALITY_MEAN_KEY = "personality-mean";

    /** Key for the personality value std dev */
    private static final String _PERSONALITY_STD_DEV_KEY = "personality-std-dev";

    /** Key for the minimum personality value */
    private static final String _MIN_PERSONALITY_KEY = "min-personality";

    /** Key for the maximum personality value */
    private static final String _MAX_PERSONALITY_KEY = "max-personality";

    /** Key for the type of random number distribution to use */
    private static final String _RNG_DIST_KEY = "personality-rng-dist";


    /** Types of random number distributions */
    protected enum RNDistribution {
        UNIFORM,
        GAUSSIAN
    };


    /** Mean personality value */
    protected float _personalityMean = 0.0f;

    /** Personality value standard deviation */
    protected float _personalityStdDev = 0.0f;

    /** The minimum allowable personality value */
    protected float _minPersonality = 0.0f;

    /** The maximum allowable personality value */
    protected float _maxPersonality = 1.0f;

    /** The type random number distribution to use */
    protected RNDistribution _rnDist = null;


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

        // Get the mean personality value
        String personalityMeanStr = props.getProperty( _PERSONALITY_MEAN_KEY );
        Validate.notEmpty( personalityMeanStr,
                "Personality mean (key="
                + _PERSONALITY_MEAN_KEY
                + ") may not be empty" );
        _personalityMean = Float.parseFloat( personalityMeanStr );
        _LOG.info( "Using _personalityMean=[" + _personalityMean + "]" );

        // Get the personality value standard deviation
        String personalityStdDevStr = props.getProperty( _PERSONALITY_STD_DEV_KEY );
        Validate.notEmpty( personalityStdDevStr,
                "Personality std dev (key="
                + _PERSONALITY_STD_DEV_KEY
                + ") may not be empty" );
        _personalityStdDev = Float.parseFloat( personalityStdDevStr );
        _LOG.info( "Using _personalityStdDev=[" + _personalityStdDev + "]" );

        // Get the min personality
        String minPersonalityStr = props.getProperty( _MIN_PERSONALITY_KEY );
        Validate.notEmpty( minPersonalityStr,
                "Minimum personality value (key="
                + _MIN_PERSONALITY_KEY
                + ") may not be empty" );
        _minPersonality = Float.parseFloat( minPersonalityStr );
        _LOG.info( "Using _minPersonality=[" + _minPersonality + "]" );

        // Get the max personality
        String maxPersonalityStr = props.getProperty( _MAX_PERSONALITY_KEY );
        Validate.notEmpty( maxPersonalityStr,
                "Maximum personality value (key="
                + _MAX_PERSONALITY_KEY
                + ") may not be empty" );
        _maxPersonality = Float.parseFloat( maxPersonalityStr );
        _LOG.info( "Using _maxPersonality=[" + _maxPersonality + "]" );

        // Get the random number distribution
        String rnDistStr = props.getProperty( _RNG_DIST_KEY );
        Validate.notEmpty( rnDistStr,
                "Random number distribution (key="
                + _RNG_DIST_KEY
                + ") may not be empty" );
        _rnDist = RNDistribution.valueOf( rnDistStr.toUpperCase() );
        _LOG.info( "Using _rnDist=[" + _rnDist + "]" );


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
        // Create the personality
        float personality = 0.0f;
        if( RNDistribution.GAUSSIAN.equals( _rnDist ) )
        {
            personality = createGaussianPersonality();
        }
        else if( RNDistribution.UNIFORM.equals( _rnDist ) )
        {
            personality = createUniformPersonality();
        }
        else
        {
            _LOG.error( "Unknown distribution [" + _rnDist + "]" );
            throw new RuntimeException( "Unknown distribution [" + _rnDist + "]" );
        }

        // Create a valid location
        Point2D location = createValidLocation( index );

        // Create the individual
        SpatialIndividual ind = new SpatialIndividual(
                generateUniqueIndividualID( index ),
                location,
                personality,
                DEFAULT_ASSERTIVENESS,
                createPreferredDir(),
                DEFAULT_RAW_CONFLICT,
                true );

        return ind;
    }

    /**
     * Creates a personality using a random value drawn from a
     * Gaussian distribution
     *
     * @return The personality value
     */
    protected float createGaussianPersonality()
    {
        float personality = _personalityMean
                + ( (float) _simState.getRandom().nextGaussian() * _personalityStdDev );
        if( _maxPersonality < personality )
        {
            personality = _maxPersonality;
        }
        else if (_minPersonality > personality )
        {
            personality = _minPersonality;
        }

        return personality;
    }

    /**
     * Creates a personality using a random value drawn from a
     * Uniform distribution
     *
     * @return The personality value
     */
    protected float createUniformPersonality()
    {
        float personality = ( _simState.getRandom().nextFloat()
                * (_maxPersonality - _minPersonality) )
                + _minPersonality;

        return personality;
    }

    /**
     * Creates the preferred direction for an individual
     *
     * @return The preferred direction
     */
    protected float createPreferredDir()
    {
        return DEFAULT_PREFERRED_DIR;
    }
}
