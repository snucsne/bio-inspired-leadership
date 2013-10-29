/*
 * COPYRIGHT
 */
package edu.snu.leader.hierarchy.simple.test;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.awt.geom.Point2D;
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
public class TestIndividual
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            TestIndividual.class.getName() );

    /** The individual's unique ID */
    private Object _id = null;

    /** The individual's internal motivation */
    private float _motivation = 0.0f;

    /** The individual's threshold for activity */
    private float _threshold = 0.0f;

    /** The individual's location */
    private Point2D _location = null;

    /** Flag indicating whether or not the individual is active */
    private boolean _active = false;

    /** The number of neighbors that are considered "nearest" */
    private int _nearestNeighborCount = 0;

    /** The individual's nearest neighbors */
    private List<TestNeighbor> _nearestNeighbors = new LinkedList<TestNeighbor>();

    /** The neighbor this individual is following (if any) */
    private TestNeighbor _leader = null;

    /** The timestep we went active */
    private long _activeTimestep = -1l;

    /** Amount to increment the motivation each timestep */
    private float _motivationTimeIncrement = 0.0f;

    /** Motivation increment multiplier for neighbors */
    private float _motivationNeighborIncrement = 0.0f;


    /**
     * Builds this Individual object
     *
     * @param id
     * @param motivation
     * @param threshold
     * @param location
     * @param nearestNeighborCount
     * @param motivationTimeIncrement
     * @param motivationNeighborIncrement
     */
    public TestIndividual( Object id,
            float motivation,
            float threshold,
            Point2D location,
            int nearestNeighborCount,
            float motivationTimeIncrement,
            float motivationNeighborIncrement )
    {
        // Validate the values
        Validate.notNull( id, "ID may not be null" );
        Validate.isTrue( ((motivation >= 0.0f) && (motivation <= 1.0f)),
                "Motivation should be in interval [0,1], given ["
                        + motivation
                        + "]" );
        Validate.isTrue( ((threshold >= 0.0f) && (threshold <= 1.0f)),
                "Threshold should be in interval [0,1], given ["
                        + threshold
                        + "]" );
        Validate.notNull( location, "Location may not be null" );
        Validate.isTrue( (threshold > 0),
                "Nearest neighbor count should be > 0, given ["
                        + nearestNeighborCount
                        + "]" );

        // Store the values
        _id = id;
        _motivation = motivation;
        _threshold = threshold;
        _location = location;
        _nearestNeighborCount = nearestNeighborCount;
        _motivationTimeIncrement = motivationTimeIncrement;
        _motivationNeighborIncrement = motivationNeighborIncrement;

        _LOG.debug( "motivation=["
                + String.format( "%05.3f", _motivation )
                + "] threshold=["
                + String.format( "%05.3f", _threshold )
                + "]" );
    }

    /**
     * Returns the id for this object
     *
     * @return The id.
     */
    public Object getId()
    {
        return _id;
    }



    /**
     * Returns the motivation for this object
     *
     * @return The motivation.
     */
    public float getMotivation()
    {
        return _motivation;
    }



    /**
     * Returns the threshold for this object
     *
     * @return The threshold.
     */
    public float getThreshold()
    {
        return _threshold;
    }



    /**
     * Returns the location for this object
     *
     * @return The location.
     */
    public Point2D getLocation()
    {
        return _location;
    }



    /**
     * Returns the active for this object
     *
     * @return The active.
     */
    public boolean isActive()
    {
        return _active;
    }



    /**
     * Returns the nearestNeighbors for this object
     *
     * @return The nearestNeighbors.
     */
    public List<TestNeighbor> getNearestNeighbors()
    {
        return _nearestNeighbors;
    }



    /**
     * Returns the leader for this object
     *
     * @return The leader.
     */
    public TestNeighbor getLeader()
    {
        return _leader;
    }


    /**
     * Returns the activeTimestep for this object
     *
     * @return The activeTimestep.
     */
    public long getActiveTimestep()
    {
        return _activeTimestep;
    }

    /**
     * Initialize the individual
     *
     * @param allIndividuals
     */
    public void initialize( List<TestIndividual> allIndividuals )
    {
        // Basically, we just need to find our neighbors
        // Build a priority queue to sort things for us
        PriorityQueue<TestNeighbor> sortedNeighbors =
                new PriorityQueue<TestNeighbor>();

        // Iterate through all the individuals
        Iterator<TestIndividual> indIter = allIndividuals.iterator();
        while( indIter.hasNext() )
        {
            // Get the individual
            TestIndividual ind = indIter.next();

            // If it is us, continue on
            if( _id.equals( ind._id ) )
            {
                continue;
            }

            // Build a neighbor out of it and put it in the queue
            TestNeighbor neighbor = new TestNeighbor(
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
    }

    /**
     * Updates this individual
     *
     * @param timestep The current timestep
     */
    public void update( long timestep )
    {
        // Are we already active?
        if( !isActive() )
        {
            // Nope
            _LOG.debug( _id + ": BEFORE   motivation=["
                    + String.format( "%05.3f", _motivation )
                    + "]" );


            // Add the increment for the time
            _motivation += _motivationTimeIncrement;
            _LOG.debug( _id + ": TIME     motivation=["
                    + String.format( "%05.3f", _motivation )
                    + "]" );


            // Iterate through all our neighbors
            Iterator<TestNeighbor> neighborIter = _nearestNeighbors.iterator();
            while( neighborIter.hasNext() )
            {
                TestNeighbor neighbor = neighborIter.next();

                // Are they active?
                if( neighbor.getIndividual().isActive() )
                {
                    // Yup
                    float distance = neighbor.getDistance();
                    _motivation += _motivationNeighborIncrement
                            / (1 + ( distance * distance ) );
                    _LOG.debug( _id + ": Increment from neighbor ["
                            + neighbor.getIndividual().getId()
                            + "]" );
                }
            }

            _LOG.debug( _id + ": NEIGHBOR motivation=["
                    + String.format( "%05.3f", _motivation )
                    + "] threshold=["
                    + String.format( "%05.3f", _threshold )
                    + "]" );


            // Are we over our threshold?
            if( _motivation >= _threshold )
            {
                // Yup
                _active = true;
                _activeTimestep = timestep;

                _LOG.debug( _id + ": Active at ["
                        + String.format( "%4d", timestep )
                        + "]" );

                // Follow the closest active neighbor (if there is one)
                neighborIter = _nearestNeighbors.iterator();
                while( neighborIter.hasNext() && (null == _leader) )
                {
                    TestNeighbor neighbor = neighborIter.next();

                    // Are they active?
                    if( neighbor.getIndividual().isActive() )
                    {
                        // Follow them
                        _leader = neighbor;
                        _LOG.debug( _id + ": Following ["
                                + _leader.getIndividual().getId()
                                + "]" );
                    }
                }

            }
        }
    }
}
