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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.personality.PersonalityCalculator;
import edu.snu.leader.hidden.personality.PersonalityUpdateType;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


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
        public float oldPersonality;
        public boolean successful;
        public int followers;
        public float newPersonality;

        /**
         * Builds this InitiationEvent object
         *
         * @param simIndex
         * @param personality
         */
        public InitiationEvent( long simIndex, float personality )
        {
            this.simIndex = simIndex;
            this.oldPersonality = personality;
        }
    }


    /** The individual's ID */
    protected Object _id = null;

    /** The individual's group ID */
    protected Object _groupID = null;

    /** The individual's location */
    protected Point2D _location = null;

    /** The individual's personality (ranking on bold/shy).  A value of 1.0
     * denotes maximum boldness, while 0.0 denotes maximum shyness. */
    protected float _personality = 0.0f;

    /** The individual's initial personality */
    protected float _initialPersonality = 0.0f;

    /** The individual's personality after the last initiation attempt */
    protected float _personalityAfterLastInitiation = 0.0f;

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
            Point2D location,
            float personality,
            float assertiveness,
            float preferredDirection,
            float conflict,
            boolean describeInitiationHistory )
    {
        _id = id;
        _location = location;
        _personality = personality;
        _initialPersonality = personality;
        _personalityAfterLastInitiation = personality;
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
        }

        _LOG.trace( "Leaving findNearestNeighbors( simState )" );
    }

    /**
     * TODO Method description
     *
     * @param ind
     */
    public void signalNearestNeighborStatus( SpatialIndividual ind )
    {
        _mimicingNeighbors.put( ind.getID(), ind );
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
    public int getMimicingNeighborCount()
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
     * Returns the location for this object
     *
     * @return The location
     */
    public Point2D getLocation()
    {
        return _location;
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

        // Update the personality
        PersonalityCalculator personalityCalc = simState.getPersonalityCalc();
        if( null != personalityCalc )
        {
            _personality = personalityCalc.calculatePersonality(
                    this,
                    PersonalityUpdateType.TRUE_WINNER,
                    getTotalFollowerCount() );

            // Allow for bystander effects
            for( Neighbor neighbor : _nearestNeighbors )
            {
                SpatialIndividual ind = neighbor.getIndividual();
                ind._personality = personalityCalc.calculatePersonality(
                        ind,
                        PersonalityUpdateType.BYSTANDER_WINNER,
                        getTotalFollowerCount() );
            }
        }

        // Save the individual's personality and the simulation index
        _personalityAfterLastInitiation = _personality;
        _lastInitiationAttempt = simState.getSimIndex();

        // Are we keeping track of these things?
        if( _describeInitiationHistory )
        {
            _currentInitiationEvent.successful = true;
            _currentInitiationEvent.followers = getTotalFollowerCount();
            _currentInitiationEvent.newPersonality = _personality;
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
//        _failedFollowers.add( getTotalFollowerCount() );
//        _failedFollowersStats.addValue( getTotalFollowerCount() );

        // Update the personality
        PersonalityCalculator personalityCalc = simState.getPersonalityCalc();
        if( null != personalityCalc )
        {
            _personality = personalityCalc.calculatePersonality(
                    this,
                    PersonalityUpdateType.TRUE_LOSER,
                    getTotalFollowerCount() );

            // Allow for bystander effects
            for( Neighbor neighbor : _nearestNeighbors )
            {
                SpatialIndividual ind = neighbor.getIndividual();
                ind._personality = personalityCalc.calculatePersonality(
                        ind,
                        PersonalityUpdateType.BYSTANDER_LOSER,
                        getTotalFollowerCount() );
            }
        }

        // Save the individual's personality and the simulation index
        _personalityAfterLastInitiation = _personality;
        _lastInitiationAttempt = simState.getSimIndex();

        // Are we keeping track of these things?
        if( _describeInitiationHistory )
        {
            _currentInitiationEvent.successful = false;
            _currentInitiationEvent.followers = getTotalFollowerCount();
            _currentInitiationEvent.newPersonality = _personality;
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
        builder.append( prefix );
        builder.append( "personality = " );
        builder.append( _personality );
        builder.append( _NEWLINE );

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

        // Add the number of individuals with this individual as a neighbor
        builder.append( prefix );
        builder.append( "mimicing-neighbor-count = " );
        builder.append( getMimicingNeighborCount() );
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
                builder.append( currentEvent.oldPersonality );
                builder.append( "  " );
                builder.append( currentEvent.successful );
                builder.append( "  " );
                builder.append( currentEvent.newPersonality );
                builder.append( "  " );
                builder.append( currentEvent.followers );
                builder.append( "]" );
            }
            builder.append( _NEWLINE );
        }

        return builder.toString();
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
        _personality = personality;
    }

    /**
     * Returns the personality for this object
     *
     * @return The personality
     */
    public float getPersonality()
    {
        return _personality;
    }

    /**
     * Returns the initial personality for this individual
     *
     * @return The initial personality
     */
    public float getInitialPersonality()
    {
        return _initialPersonality;
    }

    /**
     * Returns the personality after the individual's last initiation attempt
     *
     * @return The personality after the individual's last initiation attempt
     */
    public float getPersonalityAfterLastInitiation()
    {
        return _personalityAfterLastInitiation;
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



}
