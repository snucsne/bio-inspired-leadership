/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

// Imports
import edu.snu.leader.util.MiscUtils;
import ec.util.MersenneTwisterFast;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;


/**
 * SimulationState
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SimulationState
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SimulationState.class.getName() );

    /** Key for the the random number seed */
    private static final String _RANDOM_SEED_KEY = "random-seed";

    /** Key for the individual count */
    private static final String _INDIVIDUAL_COUNT_KEY = "individual-count";

    /** Key for the number of nearest neighbors */
    private static final String _NEAREST_NEIGHBOR_COUNT_KEY = "nearest-neighbor-count";

    /** Key for the distance for nearest neighbors */
    private static final String _NEAREST_NEIGHBOR_DISTANCE_KEY = "nearest-neighbor-distance";

    /** Key for the flag indicating that individuals should be rebuild */
    private static final String _REBUILD_INDIVIDUALS_KEY = "rebuild-individuals";

    /** Key for the flag indicating that the nearest neighbor size should be
     * used as an initiator's group size */
    private static final String _USE_NEAREST_NEIGHBOR_GROUP_SIZE_KEY =
            "use-nearest-neighbor-group-size";

    /** Key for the number of times to run the simulator */
    private static final String _SIMULATION_COUNT_KEY = "simulation-count";

    /** Key for the event time calculator class */
    private static final String _EVENT_TIME_CALCULATOR_CLASS = "event-time-calculator-class";

    /** Key for the individual builder class */
    private static final String _INDIVIDUAL_BUILDER_CLASS = "individual-builder-class";

    /** Key for the personality calculator */
    private static final String _PERSONALITY_CALCULATOR_CLASS = "personality-calculator-class";

    /** Key for the flag denoting whether the total number of departed
     * individuals should be used or the number of nearest neighbors
     * that have departed. */
    private static final String _USE_ALL_DEPARTED_KEY = "use-all-departed";



    /** Current simulation index */
    private long _simIndex = 0;

    /** Unique individual ID counter */
    private long _indIDCounter = 0;

    /** Unique group ID counter */
    private long _groupIDCounter = 0;

    /** The simulation properties */
    private Properties _props = null;

    /** Random number generator */
    private MersenneTwisterFast _random = null;

    /** All the individuals in the simulation */
    private List<SpatialIndividual> _allIndividuals =
            new LinkedList<SpatialIndividual>();

    /** The event time calculator */
    private EventTimeCalculator _eventTimeCalc = null;

    /** The individual builder */
    private IndividualBuilder _indBuilder = null;

    /** The personality calculator */
    private PersonalityCalculator _personalityCalc = null;

    /** The total number of individuals */
    private int _individualCount = 0;

    /** The number of nearest neighbors for each individual */
    private int _nearestNeighborCount = 0;

    /** The distance within which are neighbors */
    private float _nearestNeighborDistance = 0.0f;

    /** Individuals remaining (i.e., not yet departed) */
    private Map<Object, SpatialIndividual> _remaining =
            new HashMap<Object, SpatialIndividual>();

    /** Individuals who have departed (i.e., either initiated or followed) */
    private Map<Object, SpatialIndividual> _departed =
            new HashMap<Object, SpatialIndividual>();


    /** Individuals that are eligible initiators */
    private Map<Object, SpatialIndividual> _eligibleInitiators =
            new HashMap<Object, SpatialIndividual>();

    /** Flag indicating that individual's should be rebuilt */
    private boolean _rebuildIndividuals = false;

    /** Flag indicating hat an initiator's group size is the number of
     * nearest neighbors */
    private boolean _useNearestNeighborGroupSize = false;

    /** The max number of individuals departed */
    private int _maxDepartedCount = 0;

    /** The maximum radius for generated locations */
    private float _maxRadius = 1.0f;

    /** The number of times to run the simulator */
    private int _simulationCount = 0;

    /** The predefined locations for individuals */
    private List<Point2D> _locations = new LinkedList<Point2D>();

    /** Flag denoting whether the total number of departed individuals should be
     * used or the number of nearest neighbors that have departed. */
    private boolean _useAllDeparted = true;


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
//        _LOG.warn( props.toString() );

        // Get the random number generator seed
        String randomSeedStr = props.getProperty( _RANDOM_SEED_KEY );
        Validate.notEmpty( randomSeedStr, "Random seed is required" );
        long seed = Long.parseLong( randomSeedStr );
        _random = new MersenneTwisterFast( seed );

        // Get the number of individuals to create
        String individualCountStr = _props.getProperty( _INDIVIDUAL_COUNT_KEY );
        Validate.notEmpty( individualCountStr,
                "Individual count (key="
                + _INDIVIDUAL_COUNT_KEY
                + ") may not be empty" );
        _individualCount = Integer.parseInt( individualCountStr );

        // Get the number of nearest neighbors
        String nearestNeighborCountStr = _props.getProperty( _NEAREST_NEIGHBOR_COUNT_KEY );
        Validate.notEmpty( nearestNeighborCountStr,
                "Nearest neighbor count (key="
                + _NEAREST_NEIGHBOR_COUNT_KEY
                + ") may not be empty" );
        _nearestNeighborCount = Integer.parseInt( nearestNeighborCountStr );

        // Get the distance used to calculate of nearest neighbors
        String nearestNeighborDistanceStr = _props.getProperty( _NEAREST_NEIGHBOR_DISTANCE_KEY );
