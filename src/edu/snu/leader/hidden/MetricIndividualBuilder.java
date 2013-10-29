/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

// Imports
import java.awt.geom.Point2D;


/**
 * MetricIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class MetricIndividualBuilder extends AbstractIndividualBuilder
{

    /**
     * Builds an individual
     *
     * @param index The index of the individual to build
     * @return The individual
     * @see edu.snu.leader.hidden.IndividualBuilder#build(int)
     */
    @Override
    public SpatialIndividual build( int index )
    {
        // Create a valid location
        Point2D location = createValidLocation( index );

        // Create the individual
        MetricSpatialIndividual ind = new MetricSpatialIndividual(
                generateUniqueIndividualID( index ),
                location,
                DEFAULT_PERSONALITY,
                DEFAULT_ASSERTIVENESS,
                DEFAULT_PREFERRED_DIR,
                DEFAULT_CONFLICT_DIR,
                DEFAULT_DESCRIBE_INITIATION_HISTORY );

        return ind;
    }

}
