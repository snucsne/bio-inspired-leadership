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
package edu.snu.leader.util;

import java.util.Arrays;

/**
 * Statistics
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class Statistics
{
    /**
     * Calculates the median value of an array of float values
     *
     * @param values The array of float values
     * @return The median value
     */
    public static float median( float[] values )
    {
        if( (null == values) || (0 == values.length) )
        {
            throw new IllegalArgumentException(
                    "Array of floats must be non-null and have at least one value" );
        }

        // Sort a copy of the values
        float[] copy = Arrays.copyOf( values, values.length );
        Arrays.sort( copy );

        // Calculate the index of the middle of the array
        int medianIdx = (int) Math.floor(copy.length / 2.0);

        return copy[ medianIdx ];
    }
}
