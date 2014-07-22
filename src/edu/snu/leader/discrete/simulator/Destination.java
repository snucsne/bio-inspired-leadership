/*
 * The Bio-inspired Leadership Toolkit is a set of tools used to simulate the
 * emergence of leaders in multi-agent systems. Copyright (C) 2014 Southern
 * Nazarene University This program is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or at your option) any later version. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package edu.snu.leader.discrete.simulator;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


/**
 * Destination Represents a destination that agents prefer to go to.
 * 
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class Destination
{
    /** The id of the destination */
    private String _id = null;

    /** Whether or not this is the starting area */
    private boolean _isStartingArea = false;

    /** Whether or not this area is safe */
    private boolean _isSafe = false;

    /** Coordinates of this destination */
    private Vector2D _coordinates = null;

    /** Color of this destination */
    private Color _color = null;

    /** Length of the radius of the destination */
    private double _radius = 0;

    /** Whether or not this destination is good */
    private boolean _isGood = false;

    /**
     * Builds this Destination object
     * 
     * @param id
     * @param isSafe
     * @param coordinates
     * @param color
     * @param radius
     */
    public Destination( String id,
            boolean isSafe,
            Vector2D coordinates,
            Color color,
            double radius )
    {
        if( id == "D-S" )
        {
            _isStartingArea = true;
        }
        _id = id;
        _isSafe = isSafe;
        _coordinates = coordinates;
        _color = color;
        _radius = radius;
    }

    public String getID()
    {
        return _id;
    }

    public boolean isStartingArea()
    {
        return _isStartingArea;
    }

    public boolean isSafe()
    {
        return _isSafe;
    }

    public Vector2D getVector()
    {
        return _coordinates;
    }

    public Color getColor()
    {
        return _color;
    }

    public double getRadius()
    {
        return _radius;
    }

    public void setIsGood( boolean isGood )
    {
        _isGood = isGood;
    }

    public boolean isGood()
    {
        return _isGood;
    }
}
