/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;

//Imports
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


/**
 * AgentSpatialState
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class AgentSpatialState
{
    /** Current position of this agent */
    private Vector2D _position = Vector2D.ZERO;

    /** Current velocity of this agent */
    private Vector2D _velocity = Vector2D.ZERO;

    /** Initial position of this agent */
    private Vector2D _initialPosition = Vector2D.ZERO;;

    /** Preferred destination of this agent */
    private Vector2D _preferredDestination = Vector2D.ZERO;


    /**
     * Builds this AgentSpatialState object
     *
     * @param initialPosition The agent's initial position
     * @param preferredDestination The agent's preferred destination
     */
    public AgentSpatialState( Vector2D initialPosition,
            Vector2D preferredDestination )
    {
        // Validate and store the initial position
        Validate.notNull( initialPosition,
                "Initial position may not be null" );
        _initialPosition = initialPosition;
        _position = initialPosition;

        // Validate and store the preferred destination
        Validate.notNull( preferredDestination,
                "Preferred destination may not be null" );
        _preferredDestination = preferredDestination;
    }

    /**
     * Reset's the agent's spatial state
     */
    public void reset()
    {
        // Move back to our initial position and stop moving
        _position = _initialPosition;
        _velocity = Vector2D.ZERO;
    }

    /**
     * Sets the position for this object
     *
     * @param position The specified position
     */
    public void setPosition( Vector2D position )
    {
        _position = position;
    }

    /**
     * Returns the position for this object
     *
     * @return The position.
     */
    public Vector2D getPosition()
    {
        return _position;
    }

    /**
     * Returns the velocity for this object
     *
     * @return The velocity.
     */
    public Vector2D getVelocity()
    {
        return _velocity;
    }

    /**
     * Returns the initialPosition for this object
     *
     * @return The initialPosition.
     */
    public Vector2D getInitialPosition()
    {
        return _initialPosition;
    }

    /**
     * Returns the preferredDestination for this object
     *
     * @return The preferredDestination.
     */
    public Vector2D getPreferredDestination()
    {
        return _preferredDestination;
    }

}
