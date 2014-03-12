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

/**
 * Neighbor
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class Neighbor implements Comparable<Neighbor>
{
    /** The distance to the neighbor */
    private float _distance = 0.0f;

    /** The individual */
    private SpatialIndividual _ind = null;

    /**
     * Builds this Neighbor object
     *
     * @param distance
     * @param ind
     */
    public Neighbor( float distance, SpatialIndividual ind )
    {
        _distance = distance;
        _ind = ind;
    }

    /**
     * Returns the distance to the neighbor
     *
     * @return The distance
     */
    public float getDistance()
    {
        return _distance;
    }


    /**
     * Returns the individual for this object
     *
     * @return The individual
     */
    public SpatialIndividual getIndividual()
    {
        return _ind;
    }

    /**
     * TODO Method description
     *
     * @param o
     * @return
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo( Neighbor neighbor )
    {
        int result = 0;
        if( _distance < neighbor._distance )
        {
            result = -1;
        }
        else if( _distance > neighbor._distance )
        {
            result = 1;
        }
        return result;
    }

}