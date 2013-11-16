/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

import edu.snu.leader.hidden.SpatialIndividual;

/**
 * DepartureEvent
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DepartureEvent
{

    public static enum Type
    {
        INITIATE( "I" ),
        FOLLOW( "F" ),
        CANCEL( "C" );

        private String _shortCode = "*";

        Type( String shortCode )
        {
            _shortCode = shortCode;
        }


        public String getShortCode()
        {
            return _shortCode;
        }
    }

    /** The individual departing */
    private SpatialIndividual _departed = null;

    /** The leader (if any) */
    private SpatialIndividual _leader = null;

    /** The type of departure */
    private Type _type = null;

    /** The time of the departure */
    private float _time = 0.0f;


    /**
     * Builds this DepartureEvent object
     *
     * @param leader
     * @param departed
     * @param type
     * @param time
     */
    public DepartureEvent( SpatialIndividual departed,
            SpatialIndividual leader,
            Type type,
            float time )
    {
        _departed = departed;
        _leader = leader;
        _type = type;
        _time = time;
    }

    /**
     * Returns the departed for this object
     *
     * @return The departed
     */
    public SpatialIndividual getDeparted()
    {
        return _departed;
    }

    /**
     * Returns the leader for the departed individual
     *
     * @return The leader
     */
    public SpatialIndividual getLeader()
    {
        return _leader;
    }

    /**
     * Returns the leader's id
     *
     * @return The leader's id
     */
    public Object getLeaderID()
    {
        Object id = "********";
        if( null != _leader )
        {
            id = _leader.getID();
        }
        return id;
    }

    /**
     * Returns the type for this object
     *
     * @return The type
     */
    public Type getType()
    {
        return _type;
    }

    /**
     * Returns the time for this object
     *
     * @return The time
     */
    public float getTime()
    {
        return _time;
    }

    /**
     * Updates the event's time
     *
     * @param time The new time of the event
     */
    public void updateTime( float time )
    {
        _time = time;
    }
}
