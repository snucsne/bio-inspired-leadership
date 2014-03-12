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
