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
import edu.snu.leader.hidden.SpatialIndividual;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


/**
 * DefaultIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DefaultIndividualBuilder extends AbstractIndividualBuilder
{

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
        // Create a valid location
        Vector2D location = createValidLocation( index );

        // Create the individual
        SpatialIndividual ind = new SpatialIndividual(
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
