/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.builder;

// Imports
import edu.snu.leader.hidden.HybridMetricTopoSpatialIndividual;
import edu.snu.leader.hidden.SpatialIndividual;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


/**
 * HybridMetricTopoIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class HybridMetricTopoIndividualBuilder extends
        AbstractIndividualBuilder
{

    /**
     * TODO Method description
     *
     * @param index
     * @return
     * @see edu.snu.leader.hidden.builder.IndividualBuilder#build(int)
     */
    @Override
    public SpatialIndividual build( int index )
    {
        // Create a valid location
        Vector2D location = createValidLocation( index );

        // Create the individual
        HybridMetricTopoSpatialIndividual ind = new HybridMetricTopoSpatialIndividual(
                generateUniqueIndividualID( index ),
                location,
                DEFAULT_PERSONALITY,
                DEFAULT_ASSERTIVENESS,
                DEFAULT_PREFERRED_DIR,
                DEFAULT_RAW_CONFLICT,
                DEFAULT_DESCRIBE_INITIATION_HISTORY );

        return ind;
    }

}
