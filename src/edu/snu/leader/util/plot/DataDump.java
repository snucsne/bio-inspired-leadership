/*
 * COPYRIGHT
 */
package edu.snu.leader.util.plot;

// Imports
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * DataDump
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DataDump
{
    /** The world object's name */
    private String _name = null;

    /** The world object's team name */
    private String _teamName = null;

    /** The world object's collisioin bounding radius */
    private float _collisionBoundingRadius = 0.0f;

    /** A chronological list of thw world object's positions */
    private List<Vector3D> _positions = new LinkedList<Vector3D>();

    /**
     * Builds this DataDump object
     */
    public DataDump()
    {
        // Do nothing
    }

    /**
     * Returns the world object's name
     *
     * @return The name
     */
    public String getName()
    {
        return _name;
    }

    /**
     * Returns the world object's team name
     *
     * @return The name
     */
    public String getTeamName()
    {
        return _teamName;
    }

    /**
     * Returns the world object's collision bounding radius
     *
     * @return The bounding radius
     */
    public float getCollisionBoundingRadius()
    {
        return _collisionBoundingRadius;
    }

    /**
     * Returns all the positions of a world object
     *
     * @return The positions
     */
    public List<Vector3D> getAllPositions()
    {
        return new ArrayList<Vector3D>( _positions );
    }

    /**
     * Sets the world object's name
     *
     * @param name The world object's name
     */
    public void setName( String name )
    {
        if( null == name )
        {
            throw new IllegalArgumentException( "Name may not be null" );
        }
        _name = name;
    }

    /**
     * Sets the world object's team name
     *
     * @param teamName The world object team's name
     */
    public void setTeamName( String teamName )
    {
        if( null == teamName )
        {
            throw new IllegalArgumentException( "Team name may not be null" );
        }
        _teamName = teamName;
    }

    /**
     * Sets the world object's collision bounding radius
     *
     * @param radius The world object's collision bounding radius
     */
    public void setCollisionBoundingRadius( float radius )
    {
        _collisionBoundingRadius = radius;
    }

    /**
     * Adds a position to the list of positions
     *
     * @param position A position
     */
    public void addPosition( Vector3D position )
    {
        _positions.add( position );
    }

}
