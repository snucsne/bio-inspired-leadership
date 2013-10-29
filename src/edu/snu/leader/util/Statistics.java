/*
 * COPYRIGHT
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
