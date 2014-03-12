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
package edu.snu.leader.hierarchy.simple;

// Imports
import edu.snu.leader.util.MiscUtils;
import ec.util.MersenneTwisterFast;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;


/**
 * Individual
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class Individual
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( Individual.class.getName() );


    /** The individual's ID */
    private Object _id = null;

    /** The individual's group ID */
    private Object _groupID = null;

    /** The individual's location */
    private Point2D _location = null;

    /** Flag indicating whether or not the individual is active */
    private boolean _active = false;

    /** The number of neighbors that are considered "nearest" */
    private int _nearestNeighborCount = 0;

    /** The individual's nearest neighbors */
    private List<Neighbor> _nearestNeighbors = new LinkedList<Neighbor>();

    /** The neighbor this individual is following (if any) */
    private Neighbor _leader = null;

    /** The individuals following this individual */
    private List<Neighbor> _followers = new LinkedList<Neighbor>();

    /** The timestep we went active */
    private long _activeTimestep = Long.MAX_VALUE;

    /** The individual's internal motivation */
    private float _motivation = 0.0f;

    /** The individual's threshold for activity */
    private float _threshold = 0.0f;

    /** The individual's skill level */
    private float _skill = 0.0f;

    /** The individual's confidence */
    private float _confidence = 0.0f;

    /** The individual's reputation */
    private float _reputation = 0.0f;

    /** The individual's boldness */
    private float _boldness = 0.0f;



    /**
     * Builds this Individual object
     *
     * @param simState
     */
    public Individual( SimulationState simState )
    {
        // Generate our id
        _id = simState.generateUniqueIndividualID();

        // Get the random number generator
        MersenneTwisterFast random = simState.getRandom();

        // Generate our location
        _location = new Point2D.Float( random.nextFloat(), random.nextFloat() );

        // Generate our motivation
//        _motivation = (random.nextFloat() / 2.0f );
        _motivation = 0.0f;

        // Generate our threshold
        float motivationRemainder = 1.0f - _motivation;
        _threshold = _motivation
                + (random.nextFloat() * motivationRemainder );

        // Generate our skill level
        _skill = (float) MiscUtils.getUnitConstrainedGaussian( random );

        // Generate our confidence
        _confidence = (float) MiscUtils.getUnitConstrainedGaussian( random );

        // Generate our reputation
        _reputation = (float) MiscUtils.getUnitConstrainedGaussian( random );

        // Generate our boldness
        _boldness = (float) MiscUtils.getUnitConstrainedGaussian( random );

        // Log it all
        _LOG.debug( "Individual: id=["
                + _id
                + "] location=["
                + _location
                + "] motivation=["
                + String.format( "%06.4f", _motivation )
                + "] threshold=["
                + String.format( "%06.4f", _threshold )
                + "] skill=["
                + String.format( "%06.4f", _skill )
                + "] confidence=["
                + String.format( "%06.4f", _confidence )
                + "] reputation=["
                + String.format( "%06.4f", _reputation )
                + "] boldness=["
                + String.format( "%06.4f", _boldness )
                + "]" );
    }


    /**
     * Initializes this individual
     *
     * @param simState
     */
    public void initialize( SimulationState simState )
    {
        // NOTE: There used to be more here
        //       Finding nearest neighbors is left abstracted to its own
        //       method in case we do more here in the future

        // Find our nearest neighbors
        findNearestNeighbors( simState );
    }

    /**
     * Updates this individual
     */
    public void update()
    {
        // Has our activation timestep been changed?
        if( Long.MAX_VALUE > _activeTimestep )
        {
            // Yup, we are active
            _active = true;
        }
    }

    /**
     * Returns a flag indicating that the motivation is larger than the
     * threshold
     *
     * @return <code>true</code> if the motivation is larger than the
     * threshold, otherwise, <code>false</code>
     */
    public boolean isMotivationOverThreshold()
    {
        return _motivation > _threshold;
    }

    /**
     * Signals this individual to follow the specified individual
     *
     * @param ind
     * @param simState
     */
    public void follow( Individual ind, SimulationState simState )
    {
        Validate.notNull( ind, "Unable to follow a NULL leader" );

        // Get the distance to the individual
        float distance = (float) _location.distance( ind._location );

        // Follow it
        follow( new Neighbor( distance, ind ), simState );
    }

    /**
     * Signals this individual to follow the specified individual
     *
     * @param neighbor
     * @param simState
     */
    public void follow( Neighbor neighbor, SimulationState simState )
    {
        Validate.notNull( neighbor, "Unable to follow a NULL leader" );

        // Store it as our leader
        _leader = neighbor;

        // Get their group ID
        _groupID = neighbor.getIndividual().getGroupID();

        // Tell the leader it is being followed
        neighbor.getIndividual().addFollower( this );

        // Log the next time step as the time we are active
        _activeTimestep = simState.getCurrentTime() + 1;
    }

    /**
     * Signals this individual to initiate action
     *
     * @param simState
     */
    public void initiateAction( SimulationState simState )
    {
        // If we are already following an individual, something is fubar
        Validate.isTrue( null == _leader,
                "Unable to both follow and initiate action" );

        // Log the next time step as the time we are active
        _activeTimestep = simState.getCurrentTime() + 1;

        // Get our own group ID
        _groupID = simState.generateUniqueGroupID();
    }

    /**
     * Signals this individual that the specified individual is following it
     *
     * @param ind
     */
    public void addFollower( Individual ind )
    {
        // Get the distance to the individual
        float distance = (float) _location.distance( ind._location );

        // Add it to the list of followers
        _followers.add( new Neighbor( distance, ind ) );
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
     * Returns the motivation for this object
     *
     * @return The motivation
     */
    public float getMotivation()
    {
        return _motivation;
    }


    /**
     * Sets the motivation for this object.
     *
     * @param motivation The specified motivation
     */
    public void setMotivation( float motivation )
    {
        _motivation = motivation;
    }


    /**
     * Returns the threshold for this object
     *
     * @return The threshold
     */
    public float getThreshold()
    {
        return _threshold;
    }


    /**
     * Sets the threshold for this object.
     *
     * @param threshold The specified threshold
     */
    public void setThreshold( float threshold )
    {
        _threshold = threshold;
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
     * Returns the active flag for this object
     *
     * @return The active flag
     */
    public boolean isActive()
    {
        return _active;
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
     * Returns the leader for this object
     *
     * @return The leader
     */
    public Neighbor getLeader()
    {
        return _leader;
    }


    /**
     * Returns the activeTimestep for this object
     *
     * @return The activeTimestep
     */
    public long getActiveTimestep()
    {
        return _activeTimestep;
    }


    /**
     * Returns the skill for this object
     *
     * @return The skill
     */
    public float getSkill()
    {
        return _skill;
    }


    /**
     * Returns the confidence for this object
     *
     * @return The confidence
     */
    public float getConfidence()
    {
        return _confidence;
    }


    /**
     * Returns the reputation for this object
     *
     * @return The reputation
     */
    public float getReputation()
    {
        return _reputation;
    }


    /**
     * Returns the boldness for this object
     *
     * @return The boldness
     */
    public float getBoldness()
    {
        return _boldness;
    }


    /**
     * Finds the nearest neighbors for this individual
     *
     * @param simState
     */
    private void findNearestNeighbors( SimulationState simState )
    {
        _LOG.trace( "Entering findNearestNeighbors( simState )" );

        // Get the number of nearest neighbors
        _nearestNeighborCount = simState.getNearestNeighborCount();

        // Build a priority queue to sort things for us
        PriorityQueue<Neighbor> sortedNeighbors =
                new PriorityQueue<Neighbor>();

        // Iterate through all the individuals
        Iterator<Individual> indIter = simState.getAllIndividuals().iterator();
        while( indIter.hasNext() )
        {
            // Get the individual
            Individual ind = indIter.next();

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
            _nearestNeighbors.add( sortedNeighbors.poll() );
        }

        _LOG.trace( "Leaving findNearestNeighbors( simState )" );
    }

}
