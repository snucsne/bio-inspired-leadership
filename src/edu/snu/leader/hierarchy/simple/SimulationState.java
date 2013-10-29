/*
 * COPYRIGHT
 */
package edu.snu.leader.hierarchy.simple;

// Imports
import edu.snu.leader.util.MiscUtils;
import ec.util.MersenneTwisterFast;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


/**
 * ExperimentState
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SimulationState
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( SimulationState.class.getName() );


    /** Key for the the random number seed */
    private static final String _RANDOM_SEED_KEY = "random-seed";

    /** Key for the update strategy class */
    private static final String _UPDATE_STRATEGY_CLASS_KEY = "update-strategy-class";

    /** Key for the follow strategy class */
    private static final String _FOLLOW_STRATEGY_CLASS_KEY = "follow-strategy-class";

    /** Key for the individual count */
    private static final String _INDIVIDUAL_COUNT_KEY = "indivdiual-count";

    /** Key for the number of nearest neighbors */
    private static final String _NEAREST_NEIGHBOR_COUNT_KEY = "nearest-neighbor-count";


    /** The current timestep in the simulation */
    private long _currentTime = -1l;

    /** Unique individual ID counter */
    private long _indIDCounter = 0;

    /** Unique group ID counter */
    private long _groupIDCounter = 0;

    /** The simulation properties */
    private Properties _props = null;

    /** Random number generator */
    private MersenneTwisterFast _random = null;

    /** The update strategy */
    private UpdateStrategy _updateStrategy = null;

    /** The follow strategy */
    private FollowStrategy _followStrategy = null;

    /** All the individuals in the simulation */
    private List<Individual> _allIndividuals = new LinkedList<Individual>();

    /** The number of nearest neighbors for each individual */
    private int _nearestNeighborCount = 0;

    /** Flag indicating that the simulation is finished */
    private boolean _finished = false;


    /**
     * Initialize the simulation state
     *
     * @param props
     */
    public void initialize( Properties props )
    {
        _LOG.trace( "Entering initialize( props )" );

        // Save the properties
        _props = props;

        // Get the random number generator seed
        String randomSeedStr = props.getProperty( _RANDOM_SEED_KEY );
        Validate.notEmpty( randomSeedStr, "Random seed is required" );
        long seed = Long.parseLong( randomSeedStr );
        _random = new MersenneTwisterFast( seed );

        // Load the update strategy
        String updateStrategyStr = _props.getProperty(
                _UPDATE_STRATEGY_CLASS_KEY );
        Validate.notEmpty( updateStrategyStr,
                "Update strategy class (key="
                + _UPDATE_STRATEGY_CLASS_KEY
                + ") may not be empty" );
        _updateStrategy = (UpdateStrategy) MiscUtils.loadAndInstantiate(
                updateStrategyStr,
                "Update strategy class" );
        _updateStrategy.initialize( this );

        // Load the follow strategy
        String followStrategyStr = _props.getProperty(
                _FOLLOW_STRATEGY_CLASS_KEY );
        Validate.notEmpty( updateStrategyStr,
                "Follow strategy class (key="
                + _FOLLOW_STRATEGY_CLASS_KEY
                + ") may not be empty" );
        _followStrategy = (FollowStrategy) MiscUtils.loadAndInstantiate(
                followStrategyStr,
                "Follow strategy class" );
        _followStrategy.initialize( this );

        // Get the number of individuals to create
        String individualCountStr = _props.getProperty( _INDIVIDUAL_COUNT_KEY );
        Validate.notEmpty( individualCountStr,
                "Individual count (key="
                + _INDIVIDUAL_COUNT_KEY
                + ") may not be empty" );
        int individualCount = Integer.parseInt( individualCountStr );

        // Get the number of nearest neighbors
        String nearestNeighborCountStr = _props.getProperty( _NEAREST_NEIGHBOR_COUNT_KEY );
        Validate.notEmpty( nearestNeighborCountStr,
                "Nearest neighbor count (key="
                + _NEAREST_NEIGHBOR_COUNT_KEY
                + ") may not be empty" );
        _nearestNeighborCount = Integer.parseInt( nearestNeighborCountStr );

        // Create the individuals
        for( int i = 0; i < individualCount; i++ )
        {
            // Build the individual
            Individual ind = new Individual( this );

            // Add it to the list of all individuals
            _allIndividuals.add( ind );
        }

        // Initialize all the individuals
        Iterator<Individual> indIter = _allIndividuals.iterator();
        while( indIter.hasNext() )
        {
            Individual ind = indIter.next();
            ind.initialize( this );
        }

        _LOG.trace( "Leaving initialize( props )" );
    }

    /**
     * Updates the simulation
     */
    public void update()
    {
        // Increment the current time
        _currentTime++;

        // The simulation runs until all individuals are active
        boolean inactivesExist = false;

        // Process all the individuals
        Individual ind = null;
        Iterator<Individual> indIter = _allIndividuals.iterator();
        while( indIter.hasNext() )
        {
            ind = indIter.next();

            // Is the individual already active?
            if( !ind.isActive() )
            {
                // Nope
                // Update the individual
                _updateStrategy.update( ind, this );

                // Is the individual going to act?
                if( ind.isMotivationOverThreshold() )
                {
                    // Yup
                    // Find out who the leader should be
                    _followStrategy.initiateFollowing( ind, this );
                }
                else
                {
                    // There are still inactive individuals
                    inactivesExist = true;
                }
            }
        }

        // Update all the individuals
        indIter = _allIndividuals.iterator();
        while( indIter.hasNext() )
        {
            ind = indIter.next();
            ind.update();
        }

        // Are we done?
        if( !inactivesExist )
        {
            // Yup
            _finished = true;
        }
    }


    /**
     * Returns the props for this object
     *
     * @return The props
     */
    public Properties getProps()
    {
        return _props;
    }

    /**
     * Returns the currentTime for this object
     *
     * @return The currentTime
     */
    public long getCurrentTime()
    {
        return _currentTime;
    }

    /**
     * Returns the nearestNeighborCount for this object
     *
     * @return The nearestNeighborCount
     */
    public int getNearestNeighborCount()
    {
        return _nearestNeighborCount;
    }

    /**
     * Returns a flag indicating whether or not the simulation has finished
     *
     * @return <code>true</code> if the simulation has finished, otherwise,
     * <code>false</code>
     */
    public boolean isFinished()
    {
        return _finished;
    }

    /**
     * Returns all the individuals in the simulation
     *
     * @return All the individuals
     */
    public List<Individual> getAllIndividuals()
    {
        return new ArrayList<Individual>( _allIndividuals );
    }


    /**
     * Returns the random number generator
     *
     * @return The random number generator
     */
    public MersenneTwisterFast getRandom()
    {
        return _random;
    }


    /**
     * Returns a new unique ID
     *
     * @return The unique ID
     */
    public Object generateUniqueIndividualID()
    {
        return "Ind"
                + String.format( "%05d", _indIDCounter++);
//                + ":"
//                + System.currentTimeMillis();
    }

    /**
     * Returns a new unique ID
     *
     * @return The unique ID
     */
    public Object generateUniqueGroupID()
    {
        return "Group"
                + String.format( "%05d", _groupIDCounter++);
//                + ":"
//                + System.currentTimeMillis();
    }

}
