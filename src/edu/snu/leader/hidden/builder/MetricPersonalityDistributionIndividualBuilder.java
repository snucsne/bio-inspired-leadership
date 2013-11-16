/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.builder;

// Imports
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.MetricSpatialIndividual;
import edu.snu.leader.hidden.SpatialIndividual;

import java.awt.geom.Point2D;


/**
 * MetricPersonalityDistributionIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class MetricPersonalityDistributionIndividualBuilder
        extends PersonalityDistributionIndividualBuilder
        implements IndividualBuilder
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            MetricPersonalityDistributionIndividualBuilder.class.getName() );

    /**
     * Builds an individual
     *
     * @param index The index of the individual to build
     * @return The individual
     * @see edu.snu.leader.hidden.builder.IndividualBuilder#build(int)
     */
    @Override
    public SpatialIndividual build( int index )
    {
        // Create the personality
        float personality = 0.0f;
        if( RNDistribution.GAUSSIAN.equals( _rnDist ) )
        {
            personality = createGaussianPersonality();
        }
        else if( RNDistribution.UNIFORM.equals( _rnDist ) )
        {
            personality = createUniformPersonality();
        }
        else
        {
            _LOG.error( "Unknown distribution [" + _rnDist + "]" );
            throw new RuntimeException( "Unknown distribution [" + _rnDist + "]" );
        }

        // Create a valid location
        Point2D location = createValidLocation( index );

        // Create the individual
        SpatialIndividual ind = new MetricSpatialIndividual(
                generateUniqueIndividualID( index ),
                location,
                personality,
                DEFAULT_ASSERTIVENESS,
                DEFAULT_PREFERRED_DIR,
                DEFAULT_CONFLICT_DIR,
                true );

        return ind;
    }

}
