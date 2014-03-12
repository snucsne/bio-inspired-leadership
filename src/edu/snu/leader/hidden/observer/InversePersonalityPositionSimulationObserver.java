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
package edu.snu.leader.hidden.observer;

// Imports
import java.util.Iterator;

import edu.snu.leader.hidden.SpatialIndividual;


/**
 * InversePersonalityPositionSimulationObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class InversePersonalityPositionSimulationObserver extends
        AbstractSimulationObserver
{

    /**
     * Prepares the simulation for execution
     *
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#simSetUp()
     */
    @Override
    public void simSetUp()
    {
        // Is this the first simulation?
        if( 0 == _simState.getSimulationCount() )
        {
            /* Yup.  Recalculate all the personalities to be inversely-related
             * to the number of mimicking neighbors */

            // Find the min and max number of mimicking neighbors
            SpatialIndividual current = null;
            int minMimics = Integer.MAX_VALUE;
            int maxMimics = Integer.MIN_VALUE;
            Iterator<SpatialIndividual> iter = _simState.getAllIndividuals().iterator();
            while( iter.hasNext() )
            {
                // Get the current individuals number of mimics
                current = iter.next();
                int currentMimics = current.getMimicingNeighborCount();

                // Is it smaller than the current minimum?
                if( currentMimics < minMimics )
                {
                    // Yup
                    minMimics = currentMimics;
                }

                // Is it bigger than the current maximum?
                if( currentMimics > maxMimics )
                {
                    // Yup
                    maxMimics = currentMimics;
                }
            }

            // Calculate the difference in mimics
            float diff = maxMimics - minMimics;

            // Iterate through the individuals again to set their personalities
            iter = _simState.getAllIndividuals().iterator();
            while( iter.hasNext() )
            {
                // Get the current individual and their number of mimics
                current = iter.next();
                int currentMimics = current.getMimicingNeighborCount();

                /* Calculate the personality by using the number of mimics
                 * to scale the value and then ensure that it is between
                 * the bounds of 0.1 and 0.9 */
                float percentage = (currentMimics - minMimics) / diff;
                float personality = ( (1.0f - percentage) * 0.8f ) + 0.1f;
                current.setPersonality( personality );
            }
        }
    }

}
