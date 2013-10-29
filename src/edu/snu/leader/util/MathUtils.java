/*
 * COPYRIGHT
 */
package edu.snu.leader.util;

// Imports
import ec.util.MersenneTwisterFast;

/**
 * MathUtils
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class MathUtils
{
    /**
     * Generates a random number from the exponential distribution with the
     * specified rate
     *
     * @param rate
     * @param random
     * @return
     */
    public static float generateRandomExponential( float rate,
            MersenneTwisterFast random )
    {
        float randomUniform = random.nextFloat();
        float next = (float) ( -1 * Math.log( 1 - randomUniform ) )
                / rate;

        return next;
    }

}
