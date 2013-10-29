/*
 * COPYRIGHT
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