//        Validate.notEmpty( nearestNeighborDistanceStr,
//                "Nearest neighbor distance (key="
//                + _NEAREST_NEIGHBOR_DISTANCE_KEY
//                + ") may not be empty" );
        if( null != nearestNeighborDistanceStr )
        {
            _nearestNeighborDistance = Float.parseFloat( nearestNeighborDistanceStr );
        }

        // Get the individual rebuild flag
        String rebuildIndividualsStr = _props.getProperty( _REBUILD_INDIVIDUALS_KEY );
        Validate.notEmpty( rebuildIndividualsStr,
                "Rebuild individuals flag (key="
                + _REBUILD_INDIVIDUALS_KEY
                + ") may not be empty" );
        _rebuildIndividuals = Boolean.parseBoolean( rebuildIndividualsStr );

        // Get the flag that specifies how the group size is calculated
        String useNearestNeighborGroupSizeStr = _props.getProperty(
                _USE_NEAREST_NEIGHBOR_GROUP_SIZE_KEY );
        Validate.notEmpty( useNearestNeighborGroupSizeStr,
                "Use nearest neighbor as group size flag (key="
                + _USE_NEAREST_NEIGHBOR_GROUP_SIZE_KEY
                + ") may not be empty" );
        _useNearestNeighborGroupSize = Boolean.parseBoolean(
                useNearestNeighborGroupSizeStr );

        // Get the simulation count
        String simulationCountStr = _props.getProperty( _SIMULATION_COUNT_KEY );
        Validate.notEmpty( simulationCountStr, "Simulation count is required" );
        _simulationCount = Integer.parseInt( simulationCountStr );

        // Load the event time calculator class
        String eventTimeCalcStr = props.getProperty(
                _EVENT_TIME_CALCULATOR_CLASS );
        Validate.notEmpty( eventTimeCalcStr,
                "Event time calculator class (key="
                + _EVENT_TIME_CALCULATOR_CLASS
                + ") may not be empty" );
        _eventTimeCalc = (EventTimeCalculator) MiscUtils.loadAndInstantiate(
                eventTimeCalcStr,
                "Event time calculator class" );
        _eventTimeCalc.initialize( this );

        // Load the individual builder
        String indBuilderStr = props.getProperty(
                _INDIVIDUAL_BUILDER_CLASS );
        Validate.notEmpty( indBuilderStr,
                "Individual builder class (key="
                + _INDIVIDUAL_BUILDER_CLASS
                + ") may not be empty" );
        _indBuilder = (IndividualBuilder) MiscUtils.loadAndInstantiate(
                indBuilderStr,
                "Individual builder class" );
        _indBuilder.initialize( this );

        // Load the personality calculator class
        String personalityCalculatorStr = props.getProperty(
                _PERSONALITY_CALCULATOR_CLASS );
        if( null != personalityCalculatorStr )
        {
            _personalityCalc = (PersonalityCalculator) MiscUtils.loadAndInstantiate(
                    personalityCalculatorStr,
                    "Personality calculator class" );
        }
        else
        {
            _personalityCalc = new ConstantPersonalityCalculator();
        }
        _personalityCalc.initialize( this );

        /*  Get the flag denoting whether the total number of departed
         * individuals should be used or the number of nearest neighbors
         * that have departed */
        String useAllDepartedString = _props.getProperty(
                _USE_ALL_DEPARTED_KEY );
        if( null != useAllDepartedString )
        {
            _useAllDeparted = Boolean.parseBoolean(
                    useAllDepartedString );
            _LOG.info( "Using _useAllDeparted=[" + _useAllDeparted + "]" );
        }

        // Create the individuals
        createIndividuals();

        _LOG.trace( "Leaving initialize( props )" );
    }


    /**
     * Resets this simulation state
     */
    public void reset()
    {
        // Clear the maps of remaining and departed individuals
        _remaining.clear();
        _eligibleInitiators.clear();
        _departed.clear();
        _maxDepartedCount = 0;

        // Do we build new individuals?
        if( _rebuildIndividuals )
        {
            // Yup
            _allIndividuals.clear();
            createIndividuals();
        }
        else
        {
            // Nope, just reset them all
            Iterator<SpatialIndividual> indIter = _allIndividuals.iterator();
            while( indIter.hasNext() )
            {
                indIter.next().reset();
            }
        }

        // Add all the individuals to the remaining and eligible initiators maps
        Iterator<SpatialIndividual> indIter = _allIndividuals.iterator();
        while( indIter.hasNext() )
        {
            SpatialIndividual ind = indIter.next();
            _remaining.put( ind.getID(), ind );
            _eligibleInitiators.put( ind.getID(), ind );
        }
    }

    /**
     * Return the number of individuals remaining
     *
     * @return The number of individuals remaining
     */
    public int getRemainingCount()
    {
        return _remaining.size();
    }

    /**
     * Returns the number of individuals already departed
     *
     * @return The number of individuals departed
     */
    public int getDepartedCount()
    {
        return _departed.size();
    }

    /**
     * Returns the maximum number of individuals departed at any given time
     * during the current simulation
     *
     * @return The max number of departed individuals
     */
    public int getMaxDepartedCount()
    {
        return _maxDepartedCount;
    }

    /**
     * Returns the total number of individuals
     *
     * @return The total number of individuals
     */
    public int getIndividualCount()
    {
        return _individualCount;
    }

    /**
     * Returns an iterator over the individuals remaining in the group
     *
     * @return An iterator over the remaining individuals
     */
    public Iterator<SpatialIndividual> getRemainingIterator()
    {
        return _remaining.values().iterator();
    }

    /**
     * Returns an iterator over the individuals departed from the group
     *
     * @return An iterator over the departed individuals
     */
    public Iterator<SpatialIndividual> getDepartedIterator()
    {
        return _departed.values().iterator();
    }

    /**
     * Returns an iterator of the eligible initiators from the group.  An
     * eligible initiator is an individual that has no neighbors that are
     * initiators or following an initiator.
     *
     * @return An iterator of the eligible initiators
     */
    public Iterator<SpatialIndividual> getEligibleInitiatorsIterator()
    {
        if( _LOG.isDebugEnabled() )
        {
            StringBuilder builder = new StringBuilder();
            Iterator<Object> initIter = _eligibleInitiators.keySet().iterator();
            while( initIter.hasNext() )
            {
                builder.append( initIter.next() );
                builder.append( " " );
            }

            if( _LOG.isDebugEnabled() )
            {
                _LOG.debug( "Eligible initiators ["
                        + _eligibleInitiators.size()
                        + "]: "
                        + builder.toString() );
            }
        }
        return _eligibleInitiators.values().iterator();
    }

    /**
     * Returns an iterator for the potential followers for the specified
     * initiator
     *
     * @param initiator
     * @return An iterator of the potential followers
     */
    public Iterator<SpatialIndividual> getPotentialFollowersIterator(
            SpatialIndividual initiator )
    {
        List<SpatialIndividual> potentialFollowers =
                findPotentialFollowers( initiator );

        return potentialFollowers.iterator();
    }

    /**
     * Returns a flag denoting whether or not the initiator has any remaining
     * potential followers
     *
     * @param initiator
     * @return
     */
    public boolean hasPotentialFollowers( SpatialIndividual initiator )
    {
        List<SpatialIndividual> potentialFollowers =
                findPotentialFollowers( initiator );
        return (potentialFollowers.size() > 0);
    }

    /**
     * Returns a flag denoting whether or not the individual has departed
     *
     * @param indID
     * @return <code>true</code> if the indivdiual has departed, otherwise,
     * <code>false</code>
     */
    public boolean hasDeparted( Object indID )
    {
        return _departed.containsKey( indID );
    }

    /**
     * Returns the size of the group associated with the current initiator
     *
     * @param initiator The current initiator
     * @return The size of the group
     */
    public int getInitiatorsGroupSize( SpatialIndividual initiator )
    {
        int groupSize = 0;

        if( _useNearestNeighborGroupSize )
        {
            groupSize = initiator.getNearestNeighborCount();
        }
        else
        {
            groupSize = _individualCount;
        }

        return groupSize;
    }

    /**
     * Signals that the specified individual has initiated a group movement
     *
     * @param initiator
     */
    public void initiate( SpatialIndividual initiator )
    {
        // Tell the individual that it is initiating action
        initiator.initiateMovement( this );

        // Process the initiator
        depart( initiator );
    }

    /**
     * Signals that the specified individual has chosen to follow the specified
     * leader
     *
     * @param initiator
     * @param follower
     */
    public void follow( SpatialIndividual leader,
            SpatialIndividual follower )
    {
        // Tell the follower that it is following the leader
        follower.follow( leader );

        // Process the follower
        depart( follower );
    }

    /**
     * Signals that the specified individual has canceled a group movement
     *
     * @param individual
     */
    public void cancelInitiation( SpatialIndividual individual )
    {
        if( _LOG.isDebugEnabled() )
        {
            _LOG.debug( "Before cancel ["
                    + individual.getID()
                    + "]: eligibleInitiators=["
                    + _eligibleInitiators.size()
                    + "] remaining=["
                    + _remaining.size()
                    + "] totalFollowers=["
                    + individual.getTotalFollowerCount()
                    + "]" );
        }

        // Send it a signal so it can log some information
        individual.signalInitiationFailure( this );

        // We need to maintain a list of all the affected individuals
        List<SpatialIndividual> affected = new LinkedList<SpatialIndividual>();

        // Build the list starting with the initiator itself
        Queue<SpatialIndividual> indsToProcess =
                new LinkedList<SpatialIndividual>();
        indsToProcess.add( individual );
        while( !indsToProcess.isEmpty() )
        {
            // Get the first in the queue
            SpatialIndividual current = indsToProcess.remove();

//            _LOG.debug( "Processing ["
//                    + current.getID()
//                    + "]" );

            // Add it to the list
            affected.add( current );

            // Add it's immediate followers to the queue for processing
            Iterator<Neighbor> followerIter = current.getFollowers().iterator();
            while( followerIter.hasNext() )
            {
                indsToProcess.add( followerIter.next().getIndividual() );
            }
        }

        /* Iterate through all the affected individuals to change them from
         * departed to remaining and tell them to cancel */
        Iterator<SpatialIndividual> affectedIter = affected.iterator();
        while( affectedIter.hasNext() )
        {
            SpatialIndividual current = affectedIter.next();

//            _LOG.debug( "Processing affected ["
//                    + current.getID()
//                    + "]" );

            // Remove the individual from the departed group
            _departed.remove( current.getID() );

            // Add it to the remaining group
            _remaining.put( current.getID(), current );

            // Tell it to cancel
            current.cancel();

        }

        /* Iterate through the list again to see if they are eligible
         * initiators.  We couldn't do it during the last pass through since
         * we hadn't cleaned up all the groups yet. */
//        affectedIter = affected.iterator();
        affectedIter = _remaining.values().iterator();
        while( affectedIter.hasNext() )
        {
            SpatialIndividual current = affectedIter.next();

            // Are any of the individual's neighbors initiators or followers?
            boolean eligible = true;
            Iterator<Neighbor> neighborIter = current.getNearestNeighbors().iterator();
            while( eligible && neighborIter.hasNext() )
            {
                // Can tell by looking at the group ID
                Neighbor neighbor = neighborIter.next();
                if( null != neighbor.getIndividual().getGroupID() )
                {
                    /* The neighbor belongs to a group, the individual is NOT
                     * eligible. */
                    eligible = false;
                }
            }

            // Is the individual eligible?
            if( eligible )
            {
                // Yup
                _eligibleInitiators.put( current.getID(), current );
            }
            else
            {
                // Nope, tell them who their first mover was
                // Iterate through the list of departed individuals and
                // find the first nearest neighbor
                Iterator<SpatialIndividual> departedIter = _departed.values().iterator();
                while( departedIter.hasNext() )
                {
                    SpatialIndividual departedInd = departedIter.next();
                    if( current.isNearestNeighbor( departedInd ) )
                    {
                        current.observeFirstMover( departedInd );
                        break;
                    }
                }
            }

            /* Check all the individuals not yet departed to see if they
             * observed this individual as a first mover.  If so, reset their
             * first mover if no other neighbors have departed or if another
             * has departed, set it to that neighbor */
            Iterator<SpatialIndividual> remainingIter = _remaining.values().iterator();
            while( remainingIter.hasNext() )
            {
                SpatialIndividual currentRemaining = remainingIter.next();
                Neighbor firstMover = currentRemaining.getFirstMover();
                if( (null != firstMover)
                        && (firstMover.getIndividual().getID().equals( current.getID() )) )
                {
                    // Reset the first mover
                    currentRemaining.resetFirstMover();

                    // See if they now have another first mover
                    Iterator<SpatialIndividual> departedIter = _departed.values().iterator();
                    while( departedIter.hasNext() )
                    {
                        SpatialIndividual departedInd = departedIter.next();
                        if( currentRemaining.isNearestNeighbor( departedInd ) )
                        {
                            currentRemaining.observeFirstMover( departedInd );
                            break;
                        }
                    }
                }
            }
        }

        _LOG.debug( "After cancel: eligibleInitiators=["
                + _eligibleInitiators.size()
                + "] remaining=["
                + _remaining.size()
                + "]" );
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
     * Returns the event time calculator for this simulation
     *
     * @return The event time calculator
     */
    public EventTimeCalculator getEventTimeCalculator()
    {
        return _eventTimeCalc;
    }

    /**
     * Returns the personalityCalc for this object
     *
     * @return The personalityCalc
     */
    public PersonalityCalculator getPersonalityCalc()
    {
        return _personalityCalc;
    }

    /**
     * Returns the simIndex for this object
     *
     * @return The simIndex
     */
    public long getSimIndex()
    {
        return _simIndex;
    }


    /**
     * Sets the simIndex for this object.
     *
     * @param simIndex The specified simIndex
     */
    public void setSimIndex( long simIndex )
    {
        _simIndex = simIndex;
    }


    /**
     * Returns the simulationCount for this object
     *
     * @return The simulationCount
     */
    public int getSimulationCount()
    {
        return _simulationCount;
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
     * Returns the nearestNeighborDistance for this object
     *
     * @return The nearestNeighborDistance
     */
    public float getNearestNeighborDistance()
    {
        return _nearestNeighborDistance;
    }


    /**
     * Returns all the individuals in the simulation
     *
     * @return All the individuals
     */
    public List<SpatialIndividual> getAllIndividuals()
    {
        return new ArrayList<SpatialIndividual>( _allIndividuals );
    }

    /**
     * Returns a flag denoting whether or not all the departed individuals
     * should be used in rate calculations.
     *
     * @return <code>true</code> if all the departed individuals should be used,
     * otherwise, <code>false</code>
     */
    public boolean useAllDepartedIndividuals()
    {
        return _useAllDeparted;
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
    }

    /**
     * Create the individuals for the simulation
     */
    private void createIndividuals()
    {
        _LOG.trace( "Entering createIndividuals()" );

        // Build all the individuals
        for( int i = 0; i < _individualCount; i++ )
        {
            // Build the individual
            SpatialIndividual ind = _indBuilder.build( i );

            // Add it to the list
            _allIndividuals.add( ind );
        }

        // Have the individuals find their neighbors
        List<SpatialIndividual> lonelyInds = new LinkedList<SpatialIndividual>();
        Iterator<SpatialIndividual> indIter = _allIndividuals.iterator();
        while( indIter.hasNext() )
        {
            SpatialIndividual ind = indIter.next();
            ind.findNearestNeighbors( this );

            // Is it all alone
            if( 0 == ind.getNearestNeighborCount() )
            {
                // Yup
                lonelyInds.add( ind );
//                _LOG.warn( "Ind ["
//                        + ind.getID()
//                        + "] has no neighbors" );
            }
        }

        // If an individual has no neighbors, delete it
        Iterator<SpatialIndividual> lonelyIndIter = lonelyInds.iterator();
        while( lonelyIndIter.hasNext() )
        {
            SpatialIndividual lonely = lonelyIndIter.next();
            _LOG.warn( "Removing lonely individual ["
                    + lonely.getID()
                    + "]" );
            _allIndividuals.remove( lonely );
        }

        _LOG.trace( "Leaving createIndividuals()" );
    }

    /**
     * Process the individual as now departed
     *
     * @param ind
     */
    private void depart( SpatialIndividual ind )
    {
        Object leaderID = null;
        if( null != ind.getFirstMover() )
        {
            leaderID = ind.getFirstMover().getIndividual().getID();
        }

        if( _LOG.isDebugEnabled() )
        {
            _LOG.debug( "Depart ind=["
                    + ind.getID()
                    + "]" );
            _LOG.debug( "Following ["
                    + leaderID
                    + "]" );
            _LOG.debug( "Checking ["
                    + _eligibleInitiators.size()
                    + "] possible initiators" );
        }

        // Remove the individual from the remaining and eligible initiators groups
        _remaining.remove( ind.getID() );
        _eligibleInitiators.remove( ind.getID() );

        // Add it to the departed group
        _departed.put( ind.getID(), ind );

        // Get all the possible initiators for whom this individual is a neighbor
        List<SpatialIndividual> inds = new LinkedList<SpatialIndividual>();
        Iterator<SpatialIndividual> eligibleIter = _eligibleInitiators.values().iterator();
        while( eligibleIter.hasNext() )
        {
            SpatialIndividual current = eligibleIter.next();

//            _LOG.debug( "  Checking ind=["
//                    + current.getID()
//                    + "]" );

            // Is the original individual a nearest neighbor?
            if( ind.isMimicingNeighbor( current ) )
            {
//                _LOG.debug( "[" + current.getID() + "] is a mimicking neighbor" );

                // Yup, add them to the list
                inds.add( current );

                // Also, tell them this is their immediate leader
                current.observeFirstMover( ind );
            }
        }

        // Remove all of these individuals from the eligible initiators group
        Iterator<SpatialIndividual> indIter = inds.iterator();
        while( indIter.hasNext() )
        {
            Object id = indIter.next().getID();
            _eligibleInitiators.remove( id );
            _LOG.debug( "Removing ["
                    + id
                    + "] from eligible initiators group" );
        }

        // Update the max number of departed individuals (if needed)
        if( _maxDepartedCount < _departed.size() )
        {
            _maxDepartedCount = _departed.size();
        }
    }

    /**
     * TODO Method description
     *
     * @param initiator
     * @return
     */
    private List<SpatialIndividual> findPotentialFollowers( SpatialIndividual initiator )
    {
        List<SpatialIndividual> potentialFollowers =
                new LinkedList<SpatialIndividual>();

        // Get the initiator's group ID
        Object initiatorGroupID = initiator.getGroupID();

        StringBuilder builder = new StringBuilder();

        // Iterate through all the remaining individuals
        Iterator<SpatialIndividual> remainingIter =
                _remaining.values().iterator();
        while( remainingIter.hasNext() )
        {
            SpatialIndividual currentRemaining = remainingIter.next();

            // Does any of this individual's neighbors belong to the same group?
            boolean foundSameGroup = false;
            Iterator<Neighbor> neighborIter =
                    currentRemaining.getNearestNeighbors().iterator();
            while( !foundSameGroup && neighborIter.hasNext() )
            {
                Neighbor neighbor = neighborIter.next();

                // Do the group ID's match?
                if( initiatorGroupID.equals( neighbor.getIndividual().getGroupID() ) )
                {
                    // Yup, note it
                    foundSameGroup = true;
                }
            }

            if( foundSameGroup )
            {
                // The current individual is a potential follower
                potentialFollowers.add( currentRemaining );
                builder.append( currentRemaining.getID() );
                builder.append( " " );
            }
        }

//        if( _LOG.isDebugEnabled() )
//        {
//            _LOG.debug( "Initiator=["
//                    + initiator.getID()
//                    + "] has ["
//                    + potentialFollowers.size()
//                    + "] potential followers: "
//                    + builder.toString() );
//        }

        return potentialFollowers;
    }

}
