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
                DEFAULT_RAW_CONFLICT,
                true );

        return ind;
    }

}
