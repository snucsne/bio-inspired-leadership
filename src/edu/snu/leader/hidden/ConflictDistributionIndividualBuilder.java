/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.awt.geom.Point2D;
import java.util.Properties;


/**
 * ConflictDistributionIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ConflictDistributionIndividualBuilder extends
        AbstractIndividualBuilder
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            ConflictDistributionIndividualBuilder.class.getName() );

    /** Key for the mean conflict value */
    private static final String _CONFLICT_MEAN_KEY = "conflict-mean";

    /** Key for the conflict value std dev */
    private static final String _CONFLICT_STD_DEV_KEY = "conflict-std-dev";

    /** Key for the minimum conflict value */
    private static final String _MIN_CONFLICT_KEY = "min-conflict";

    /** Key for the maximum conflict value */
    private static final String _MAX_CONFLICT_KEY = "max-conflict";

    /** Key for the type of random number distribution to use */
    private static final String _RNG_DIST_KEY = "conflict-rng-dist";


    /** Types of random number distributions */
    protected enum RNDistribution {
        UNIFORM,
        GAUSSIAN
    };


    /** Mean conflict value */
    private float _conflictMean = 0.0f;

    /** Conflict value standard deviation */
    private float _conflictStdDev = 0.0f;

    /** The minimum allowable conflict value */
    private float _minConflict = 0.0f;

    /** The maximum allowable conflict value */
    private float _maxConflict = 1.0f;

    /** The type random number distribution to use */
    private RNDistribution _rnDist = null;


    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.AbstractIndividualBuilder#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

        // Get the mean conflict value
        String conflictMeanStr = props.getProperty( _CONFLICT_MEAN_KEY );
        Validate.notEmpty( conflictMeanStr,
                "Conflict mean (key="
                + _CONFLICT_MEAN_KEY
                + ") may not be empty" );
        _conflictMean = Float.parseFloat( conflictMeanStr );
        _LOG.info( "Using _conflictMean=[" + _conflictMean + "]" );

        // Get the conflict value standard deviation
        String conflictStdDevStr = props.getProperty( _CONFLICT_STD_DEV_KEY );
        Validate.notEmpty( conflictStdDevStr,
                "Conflict std dev (key="
                + _CONFLICT_STD_DEV_KEY
                + ") may not be empty" );
        _conflictStdDev = Float.parseFloat( conflictStdDevStr );
        _LOG.info( "Using _conflictStdDev=[" + _conflictStdDev + "]" );

        // Get the min conflict
        String minConflictStr = props.getProperty( _MIN_CONFLICT_KEY );
        Validate.notEmpty( minConflictStr,
                "Minimum conflict value (key="
                + _MIN_CONFLICT_KEY
                + ") may not be empty" );
        _minConflict = Float.parseFloat( minConflictStr );
        _LOG.info( "Using _minConflict=[" + _minConflict + "]" );

        // Get the max conflict
        String maxConflictStr = props.getProperty( _MAX_CONFLICT_KEY );
        Validate.notEmpty( maxConflictStr,
                "Maximum conflict value (key="
                + _MAX_CONFLICT_KEY
                + ") may not be empty" );
        _maxConflict = Float.parseFloat( maxConflictStr );
        _LOG.info( "Using _maxConflict=[" + _maxConflict + "]" );

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
     * @see edu.snu.leader.hidden.IndividualBuilder#build(int)
     */
    @Override
    public SpatialIndividual build( int index )
    {
        // Create the conflict
        float conflict = 0.0f;
        if( RNDistribution.GAUSSIAN.equals( _rnDist ) )
        {
            conflict = createGaussianConflict();
        }
        else if( RNDistribution.UNIFORM.equals( _rnDist ) )
        {
            conflict = createUniformConflict();
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
                DEFAULT_PERSONALITY,
                DEFAULT_ASSERTIVENESS,
                DEFAULT_PREFERRED_DIR,
                conflict,
                DEFAULT_DESCRIBE_INITIATION_HISTORY );

        return ind;
    }

    /**
     * Creates a conflict using a random value drawn from a
     * Gaussian distribution
     *
     * @return The conflict value
     */
    private float createGaussianConflict()
    {
        float conflict = _conflictMean
                + ( (float) _simState.getRandom().nextGaussian() * _conflictStdDev );
        if( _maxConflict < conflict )
        {
            conflict = _maxConflict;
        }
        else if (_minConflict > conflict )
        {
            conflict = _minConflict;
        }

        return conflict;
    }

    /**
     * Creates a conflict using a random value drawn from a
     * Uniform distribution
     *
     * @return The conflict value
     */
    private float createUniformConflict()
    {
        float conflict = ( _simState.getRandom().nextFloat()
                * (_maxConflict - _minConflict) )
                + _minConflict;

        return conflict;
    }
}
