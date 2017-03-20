/*
 *  The Bio-inspired Leadership Toolkit is a set of tools used to
 *  simulate the emergence of leaders in multi-agent systems.
 *  Copyright (C) 2014 Southern Nazarene University
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.snu.leader.hidden;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.personality.PersonalityCalculator;
import edu.snu.leader.hidden.personality.PersonalityUpdateType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 * SpatialIndividual
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SpatialIndividual
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SpatialIndividual.class.getName() );

    /** Newline string.  NOTE: The old way is temporarily used
      * because a student can't use Java 1.7.  Once that is fixed, the new way will be used. */
    //protected static final String _NEWLINE = System.lineSeparator();
    protected static final String _NEWLINE = System.getProperty("line.separator");


    public static class InitiationEvent
    {
        public long simIndex;
        @Deprecated
        public float oldPersonality;
        public Map<PersonalityTrait,Float> oldPersonalityTraits =
                new EnumMap<PersonalityTrait, Float>( PersonalityTrait.class );
        public boolean successful;
        public int followers;
        @Deprecated
        public float newPersonality;
        public Map<PersonalityTrait,Float> newPersonalityTraits =
                new EnumMap<PersonalityTrait, Float>( PersonalityTrait.class );

        /**
         * Builds this InitiationEvent object
         *
         * @param simIndex
         * @param personality
         */
        public InitiationEvent( long simIndex, float personality )
        {
            this.simIndex = simIndex;
//            this.oldPersonality = personality;
            this.oldPersonalityTraits.put( PersonalityTrait.BOLD_SHY,
                    new Float( personality ) );
        }

        /**
         * Builds this InitiationEvent object
         *
         * @param simIndex
         * @param personalityTraits
         */
        public InitiationEvent( long simIndex,
                Map<PersonalityTrait,Float> personalityTraits )
        {
            this.simIndex = simIndex;
            oldPersonalityTraits.putAll( personalityTraits );
        }
    }


    /** The individual's ID */
    protected Object _id = null;

    /** The individual's group ID */
    protected Object _groupID = null;

    /** The individual's location */
    protected Vector2D _location = null;

    /** The individual's personality (ranking on bold/shy).  A value of 1.0
     * denotes maximum boldness, while 0.0 denotes maximum shyness. */
    @Deprecated
    protected float _personality = 0.0f;

    /** The individual's personality traits.  A value of 1.0 denotes the max,
     *  while denotes the minimum. */
    protected Map<PersonalityTrait,Float> _personalityTraits =
            new EnumMap<PersonalityTrait, Float>( PersonalityTrait.class );

    /** The individual's initial personality */
    @Deprecated
    protected float _initialPersonality = 0.0f;

    /** The individual's initial personality traits */
    protected Map<PersonalityTrait,Float> _initialPersonalityTraits =
            new EnumMap<PersonalityTrait, Float>( PersonalityTrait.class );

    /** The individual's personality after the last initiation attempt */
    @Deprecated
    protected float _personalityAfterLastInitiation = 0.0f;

    /** THe individual's personality traits after the last initiation attempt */
    protected Map<PersonalityTrait,Float> _personalityTraitsAfterLastInitiation =
            new EnumMap<PersonalityTrait, Float>( PersonalityTrait.class );

    /** The simulation index of this individual's last initiation attempt */
    protected long _lastInitiationAttempt = 0;

    /** The individual's assertiveness */
    protected float _assertiveness = 0.0f;

    /** The individual's preferred direction */
    protected float _preferredDirection = 0.0f;

    /** The individual's abstract conflict */
    protected float _conflict = 0.0f;

    /** The number of neighbors that are considered "nearest" */
    protected int _nearestNeighborCount = 0;

    /** The individual's nearest neighbors */
    protected List<Neighbor> _nearestNeighbors = new LinkedList<Neighbor>();

    /** Individuals for whom this individual is a nearest neighbor */
    protected Map<Object, SpatialIndividual> _mimicingNeighbors =
            new HashMap<Object, SpatialIndividual>();

    /** The neighbor this individual is following (if any) */
    protected Neighbor _leader = null;

    /** The neighbor that this individual first saw moving (if any) */
    protected Neighbor _firstMover = null;

    /** The individuals following this individual */
    protected List<Neighbor> _followers = new LinkedList<Neighbor>();

    /** The number of times the individual attempted initiation */
    protected int _initiationAttempts = 0;

    /** The number of times the individual successfully initiated */
    protected int _initiationSuccesses = 0;

    /** The number of followers this individual had when an initiation was
     * successful */
    protected List<Integer> _successfulFollowers = new LinkedList<Integer>();

    /** The number of followers this individual had when an initiation failed */
    protected List<Integer> _failedFollowers = new LinkedList<Integer>();

    /** Statistics for the number of followers in a successful initiation */
    protected DescriptiveStatistics _successfulFollowersStats =
            new DescriptiveStatistics();

    /** Statistics for the number of followers in a failed initiation */
    protected DescriptiveStatistics _failedFollowersStats =
            new DescriptiveStatistics();

    /** A record of all the individual's initiations */
    protected List<InitiationEvent> _initiationHistory =
            new LinkedList<InitiationEvent>();

    /** The current initiation event */
    protected InitiationEvent _currentInitiationEvent = null;

    /** Flag indicating that the initiation history should be described */
    protected boolean _describeInitiationHistory = false;

    /** All the failed initiators in the current simulation */
    protected List<Neighbor> _failedLeaders = new LinkedList<Neighbor>();

    /** The mean topological distance to the rest of the individuals */
    protected float _meanTopologicalDistance = 0.0f;

    /** The mean position of all the nearest neighbors */
    protected Vector2D _meanPositionOfNearestNeighbors = null;
    
    /** Distance to the mean position of all the nearest neighbors */
    protected float _distanceToMeanPositionOfNearestNeighbors = 0.0f;
    
    
    /**
     * Builds this SpatialIndividual object
     *
     * @param id
     * @param location
     * @param personality
     * @param assertiveness
     * @param preferredDirection
     * @param conflict
     * @param describeInitiationHistory
     */
    public SpatialIndividual( Object id,
            Vector2D location,
            float personality,
            float assertiveness,
            float preferredDirection,
            float conflict,
            boolean describeInitiationHistory )
    {
        _id = id;
        _location = location;
//        _personality = personality;
//        _initialPersonality = personality;
//        _personalityAfterLastInitiation = personality;
        _personalityTraits.put( PersonalityTrait.BOLD_SHY,
                new Float( personality ) );
        _initialPersonalityTraits.put( PersonalityTrait.BOLD_SHY,
                new Float( personality ) );
        _personalityTraitsAfterLastInitiation.put( PersonalityTrait.BOLD_SHY,
                new Float( personality ) );
        _assertiveness = assertiveness;
        _preferredDirection = preferredDirection;
        _conflict = conflict;
        _describeInitiationHistory = describeInitiationHistory;
    }

    /**
     * Builds this SpatialIndividual object
     *
     * @param id
     * @param location
     * @param personalityTraits
     * @param assertiveness
     * @param preferredDirection
     * @param conflict
     * @param describeInitiationHistory
     */
    public SpatialIndividual( Object id,
            Vector2D location,
            Map<PersonalityTrait,Float> personalityTraits,
            float assertiveness,
            float preferredDirection,
            float conflict,
            boolean describeInitiationHistory )
    {
        _id = id;
        _location = location;
//        _personality = personality;
//        _initialPersonality = personality;
//        _personalityAfterLastInitiation = personality;
        _personalityTraits.putAll( personalityTraits );
        _initialPersonalityTraits.putAll( personalityTraits );
        _personalityTraitsAfterLastInitiation.putAll( personalityTraits );
        _assertiveness = assertiveness;
        _preferredDirection = preferredDirection;
        _conflict = conflict;
        _describeInitiationHistory = describeInitiationHistory;
    }

    /**
     * Finds the nearest neighbors for this individual
     *
     * @param simState
     */
    public void findNearestNeighbors( SimulationState simState )
    {
        _LOG.trace( "Entering findNearestNeighbors( simState )" );

        // Get the number of nearest neighbors
        _nearestNeighborCount = simState.getNearestNeighborCount();

        // Build a priority queue to sort things for us
        PriorityQueue<Neighbor> sortedNeighbors =
                new PriorityQueue<Neighbor>();

        // Iterate through all the individuals
        Iterator<SpatialIndividual> indIter = simState.getAllIndividuals().iterator();
        while( indIter.hasNext() )
        {
            // Get the individual
            SpatialIndividual ind = indIter.next();

            // If it is us, continue on
            if( _id.equals( ind._id ) )
            {
                continue;
            }

            // Build a neighbor out of it and put it in the queue
            Neighbor neighbor = new Neighbor(
                    (float) _location.distance( ind._location ),
                    ind );
            sortedNeighbors.add( neighbor );
        }

        // Get the "nearest" neighbors
        int count = Math.min( sortedNeighbors.size(),
                _nearestNeighborCount );
        for( int i = 0; i < count; i++ )
        {
            Neighbor neighbor = sortedNeighbors.poll();
            _nearestNeighbors.add( neighbor );
            neighbor.getIndividual().signalNearestNeighborStatus( this );
//            _LOG.debug( "Nearest neighbor: id=["
//                    + getID()
//                    + "] neighbor=["
//                    + neighbor.getIndividual().getID()
//                    + "]" );
        }
        
        // Compute the mean position of the nearest neighbors
        computeMeanPositionOfNearestNeighbors();

        _LOG.trace( "Leaving findNearestNeighbors( simState )" );
    }

    /**
     * Resets all the nearest neighbor information for this individual
     */
    public void resetNearestNeighbors()
    {
        _nearestNeighbors.clear();
        _mimicingNeighbors.clear();
    }

    /**
     * TODO Method description
     *
     * @param ind
     */
    public void signalNearestNeighborStatus( SpatialIndividual ind )
    {
        _mimicingNeighbors.put( ind.getID(), ind );
        _LOG.debug( "Mimic: watched=["
                + getID()
                + "] watcher=["
                + ind.getID()
                + "]" );
    }

    /**
     * Returns true if this individual is given individual is a mimicing
     * neighbor of this individual
     *
     * @param ind The individual to test
     * @return
     */
    public boolean isMimicingNeighbor( SpatialIndividual ind )
    {
        return _mimicingNeighbors.containsKey( ind.getID() );
    }

    /**
     * Returns the number of mimicing neighbors for this individual
     *
     * @return
     */
    public int getMimickingNeighborCount()
    {
        return _mimicingNeighbors.size();
    }

    /**
     * Signals this individual that a neighbor has moved
     *
     * @param ind
     */
    public void observeFirstMover( SpatialIndividual ind )
    {
        if( null == _firstMover )
        {
            // Get the distance to the individual
            float distance = (float) _location.distance( ind._location );

            _firstMover = new Neighbor( distance, ind );
            _LOG.debug( "Ind ["
                    + getID()
                    + "] observed ["
                    + ind.getID()
                    + "]" );
        }
    }

    public void resetFirstMover()
    {
        _firstMover = null;
    }

    /**
     * Signals this individual to follow the specified individual
     *
     * @param ind
     */
    public void follow( SpatialIndividual ind )
    {
        Validate.notNull( ind, "Unable to follow a NULL leader" );

        // Get the distance to the individual
        float distance = (float) _location.distance( ind._location );

        // Follow it
        follow( new Neighbor( distance, ind ) );
    }

    /**
     * Signals this individual to follow the specified individual
     *
     * @param neighbor
     */
    public void follow( Neighbor neighbor )
    {
        Validate.notNull( neighbor, "Unable to follow a NULL leader" );

        // Store it as our leader
        _leader = neighbor;

        // Get their group ID
        _groupID = neighbor.getIndividual().getGroupID();

        // Tell the leader it is being followed
        neighbor.getIndividual().addFollower( this );
    }

    /**
     * Signals this individual to initiate movement
     *
     * @param simState
     */
    public void initiateMovement( SimulationState simState )
    {
        // If we are already following an individual, something is fubar
        Validate.isTrue( null == _leader,
                "Unable to both follow and initiate action" );

        // Get our own group ID
        _groupID = simState.generateUniqueGroupID();

        // Signal that we initiated a movement
        signalInitiationAttempt( simState );
    }

    /**
     * Signals this individual that the specified individual is following it
     *
     * @param ind
     */
    public void addFollower( SpatialIndividual ind )
    {
        // Get the distance to the individual
        float distance = (float) _location.distance( ind._location );

        // Add it to the list of followers
        _followers.add( new Neighbor( distance, ind ) );
    }

    /**
     * Cancel this individual's movement
     */
    public void cancel()
    {
        _LOG.debug( "Ind=["
                + getID()
                + "] is cancelling" );

        // If we have a leader, stop following
        if( null != _leader )
        {
            _failedLeaders.add( _leader );
        }
        _leader = null;

        // Reset our group ID
        _groupID = null;

        // Tell all our followers to cancel
        Iterator<Neighbor> followerIter = _followers.iterator();
        while( followerIter.hasNext() )
        {
            followerIter.next().getIndividual().cancel();
        }

        // Clear all our followers
        _followers.clear();

        // Reset our first mover
        _firstMover = null;
    }

    /**
     * Resets this individual's simulation state
     */
    public void reset()
    {
        // Reset all the values changed during initiation or following
        _leader = null;
        _groupID = null;
        _followers.clear();
        _firstMover = null;
        _failedLeaders.clear();
    }

    /**
     * Returns a list of individuals that are following this individual
     *
     * @return The followers
     */
    public List<Neighbor> getFollowers()
    {
        return new ArrayList<Neighbor>( _followers );
    }

    /**
     * Returns a list of all the followers (including those following this
     * individual's followers)
     *
     * @return All the followers
     */
    public List<Neighbor> getAllFollowers()
    {
        List<Neighbor> allFollowers = new LinkedList<Neighbor>( _followers );
        Iterator<Neighbor> followerIter = _followers.iterator();
        while( followerIter.hasNext() )
        {
            allFollowers.addAll( followerIter.next().getIndividual().getAllFollowers() );
        }

        return allFollowers;
    }

    /**
     * Returns the number of immediate followers
     *
     * @return The number of immediate followers
     */
    public int getImmediateFollowerCount()
    {
        return _followers.size();
    }

    public int getNearestNeighborsFollowingCount()
    {
        int count = 0;

        Iterator<Neighbor> nearestIter = _nearestNeighbors.iterator();
        while( nearestIter.hasNext() )
        {
            // Is it following us?
            Neighbor neighbor = nearestIter.next();

            // Get their leader
            Neighbor leader = neighbor.getIndividual().getLeader();
            boolean done = false;
            while( ( null != leader ) && !done )
            {
                // Is it us?
                if( leader.getIndividual().getID().equals( getID() ) )
                {
                    // Yup
                    ++count;
                    done = true;
                }
                leader = leader.getIndividual().getLeader();
            }

        }

        return count;
    }

    /**
     * Returns the total number of followers of this individual
     *
     * @return The total number of followers
     */
    public int getTotalFollowerCount()
    {
        int total = 0;

        Iterator<Neighbor> followerIter = _followers.iterator();
        while( followerIter.hasNext() )
        {
            // Increment the total to include the follower itself
            ++total;

            // Add the total number of followers of this follower
            total += followerIter.next().getIndividual().getTotalFollowerCount();
        }

        return total;
    }

    /**
     * Returns the distance of the individual from the initiator
     *
     * @return The distance
     */
    public int getDistanceToInitiator()
    {
        int distance = 0;

        // Make sure we aren't the initiator
        if( null != _leader )
        {
            // Ask our leader how far away they are
            distance = 1 + _leader.getIndividual().getDistanceToInitiator();
        }

        return distance;
    }

    /**
     * Returns the id for this object
     *
     * @return The id
     */
    public Object getID()
    {
        return _id;
    }


    /**
     * Returns the groupID for this object
     *
     * @return The groupID
     */
    public Object getGroupID()
    {
        return _groupID;
    }


    /**
     * Returns the nearestNeighbors for this object
     *
     * @return The nearestNeighbors
     */
    public List<Neighbor> getNearestNeighbors()
    {
        return _nearestNeighbors;
    }

    /**
     * Returns the mimicking neighbors for this individual
     *
     * @return The mimicking neighbors
     */
    public List<SpatialIndividual> getMimickingNeighbors()
    {
        return new LinkedList<SpatialIndividual>( _mimicingNeighbors.values() );
    }

    /**
     * TODO Method description
     *
     * @param ind
     * @return
     */
    public boolean isNearestNeighbor( SpatialIndividual ind )
    {
        boolean found = false;

        // Iterate through the nearest neighbors and check the ID
        Iterator<Neighbor> nearestIter = _nearestNeighbors.iterator();
        while( nearestIter.hasNext() )
        {
            if( nearestIter.next().getIndividual().getID().equals( ind.getID() ) )
            {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Returns the number of nearest neighbors for this individual
     *
     * @return The number of nearest neighbors for this individual
     */
    public int getNearestNeighborCount()
    {
        return _nearestNeighbors.size();
    }

    /**
     * Returns the number of nearest neighbors that have departed
     *
     * @param simState The current simulation state
     * @return The number of nearest neighbors that have departed
     */
    public int getNearestNeighborDepartedCount( SimulationState simState )
    {
        int count = 0;
        Iterator<Neighbor> iter = _nearestNeighbors.iterator();
        while( iter.hasNext() )
        {
            // Has this individual departed?
            if( simState.hasDeparted( iter.next().getIndividual().getID() ) )
            {
                // Yup
                ++count;
            }
        }

        return count;
    }

    /**
     * Returns the leader for this object
     *
     * @return The leader
     */
    public Neighbor getLeader()
    {
        return _leader;
    }

    /**
     * Returns the failed leaders for this individual
     *
     * @return The failed leaders
     */
    public List<Neighbor> getFailedLeaders()
    {
        return new LinkedList<Neighbor>( _failedLeaders );
    }

    /**
     *  Notes that the individual attempted an initiation
     *
     * @param simState The simulation's state
     */
    public void signalInitiationAttempt( SimulationState simState )
    {
        _initiationAttempts++;

        if( _describeInitiationHistory )
        {
            _currentInitiationEvent = new InitiationEvent( simState.getSimIndex(),
                    _personality );
            _initiationHistory.add( _currentInitiationEvent );
        }
    }

    /**
     * Notes that the individual was successful in initiating movement
     *
     * @param simState The simulation's state
     */
    public void signalInitiationSuccess( SimulationState simState )
    {
        _initiationSuccesses++;

        // Log our total number of followers
        _successfulFollowers.add( getTotalFollowerCount() );
        _successfulFollowersStats.addValue( getTotalFollowerCount() );

        // Save the individual's personality and the simulation index
        _personalityTraitsAfterLastInitiation.putAll( _personalityTraits );
        _lastInitiationAttempt = simState.getSimIndex();

        // Update the personality
        PersonalityCalculator personalityCalc = simState.getPersonalityCalc();
        if( null != personalityCalc )
        {
            personalityCalc.updateTraits( this,
                    PersonalityUpdateType.TRUE_WINNER,
                    simState.getCurrentTask() );

//            _LOG.warn( "Updating personalities due to success" );
//            float boldPersonality = personalityCalc.calculatePersonality(
//                    this,
//                    PersonalityUpdateType.TRUE_WINNER,
//                    getTotalFollowerCount() );
//            _personalityTraits.put( PersonalityTrait.BOLDNESS_SHYNESS,
//                    new Float( boldPersonality ) );
//
//            // Allow for bystander effects
//            for( Neighbor neighbor : _nearestNeighbors )
//            {
//                SpatialIndividual ind = neighbor.getIndividual();
//                float otherBoldPersonality = personalityCalc.calculatePersonality(
//                        ind,
//                        PersonalityUpdateType.BYSTANDER_WINNER,
//                        getTotalFollowerCount() );
//                ind._personalityTraits.put( PersonalityTrait.BOLDNESS_SHYNESS,
//                        new Float( boldPersonality ) );
//            }
        }


        // Are we keeping track of these things?
        if( _describeInitiationHistory )
        {
            _currentInitiationEvent.successful = true;
            _currentInitiationEvent.followers = getTotalFollowerCount();
//            _currentInitiationEvent.newPersonality = _personality;
            _currentInitiationEvent.newPersonalityTraits.putAll( _personalityTraits );
        }
    }

    /**
     * Notes that the individual failed in initiating movement
     *
     * @param simState The simulation's state
     */
    public void signalInitiationFailure( SimulationState simState )
    {

        // Log our total number of followers
        _failedFollowers.add( getTotalFollowerCount() );
        _failedFollowersStats.addValue( getTotalFollowerCount() );

        // Save the individual's personality and the simulation index
        _personalityTraitsAfterLastInitiation.putAll( _personalityTraits );
        _lastInitiationAttempt = simState.getSimIndex();

        // Update the personality
        PersonalityCalculator personalityCalc = simState.getPersonalityCalc();
        if( null != personalityCalc )
        {
            personalityCalc.updateTraits( this,
                    PersonalityUpdateType.TRUE_LOSER,
                    simState.getCurrentTask() );
            
//            _LOG.warn( "Updating personalities due to failure" );
//            float boldPersonality = personalityCalc.calculatePersonality(
//                    this,
//                    PersonalityUpdateType.TRUE_LOSER,
//                    getTotalFollowerCount() );
//            _personalityTraits.put( PersonalityTrait.BOLDNESS_SHYNESS,
//                    new Float( boldPersonality ) );
//
//            // Allow for bystander effects
//            for( Neighbor neighbor : _nearestNeighbors )
//            {
//                SpatialIndividual ind = neighbor.getIndividual();
//                float otherBoldPersonality = personalityCalc.calculatePersonality(
//                        ind,
//                        PersonalityUpdateType.BYSTANDER_LOSER,
//                        getTotalFollowerCount() );
//                ind._personalityTraits.put( PersonalityTrait.BOLDNESS_SHYNESS,
//                        new Float( boldPersonality ) );
//            }
        }

        // Are we keeping track of these things?
        if( _describeInitiationHistory )
        {
            _currentInitiationEvent.successful = false;
            _currentInitiationEvent.followers = getTotalFollowerCount();
//          _currentInitiationEvent.newPersonality = _personality;
          _currentInitiationEvent.newPersonalityTraits.putAll( _personalityTraits );
        }
    }

    /**
     * Describes this individual
     *
     * @return A string describing this individual
     */
    public String describe()
    {
        StringBuilder builder = new StringBuilder();

        String prefix = "individual." + getID() + ".";

        // Add the location
        builder.append( prefix );
        builder.append( "location = " );
        builder.append( String.format( "%06.4f", _location.getX() )
                    + " "
                    + String.format( "%06.4f", _location.getY() ) );
        builder.append( _NEWLINE );

        // Add the personality
        if( 1 < _personalityTraits.size() )
        {
            for( Map.Entry<PersonalityTrait, Float> entry : _personalityTraits.entrySet() )
            {
                builder.append( prefix );
                builder.append( "personality-trait." );
                builder.append( entry.getKey().name().toLowerCase() );
                builder.append( " = " );
                builder.append( entry.getValue() );
                builder.append( _NEWLINE );
            }
        }
        else
        {
            builder.append( prefix );
            builder.append( "personality = " );
            builder.append( _personalityTraits.get( PersonalityTrait.BOLD_SHY ) );
            builder.append( _NEWLINE );
        }

        // Add the assertiveness
        builder.append( prefix );
        builder.append( "assertiveness = " );
        builder.append( _assertiveness );
        builder.append( _NEWLINE );

        // Add the preferred direction
        builder.append( prefix );
        builder.append( "preferred-direction = " );
        builder.append( _preferredDirection );
        builder.append( _NEWLINE );

        // Add the conflict
        builder.append( prefix );
        builder.append( "conflict = " );
        builder.append( _conflict );
        builder.append( _NEWLINE );

//        // Add the information for the number of successful followers
//        double successfulFollowersMean = 0;
//        double successfulFollowersStdDev = 0;
//        if( 0 < getInitiationSuccesses() )
//        {
//            successfulFollowersMean = getSuccessfulFollowersMean();
//            successfulFollowersStdDev = getSuccessfulFollowersStdDev();
//        }
//        builder.append( prefix );
//        builder.append( "successful-followers-mean = " );
//        builder.append( successfulFollowersMean );
//        builder.append( _NEWLINE );
//
//        builder.append( prefix );
//        builder.append( "successful-followers-std-dev = " );
//        builder.append( successfulFollowersStdDev );
//        builder.append( _NEWLINE );
//
//        // Add the information for the number of failed followers
//        builder.append( prefix );
//        builder.append( "failed-followers-mean = " );
//        builder.append( getFailedFollowersMean() );
//        builder.append( _NEWLINE );
//
//        builder.append( prefix );
//        builder.append( "failed-followers-std-dev = " );
//        builder.append( getFailedFollowersStdDev() );
//        builder.append( _NEWLINE );

        // Add a the nearest neighbors information
        builder.append( prefix );
        builder.append( "nearest-neighbors = " );
        Iterator<Neighbor> neighborIter = getNearestNeighbors().iterator();
        while( neighborIter.hasNext() )
        {
            Neighbor currentNeighbor = neighborIter.next();
            builder.append( currentNeighbor.getIndividual().getID() );
            builder.append( " " );
        }
        builder.append( _NEWLINE );
        
        // Add the mean location of the nearest neighbors
        builder.append( prefix );
        builder.append( "mean-position-nearest-neighbors = " );
        builder.append( String.format( "%06.4f",
                        _meanPositionOfNearestNeighbors.getX() )
                + " "
                + String.format( "%06.4f",
                        _meanPositionOfNearestNeighbors.getY() ) );
        builder.append( _NEWLINE );

        // Add the distance to the mean location
        builder.append( prefix );
        builder.append( "distance-to-mean-position-nearest-neigbhors = " );
        builder.append( _distanceToMeanPositionOfNearestNeighbors );
        builder.append( _NEWLINE );

        // Add the number of individuals with this individual as a neighbor
        builder.append( prefix );
        builder.append( "mimicing-neighbor-count = " );
        builder.append( getMimickingNeighborCount() );
        builder.append( _NEWLINE );

        // Add the mean topological distance to all individuals
        builder.append( prefix );
        builder.append( "mean-topoligical-distance = " );
        builder.append( getMeanTopologicalDistance() );
        builder.append( _NEWLINE );
        
        // Add placeholders for the social network analysis data
        builder.append( prefix );
        builder.append( "eigenvector-centrality = %%%");
        builder.append( getID() );
        builder.append( "-EIGENVECTOR-CENTRALITY%%%" );
        builder.append( _NEWLINE );

        builder.append( prefix );
        builder.append( "betweenness = %%%");
        builder.append( getID() );
        builder.append( "-BETWEENNESS%%%" );
        builder.append( _NEWLINE );

        builder.append( prefix );
        builder.append( "sna-todo = %%%");
        builder.append( getID() );
        builder.append( "-sna-todo%%%" );
        builder.append( _NEWLINE );

        if( _describeInitiationHistory )
        {
            // Add the initiation history
            builder.append( prefix );
            builder.append( "initiation-history =" );
            Iterator<InitiationEvent> initiationIter = _initiationHistory.iterator();
            InitiationEvent currentEvent = null;
            while( initiationIter.hasNext() )
            {
                currentEvent = initiationIter.next();

                builder.append( " [" );
                builder.append( currentEvent.simIndex );
                builder.append( "  " );
                builder.append( currentEvent.oldPersonalityTraits.get(
                        PersonalityTrait.BOLD_SHY ) );
                builder.append( "  " );
                builder.append( currentEvent.successful );
                builder.append( "  " );
                builder.append( currentEvent.newPersonalityTraits.get(
                        PersonalityTrait.BOLD_SHY ) );
                builder.append( "  " );
                builder.append( currentEvent.followers );
                builder.append( "]" );
            }
            builder.append( _NEWLINE );
        }

        return builder.toString();
    }

    /**
     * Returns the location for this individual
     *
     * @return The location
     */
    public Vector2D getLocation()
    {
        return _location;
    }

    /**
     * Sets the location for this individual
     *
     * @param location The new location
     */
    public void setLocation( Vector2D location )
    {
        _location = location;
    }

    /**
     * Returns the mean number of followers in failed initiations
     *
     * @return The mean
     */
    public double getFailedFollowersMean()
    {
        return _failedFollowersStats.getMean();
    }

    /**
     * Returns the mean number of followers in failed initiations
     *
     * @return The mean
     */
    public double getFailedFollowersStdDev()
    {
        return _failedFollowersStats.getStandardDeviation();
    }

    /**
     * Returns the mean number of followers in successful initiations
     *
     * @return The mean
     */
    public double getSuccessfulFollowersMean()
    {
        return _successfulFollowersStats.getMean();
    }

    /**
     * Returns the mean number of followers in successful initiations
     *
     * @return The mean
     */
    public double getSuccessfulFollowersStdDev()
    {
        return _successfulFollowersStats.getStandardDeviation();
    }


    /**
     * Returns the initiationAttempts for this object
     *
     * @return The initiationAttempts
     */
    public int getInitiationAttempts()
    {
        return _initiationAttempts;
    }

    /**
     * Returns the initiationSuccesses for this object
     *
     * @return The initiationSuccesses
     */
    public int getInitiationSuccesses()
    {
        return _initiationSuccesses;
    }

    /**
     * Sets the personality for this object.
     *
     * @param personality The specified personality
     */
    public void setPersonality( float personality )
    {
        _LOG.warn( "Setting personality [" + personality + "]" );
        _personalityTraits.put( PersonalityTrait.BOLD_SHY,
                new Float( personality ) );
    }

    /**
     * Sets the specified personality trait
     *
     * @param trait
     * @param value
     */
    public void setPersonalityTrait( PersonalityTrait trait, float value )
    {
//        _LOG.warn( "Setting personality trait [" + trait + "]=[" + value + "]" );
        _personalityTraits.put( trait, new Float( value ) );
    }

    /**
     * Returns the personality for this object
     *
     * @return The personality
     */
    public float getPersonality()
    {
        return _personalityTraits.get( PersonalityTrait.BOLD_SHY );
    }

    /**
     * Returns the specified personality trait value
     *
     * @param trait
     * @return The value associated with the personality trait
     */
    public float getPersonalityTrait( PersonalityTrait trait )
    {
        float value = 0.0f;
        Float valueObj = _personalityTraits.get( trait );
        if( null != valueObj )
        {
            value = valueObj.floatValue();
        }
        else
        {
            _LOG.warn( "No value for personality trait ["
                    + trait
                    + "] was found" );
        }

        return value;
    }

    /**
     * Returns the initial personality for this individual
     *
     * @return The initial personality
     */
    public float getInitialPersonality()
    {
        return _initialPersonalityTraits.get( PersonalityTrait.BOLD_SHY );
    }

    /**
     * Returns the initial value of the personality trait
     *
     * @param trait
     * @return The initiation value of the specified personality trait
     */
    public float getInitialPersonalityTrait( PersonalityTrait trait )
    {
        float value = 0.0f;
        Float valueObj = _initialPersonalityTraits.get( trait );
        if( null != valueObj )
        {
            value = valueObj.floatValue();
        }

        return value;
    }


    /**
     * Returns the personality after the individual's last initiation attempt
     *
     * @return The personality after the individual's last initiation attempt
     */
    public float getPersonalityAfterLastInitiation()
    {
        return _personalityTraitsAfterLastInitiation.get(
                PersonalityTrait.BOLD_SHY );
    }

    /**
     * Returns the specified personality traits value after the last initiation
     *
     * @param trait
     * @return The value of the specified personality trait
     */
    public float getPersonalityTraitAfterLastInitiation( PersonalityTrait trait )
    {
        float value = 0.0f;
        Float valueObj = _personalityTraitsAfterLastInitiation.get( trait );
        if( null != valueObj )
        {
            value = valueObj.floatValue();
        }

        return value;
    }

    /**
     * Returns the simulation index of the individual's last initiation attempt
     *
     * @return The simulation index of the individual's last initiation attempt
     */
    public long getLastInitiationAttempt()
    {
        return _lastInitiationAttempt;
    }

    /**
     * Returns the assertiveness for this object
     *
     * @return The assertiveness
     */
    public float getAssertiveness()
    {
        return _assertiveness;
    }

    /**
     * Returns the preferredDirection for this object
     *
     * @return The preferredDirection
     */
    public float getPreferredDirection()
    {
        return _preferredDirection;
    }

    /**
     * Sets the preferredDirection for this object
     *
     * @param preferredDirection The preferred direction of movement
     */
    public void setPreferredDirection( float preferredDirection)
    {
        _preferredDirection = preferredDirection;
    }

    /**
     * Returns the conflict for this object
     *
     * @return The conflict
     */
    public float getConflict()
    {
        return _conflict;
    }

    /**
     * Returns the firstMover for this object
     *
     * @return The firstMover
     */
    public Neighbor getFirstMover()
    {
        return _firstMover;
    }


    /**
     * TODO Method description
     *
     * @param simState
     */
    public void calculateMeanTopoDistanceToAllIndividuals( SimulationState simState )
    {
        Map<Object, Integer> indDistanceMap = new HashMap<Object, Integer>();
        Queue<SpatialIndividual> indsToProcess = new LinkedList<SpatialIndividual>();
        
        // Automatically add all the mimicing neighbors
        for( SpatialIndividual neighbor : getMimickingNeighbors() )
        {
            indDistanceMap.put( neighbor.getID(), new Integer(1) );
            indsToProcess.add( neighbor );
        }
//        _LOG.warn( "Mimicking neighbor count ["
//                + getMimickingNeighborCount()
//                + "]" );
        
        // Process all the individuals
        while( !indsToProcess.isEmpty() )
        {
            // Get the individual and it's distance
            SpatialIndividual current = indsToProcess.remove();
            int currentDistance = indDistanceMap.get( current.getID() ).intValue();
            
            // Get all it's mimicking neighbors
            for( SpatialIndividual currentsNeighbor : current.getMimickingNeighbors() )
            {
                // Have we processed it yet?
                if( !indDistanceMap.containsKey( currentsNeighbor.getID() ) )
                {
                    // Nope
                    indDistanceMap.put( currentsNeighbor.getID(),
                            new Integer( currentDistance + 1 ) );
                    indsToProcess.add( currentsNeighbor );
                }
            }
        }
        
        // Compute the mean
        int distanceTotals = 0;
        StringBuilder builder = new StringBuilder();
        for( Integer current : indDistanceMap.values() )
        {
            distanceTotals += current.intValue();
            builder.append( current );
            builder.append( " " );
        }
        _meanTopologicalDistance = distanceTotals / ((float) indDistanceMap.size() - 1.0f);
        _LOG.warn( "Mean topological distance for ind=["
                + getID()
                + "] => ["
                + _meanTopologicalDistance
//                + "]  distances=[ "
//                + builder.toString()
                + "] total=[" + distanceTotals + "]" );
    }
    
    public float getMeanTopologicalDistance()
    {
        return _meanTopologicalDistance;
    }
    
    private void computeMeanPositionOfNearestNeighbors()
    {
        // Sum all the positions
        Vector2D sumOfPositions = Vector2D.ZERO;
        for( Neighbor neighbor : getNearestNeighbors() )
        {
            // The add method returns a new vector
            sumOfPositions = sumOfPositions.add( neighbor.getIndividual().getLocation() );
        }
        
        // Scale it by the number of nearest neighbors
        _meanPositionOfNearestNeighbors = sumOfPositions.scalarMultiply(
                1.0f / getNearestNeighborCount() );
        
        // Compute the distance to the mean position
        _distanceToMeanPositionOfNearestNeighbors =
                (float) getLocation().distance( _meanPositionOfNearestNeighbors );
    }
}
